/*
 * Main.java
 *
 * Created on February 6, 2005, 11:47 PM
 */

package dbtransfer;

import java.io.FileInputStream;
import java.sql.SQLException;
import java.util.Date;
import java.util.Properties;

import dbtransfer.analyse.Analyser;
import dbtransfer.constrain.Constrainer;
import dbtransfer.db.helper.HelperManager;
import dbtransfer.db.jdbc.JDBCConnection;
import dbtransfer.diff.Diff;
import dbtransfer.transfer.AsyncStatement;
import dbtransfer.transfer.Mover;

/**
 *
 * @author  lflores
 */
public class Main
{	
	public Main( Properties props )
		throws Exception
	{
		HelperManager.setProperties( props );
		System.out.println( "BEGIN: " + new java.util.Date() );
		long start = System.currentTimeMillis();
		
                JDBCConnection.debug = "true".equalsIgnoreCase(props.getProperty( Constants.DEBUG ) );
		if( "true".equalsIgnoreCase(props.getProperty( Constants.ANALYSE ) ) )
		{
			System.out.println( "Analysing" );
			Analyser a = new Analyser( props );
			a.cloneDB();
		}
		if( "true".equalsIgnoreCase(props.getProperty( Constants.TRANSFER ) ) )
		{
                    if( !"true".equalsIgnoreCase( props.getProperty( Constants.TRANSFER_CHECK_DEPS ) ) )
                    {
			String s = props.getProperty( Constants.TRANSFER_THREADS );
			try
			{
				int i = Integer.parseInt( s );
				AsyncStatement.PARALLEL_THREADS = i;
			}
			catch( Exception ex )
			{
			}
			System.out.println( "Transfering" );
			Mover m = new Mover( props );
			try
			{
				m.moveDB();
			}
			catch( SQLException ex )
			{
				ex.printStackTrace( System.out );
				ex.printStackTrace();
//				ErrorLogger.logException( ex );
				throw ex.getNextException();
			}
                    }
		}
		if( "true".equalsIgnoreCase( props.getProperty( Constants.CONSTRAIN ) ) )
		{
			System.out.println( "Constraining" );
			Constrainer c = new Constrainer( props );
			c.constrainDB();
		}
		if( "true".equalsIgnoreCase( props.getProperty( Constants.DIFF ) ) )
		{
			System.out.println( "Diffing" );
			Diff d = new Diff( props );
			d.diffDb();
		}
			System.out.println( "END: " + new Date() );
			System.out.println( "Transfer took: " + ( System.currentTimeMillis() - start ) / 1000 + " seconds" );
	}
	
	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args)
	{
		if( args.length != 1 )
		{
			System.err.println( "Usage: " + Main.class.getName() + " <props.file>" );
			System.exit( 1 );
		}
		else
		{
			try
			{
				System.out.println( "Loading props: " + args[ 0 ] );
				Properties p = new Properties();
				p.load( new FileInputStream( args[ 0 ] ) );
				p.list( System.out );
				new Main( p );
			}
			catch( Throwable th )
			{
				th.printStackTrace();
				th.printStackTrace( System.out );
				try
				{
					Thread.sleep( 1500 );
				}
				catch( InterruptedException ex )
				{
				}
				System.exit( 2 );				
			}
			System.exit( 0 );
		}
	}
	
}
