package pt.evolute.dbtransfer.transfer;

import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import pt.evolute.utils.arrays.Virtual2DArray;
import pt.evolute.utils.db.Connector;
import pt.evolute.utils.string.UnicodeChecker;
import pt.evolute.dbtransfer.Constants;
import pt.evolute.dbtransfer.db.DBConnection;
import pt.evolute.dbtransfer.db.DBConnector;
import pt.evolute.dbtransfer.db.beans.ColumnDefinition;
import pt.evolute.dbtransfer.db.beans.ConnectionDefinitionBean;
import pt.evolute.dbtransfer.db.beans.ForeignKeyDefinition;
import pt.evolute.dbtransfer.db.beans.Name;
import pt.evolute.dbtransfer.db.helper.Helper;
import pt.evolute.dbtransfer.db.helper.HelperManager;
import pt.evolute.dbtransfer.db.jdbc.JDBCConnection;
import pt.evolute.utils.arrays.exception.EndOfArrayException;


/**
 *
 * @author  lflores
 */
public class Mover extends Connector implements Constants
{
	public final long MAX_READ_ROWS;
	public final boolean IGNORE_BLOB;
	
    public static final int MAX_BATCH_ROWS = 1024;
    public static final int MAX_SHARED_ROWS = 1000000;

    private final Name TABLES[];
    private final ConnectionDefinitionBean SRC;
    private final ConnectionDefinitionBean DST;
    
    private final DBConnection CON_SRC;
    private final DBConnection CON_DEST;
    private final boolean CHECK_DEPS;
    private final boolean USE_DEST_FOR_DEPS;
    private final boolean ESCAPE_UNICODE;

    private static final long MAX_MEM = Runtime.getRuntime().maxMemory();

    private boolean sleeping = false;
    private int readRows = 0;
    
    private static long lastGc = 0;
//	private static int oom = 0;

    /** Creates a new instance of Mover */
    public Mover( Properties props, ConnectionDefinitionBean src, ConnectionDefinitionBean dst )
            throws Exception
    {
        SRC = src;
        DST = dst;
        ESCAPE_UNICODE = "true".equals( props.getProperty( TRANSFER_ESCAPE_UNICODE ) );
        boolean ignoreEmpty = Boolean.parseBoolean( props.getProperty( ONLY_NOT_EMPTY, "false" ) );

        CON_SRC = DBConnector.getConnection( SRC, ignoreEmpty );

        CON_DEST = DBConnector.getConnection( DST, false );
System.out.println( "Using max " + ( MAX_MEM / ( 1024 * 1024 ) ) + " MB of memory" );

        CHECK_DEPS = "true".equalsIgnoreCase( props.getProperty( TRANSFER_CHECK_DEPS, "false" ) );
        USE_DEST_FOR_DEPS = "true".equalsIgnoreCase( props.getProperty( TRANSFER_USE_DEST_FOR_DEPS, "false" ) );
        IGNORE_BLOB = "true".equalsIgnoreCase( props.getProperty( TRANSFER_IGNORE_BLOBS, "false" ) );
        if( IGNORE_BLOB )
        {
        	System.out.println( "Ignoring BLOBS" );
        }
        String str = props.getProperty( TRANSFER_MAX_READ_ROWS );
        long maxRead = Long.MAX_VALUE;
        if( str != null && !str.isEmpty() )
        {
        	try
        	{
        		maxRead = Long.parseLong( str );
        	}
        	catch( NumberFormatException ex )
        	{
        		System.out.println( "Invalid property: " + Constants.TRANSFER_MAX_READ_ROWS + " - " + str );
        	}
        }
        MAX_READ_ROWS = maxRead;
        if( MAX_READ_ROWS != Long.MAX_VALUE )
        {
        	System.out.println( "Max read rows: " + MAX_READ_ROWS );
        }
        
        List<Name> v = CON_SRC.getTableList();
        if( CHECK_DEPS )
        {
            System.out.println( "Reordering tables for dependencies (" + v.size() + " tables)" );
            if( USE_DEST_FOR_DEPS )
            {
                v = reorder( CON_DEST.getTableList() );
            }
            else
            {
                v = reorder( v );
            }
        }
        TABLES = v.toArray( new Name[ v.size() ] );
    }

    private List<Name> reorder(List<Name> inputList) throws Exception 
    {
        Map<Name,Name> noDepsTablesMap = new HashMap<Name,Name>();
        List<Name> deps = new ArrayList<Name>();
        List<Name> list = new ArrayList<Name>();
        while( !inputList.isEmpty() || !deps.isEmpty() )
        {
            if( !deps.isEmpty() )
            {
                inputList.addAll( deps );
            }
            for( Name n: inputList )
            {
                if( JDBCConnection.debug )
                {
                    System.out.println( "Testing: " + n.originalName );
                }
                List<ForeignKeyDefinition> fks = CON_DEST.getForeignKeyList( n );
                boolean ok = true;
                for( ForeignKeyDefinition fk: fks )
                {
                    if( !noDepsTablesMap.containsKey( fk.columns.get( 0 ).referencedTable ) )
                    {
                        if( JDBCConnection.debug )
                        {
                            System.out.println( "Depends: " + fk.columns.get( 0 ).referencedTable.originalName );
                        }
                        deps.add( n );
                        ok = false;
                        break;
                    }
                }
                if( ok )
                {
                    list.add( n );
                    noDepsTablesMap.put( n, n );
                }
            }
            inputList.clear();
        }
        System.out.println( "Reordered (" + list.size() + " tables)" );
        return list;
    }

    private static boolean testRow( Virtual2DArray array, int row )
    {
        boolean rowOk = true;
        try
        {
            array.get( row, 0 );
        }
        catch( EndOfArrayException ex )
        {
            rowOk = false;
        }
        return rowOk;
    }
    
    public void moveDB()
            throws Exception
    {
        System.out.println( "Moving (" + TABLES.length + " tables)" );
        final List<Object> TEMP = new LinkedList<Object>();
        List<AsyncStatement> threads = new LinkedList<AsyncStatement>();
        Helper tr = HelperManager.getTranslator( DST.getUrl() );
        
        ReportThread rt = new ReportThread( this, AsyncStatement.getRunningThreads() );
        rt.start();
        
        for( int i = 0; i < TABLES.length; ++i )
        {
            Virtual2DArray rs = CON_SRC.getFullTable( TABLES[ i ] );
            System.out.println( "Moving table: " + TABLES[ i ] );//+ " (" + rs.rowCount() + " rows)" );
            boolean hasData = rs != null;
            if( hasData  )
            {
                hasData = testRow( rs, 0 );
            }
            if( hasData )
            {
                StringBuilder buff = new StringBuilder( "INSERT INTO " );
                StringBuilder args = new StringBuilder();
                buff.append( TABLES[ i ].saneName );
                buff.append( " ( " );

                List<ColumnDefinition> columns = CON_SRC.getColumnList( TABLES[ i ] );
                if( columns.isEmpty() )
                {
                    System.out.println( "NO COLUMNS FOR: " + TABLES[ i ] );
                }
                int typesCache[] = new int[ columns.size() ];
                for( int j = 0; j < columns.size(); ++j )
                {
                    typesCache[ j ] = columns.get( j ).sqlType;
                    if( typesCache[ j ] == 0 )
                    {
                        System.out.println( "Can't resolve Type: " + columns.get( j ).name + " / " + columns.get( j ).sqlTypeName );
                    }
                }
                for( int j = 0; j < columns.size(); ++j )
                {
                	if( !IGNORE_BLOB 
                    		|| ( typesCache[ j ] != Types.BLOB
                    		&& typesCache[ j ] != Types.LONGVARBINARY 
                    		&& typesCache[ j ] != Types.VARBINARY ) )
                	{
	                    if( j != 0 )
	                    {
	                        buff.append( ", " );
	                        args.append( ", " );
	                    }
	                    buff.append( tr.outputName( columns.get( j ).name.saneName ) );
	                    args.append( "?" );
                	}
                }
                buff.append( " ) VALUES ( " );
                buff.append( args );
                buff.append( " )" );
                String insert = buff.toString();
//			PreparedStatement pstm = CON_DEST.prepareStatement( insert );
System.out.println( "I: " + i + " " + TABLES[ i ].saneName + " sql: " + insert + " colList.sz: " + columns.size() );
                
                // TODO - do only if table has identity (when target is sqlserver)
                String pre = tr.preLoadSetup( TABLES[ i ].saneName );
                String post = tr.postLoadSetup( TABLES[ i ].saneName );
                AsyncStatement astm = new AsyncStatement( typesCache, CON_DEST, insert, TABLES[ i ].saneName, pre, post, IGNORE_BLOB );
                threads.add( astm );
                int rows = 0;

                int row = 0;
                while( hasData )
                {
                    for( int j = 0; j < typesCache.length; ++j )
                    {
                        Object data = null;
                        if( !IGNORE_BLOB 
                        		|| ( typesCache[ j ] != Types.BLOB
                        		&& typesCache[ j ] != Types.LONGVARBINARY 
                        		&& typesCache[ j ] != Types.VARBINARY ) )
                        {
	                        	data = rs.get( row, j );
	                        if( ESCAPE_UNICODE && data != null 
	                                        && data instanceof String )
	                        {
	                            data = UnicodeChecker.parseToUnicode( ( String )data );
	                        }
	                        TEMP.add( data );
                        }
//					pstm.setObject( j, rs.getObject( j ), rsmd.getColumnType( j ) );
                    }
                    ++rows;
                    astm.DATA_SHARED.addAll( TEMP );
                    TEMP.clear();
                    if( ( rows % MAX_BATCH_ROWS ) == 0 )
                    {
                        readRows = rows;
                        //System.out.print( "+" + i + "." + ( rows / MAX_BATCH_ROWS ) );
                        while( !astm.DATA_SHARED.isEmpty() 
                                && ( astm.DATA_SHARED.size() > MAX_SHARED_ROWS * columns.size() || cacheLimit() ) )
                        {
                            try
                            {
//                                System.out.print( "W" );
                                sleeping = true;
                                Thread.sleep( 1000 );
                                sleeping = false;
                            }
                            catch( InterruptedException ex )
                            {
                            }
                        }
                    }
                    ++row;
                    hasData = row < MAX_READ_ROWS && testRow( rs, row );
                }
                System.out.println( "Done reading table: " + TABLES[ i ].saneName + " (" + rows + " rows read)" );
                astm.stopThread();
//				astm.join();
                while( AsyncStatement.waitingThreads() > 0 /* !astm.DATA_SHARED.isEmpty() /*( 3 * typesCache.length * MAX_BATCH_ROWS )*/ )
                {
                    try
                    {
//                        System.out.print( "H" );
                        sleeping = true;
                        Thread.sleep( 500 );
                        sleeping = false;
                        cacheLimit();
                    }
                    catch( InterruptedException ex )
                    {
                        ex.printStackTrace();
                    }
                }
            }
        }
        rt.stopReporting();
        for( AsyncStatement async: threads )
        {
            if( async.isAlive() )
            {
                System.out.println( "Waiting for thread: " + async.getName() );
                async.join();
            }
        }
    }

    protected static synchronized boolean cacheLimit()
    {
        boolean limit = false;
        long totalMem = Runtime.getRuntime().totalMemory();
        if( ( double )totalMem > .75 * MAX_MEM )
        {
            long freeMem = Runtime.getRuntime().freeMemory();
            limit = (double)freeMem < .30 * MAX_MEM;
            if( limit )
            {
                if( (double)freeMem < .1 * MAX_MEM )
                {
//                System.out.println( "free: " + freeMem / (1024*1024) 
//                        + "/" + MAX_MEM / (1024*1024) + "MB" );
                    if( ( System.currentTimeMillis() - lastGc ) > 60 * 1000 )
                    {
                        System.out.println( "Invoking GC" );
                        System.gc();
                        lastGc = System.currentTimeMillis();
                    }
                }
            }
        }
        return limit;
    }

    public int getReadCount() 
    {
        return readRows;
    }
    
    public boolean isSleeping()
    {
        return sleeping;
    }
}
