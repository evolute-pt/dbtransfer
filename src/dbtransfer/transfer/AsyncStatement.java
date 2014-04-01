/*
 * AsyncStatement.java
 *
 * Created on 10 de Marco de 2005, 19:36
 */

package dbtransfer.transfer;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import dbtransfer.db.DBConnection;
import dbtransfer.db.helper.Helper;

/**
 *
 * @author  lflores
 */
public class AsyncStatement extends Thread
{
	public static int PARALLEL_THREADS = 1;
	
	private static final List<Thread> THREADS = Collections.synchronizedList( new LinkedList<Thread>() );
	
	private static final List<Thread> R_THREADS = Collections.synchronizedList( new LinkedList<Thread>() );
	
	private final int colTypes[];
	private final DBConnection CONN;
	private final String INSERT;
	
	private final int id;
	
	private final List<Object> PRIVATE_DATA = new LinkedList<Object>();
	public final List<Object> DATA_SHARED = Collections.synchronizedList( new LinkedList<Object>() );
	
//	private final Object LOCK = new Object();
	
	private final String preSetup;
	private final String postSetup;
	private final Helper destHelper;
	
	private boolean run = true;
	
//	private int count = 0;
	
	/** Creates a new instance of AsyncStatement */
	public AsyncStatement( int types[], DBConnection con, String insert, int n, 
			String preSetup, String postSetup, Helper helper )
	{
		this.preSetup = preSetup;
		this.postSetup = postSetup;
		this.destHelper = helper;
		id = n;
		colTypes = types;
		CONN = con;
		INSERT = insert;
		setName( "AsyncStatement " + n );
System.out.print( "Async " + n + " created \n" + INSERT + "\nisRunning? " );
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
			// enquanto a thread nao for parada
			// ou tiver dados locais
			// ou ainda houver dados no buffer partilhado
			boolean rowOK = false;
			while( run || !PRIVATE_DATA.isEmpty() || !DATA_SHARED.isEmpty() )
			{
				rowOK = addRowToStatement( pStm );
				//for de objectos numa linha
				if( rowOK )
				{
					pStm.addBatch();
				}
				++rows;
				if( ( rows % Mover.MAX_BATCH_ROWS ) == 0 /* && OK */ )
				{
					System.out.print( "-" + id + "." + ( rows / Mover.MAX_BATCH_ROWS ) );
					pStm.executeBatch();
					rowOK = false;
				}
			}
			if( rowOK )
			{
				System.out.print( "|" + id );
				pStm.executeBatch();
				pStm.close();
			}
			if( postSetup != null )
			{
				System.out.println( "Setup query: " + postSetup );
				CONN.executeQuery( postSetup );
			}
			System.out.println( "DONE " + id );
		}
		catch( Exception ex )
		{
			System.out.println( "EX in: " + id + " " + INSERT.substring( 0, 30 ) );
			ex.printStackTrace();
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
					Thread t = THREADS.remove( 0 );
					R_THREADS.add( t );
					t.start();
				}	
			}
		}
	}

	private boolean addRowToStatement(PreparedStatement pStm) throws SQLException
	{
		int col = 0;
		for( ; col < colTypes.length; ++col)
		{
			// se ja nao tiver dados locais e estiver a correr tento ir buscar mais dados
			if(PRIVATE_DATA.isEmpty() && run)
			{
				waitForData();
			}
			if(PRIVATE_DATA.size() < colTypes.length)
			{
				synchronized(DATA_SHARED)
				{
					PRIVATE_DATA.addAll(DATA_SHARED);
					DATA_SHARED.clear();
				}
			}
			if(PRIVATE_DATA.isEmpty())
			{
				System.out.print("E" + id);
				break;
			}
			else
			{
				Object o = PRIVATE_DATA.remove(0);
				int type = colTypes[col];
				destHelper.setPreparedValue( pStm, col, o, type );
			}
		}
		boolean ok = col == colTypes.length;
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
					System.out.print("w." + id);
					sleep(1000);
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
}
