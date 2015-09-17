package pt.evolute.dbtransfer.transfer;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import pt.evolute.dbtransfer.db.DBConnection;
import pt.evolute.dbtransfer.db.helper.Helper;

/**
 *
 * @author  lflores
 */
public class AsyncStatement extends Thread
{
    public static int PARALLEL_THREADS = 1;

    private static final List<AsyncStatement> THREADS = Collections.synchronizedList( new LinkedList<AsyncStatement>() );

    private static final List<AsyncStatement> R_THREADS = Collections.synchronizedList( new LinkedList<AsyncStatement>() );

    private final int colTypes[];
    private final DBConnection CONN;
    private final String INSERT;

    private final String id;

    private final List<Object> PRIVATE_DATA = new LinkedList<Object>();
    public final List<Object> DATA_SHARED = Collections.synchronizedList( new ArrayList<Object>() );

//	private final Object LOCK = new Object();

    private final String preSetup;
    private final String postSetup;
    private final Helper destHelper;

    private final Boolean IGNORE_BLOB;
    private final int EFFECTIVE_COLUMNS;
    
    private boolean run = true;

    private boolean sleep = false;
    private int writeRows = 0;
//	private int count = 0;

    /** Creates a new instance of AsyncStatement
 * @param types array with column types
 * @param con DBConnection to use
 * @param insert INSERT query
 * @param n identifier
 * @param preSetup pre-exec query
 * @param postSetup post-exec query*/
    public AsyncStatement( int types[], DBConnection con, String insert, String threadId, 
                    String preSetup, String postSetup, boolean ignoreBlob )
    {
        this.preSetup = preSetup;
        this.postSetup = postSetup;
        this.destHelper = con.getHelper();
        IGNORE_BLOB = ignoreBlob;
        id = threadId;
        colTypes = types;
        CONN = con;
        INSERT = insert;
        setName( "AsyncStatement " + id );
        
        EFFECTIVE_COLUMNS = calculateEffectiveColumns( IGNORE_BLOB, types );
        
        synchronized( R_THREADS )
        {
            if( R_THREADS.size() >= PARALLEL_THREADS )
            {
                THREADS.add( this );
            }
            else
            {
                R_THREADS.add( this );
                start();
            }	
        }
System.out.println( "Async " + id + " created \n" + INSERT + "\nisRunning? " + isAlive() );
    }

    private static int calculateEffectiveColumns( boolean noBlob, int[] types) 
    {
		int cols = 0;
		if( noBlob )
		{
			for( int i = 0; i < types.length; ++i )
			{
				if( types[ i ] != Types.BLOB
                		&& types[ i ] != Types.LONGVARBINARY 
                		&& types[ i ] != Types.VARBINARY )
                {
                	++cols;
                }
			}
		}
		else
		{
			cols = types.length;
		}
    	return cols;
	}

	@Override
    public void run()
    {
        System.out.println( "\nStarting " + getName() );
        try
        {
            if( preSetup != null )
            {
                    System.out.println( "Setup query: " + preSetup );
                    CONN.executeQuery( preSetup );
            }
            int rows = 0;
            
            PreparedStatement pStm = CONN.prepareStatement( INSERT );
            CONN.executeQuery( "BEGIN;" );
            // enquanto a thread nao for parada
            // ou tiver dados locais
            // ou ainda houver dados no buffer partilhado
            boolean rowOK = false;
            while( run || !PRIVATE_DATA.isEmpty() || !DATA_SHARED.isEmpty() )
            {
            	
                checkData();
                rowOK = addRowToStatement( pStm );
                //for de objectos numa linha
                if( rowOK )
                {
                    ++rows;
                    writeRows = rows;
                    if( ( rows % Mover.MAX_BATCH_ROWS ) == 0 /* && OK */ )
                    {
                    	try
                    	{
                    		if( pStm.isClosed() || !pStm.getConnection().isValid( 5 ) )
                    		{
                    			pStm = CONN.prepareStatement( INSERT );
                    			CONN.executeQuery( "BEGIN;" );
                    		}
                    	}
                    	catch( SQLException ex )
                    	{
                    		pStm = CONN.prepareStatement( INSERT );
                    		CONN.executeQuery( "BEGIN;" );
                    	}
//                      System.out.print( "-" + id + "." + ( rows / Mover.MAX_BATCH_ROWS ) );
                        
                    	pStm.executeBatch();
                    	CONN.executeQuery( "COMMIT;" );
                    	CONN.executeQuery( "BEGIN;" );
                    	rowOK = false;
                    }
                }
            }
            if( rowOK )
            {
//                    System.out.print( "|" + id );
                pStm.executeBatch();
                CONN.executeQuery( "COMMIT;" );
                pStm.close();
            }
            System.out.println( "Done writing table: " + id + " (" + rows + " rows written)" );
            if( postSetup != null )
            {
//                    System.out.println( "Setup query: " + postSetup );
                    CONN.executeQuery( postSetup );
            }
//            System.out.println( "DONE " + id );
        }
        catch( Exception ex )
        {
            System.out.println( "EX in: " + id + " " + INSERT.substring( 0, 30 ) );
            ex.printStackTrace( System.out );
            ex.printStackTrace( System.err );
            if( ex instanceof SQLException )
            {
                SQLException sex = ( SQLException )ex;
                while( ( sex = sex.getNextException() ) != null )
                {
                    sex.printStackTrace();
                }
            }
        }
        R_THREADS.remove( this );
        synchronized( R_THREADS )
        {
            if( R_THREADS.size() < PARALLEL_THREADS )
            {
                if( !THREADS.isEmpty() )
                {
                    AsyncStatement t = THREADS.remove( 0 );
                    R_THREADS.add( t );
                    t.start();
                }	
            }
        }
    }

    private void checkData()
    {
        if( PRIVATE_DATA.size() < EFFECTIVE_COLUMNS )
        {
            synchronized(DATA_SHARED)
            {
                PRIVATE_DATA.addAll(DATA_SHARED);
                DATA_SHARED.clear();
            }
            if( PRIVATE_DATA.size() < EFFECTIVE_COLUMNS )
            {
                if( run )
                {
                    waitForData();
                }
            }
        }
    }

    private boolean addRowToStatement(PreparedStatement pStm) throws SQLException
    {
        int col = 0;
        int param;
        if( PRIVATE_DATA.size() >= EFFECTIVE_COLUMNS )
        {
        	param = 0;
            for( col = 0; col < colTypes.length; ++col)
            {
            	if( !IGNORE_BLOB 
                		|| ( colTypes[ col ] != Types.BLOB
                		&& colTypes[ col ] != Types.LONGVARBINARY 
                		&& colTypes[ col ] != Types.VARBINARY ) )
                {
	                Object o = PRIVATE_DATA.remove(0);
	                int type = colTypes[col];
	//                System.out.println( "D: " + o + " type: " + type );
	                destHelper.setPreparedValue( pStm, param++, o, type );
                }
            }
        }
        boolean ok = col == colTypes.length;
        if( ok )
        {
            pStm.addBatch();
        }
        return ok;
    }

    private void waitForData()
    {
            // se a thread estiver a correr e nao houver dados partilhados, espero
        while( DATA_SHARED.isEmpty() )
        {
            if( run )
            {
                try
                {
                    sleep = true;
                    sleep(1000);
                    sleep = false;
                }
                catch(InterruptedException ex)
                {
                }
            }
            else
            {
                break;
            }
        }
    }

    public void stopThread()
    {
        run = false;
    }

    public static int waitingThreads()
    {
        return THREADS.size();
    }
    
    public boolean isSleeping()
    {
        return sleep;
    }
    
    private int lastgetWriteRows = 0;
    
    public int getAndResetWriteRows()
    {
        int w = writeRows - lastgetWriteRows;
        lastgetWriteRows = writeRows;
        return w;
    }
    
    public static List<AsyncStatement> getRunningThreads()
    {
        return R_THREADS;
    }

    public int getSharedRowsSize() 
    {
        return DATA_SHARED.size() / EFFECTIVE_COLUMNS;
    }
    
    public int getPrivateRowsSize() 
    {
        return PRIVATE_DATA.size() / EFFECTIVE_COLUMNS;
    }
}
