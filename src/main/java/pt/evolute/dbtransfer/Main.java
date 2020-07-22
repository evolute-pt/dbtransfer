package pt.evolute.dbtransfer;

import java.io.FileInputStream;
import java.sql.SQLException;
import java.util.Date;
import java.util.Properties;

import pt.evolute.dbtransfer.analyse.Analyser;
import pt.evolute.dbtransfer.constrain.Constrainer;
import pt.evolute.dbtransfer.db.beans.ConnectionDefinitionBean;
import pt.evolute.dbtransfer.db.helper.HelperManager;
import pt.evolute.dbtransfer.db.jdbc.JDBCConnection;
import pt.evolute.dbtransfer.diff.Diff;
import pt.evolute.dbtransfer.transfer.AsyncStatement;
import pt.evolute.dbtransfer.transfer.Mover;

/**
 *
 * @author  lflores
 */
public class Main
{	
	public Main()
		throws Exception
	{
		System.out.println( "BEGIN: " + new Date() );
		long start = System.currentTimeMillis();
		
                ConnectionDefinitionBean srcBean = ConnectionDefinitionBean.loadBean( HelperManager.getProperties(), Constants.SOURCE_PROPS );
                ConnectionDefinitionBean dstBean = ConnectionDefinitionBean.loadBean( HelperManager.getProperties(), Constants.DESTINATION_PROPS );
                
                JDBCConnection.debug = Config.debug();
		if( Config.analyse() )
		{
			System.out.println( "Analysing" );
			Analyser a = new Analyser( srcBean, dstBean );
			a.cloneDB();
		}
		if( Config.transfer() )
		{
                    if( !Config.checkDependencies() )
                    {
                    	AsyncStatement.PARALLEL_THREADS = Config.getParallelThreads();
			        }
                    System.out.println( "Transfering" );
                    Mover m = new Mover( srcBean, dstBean );
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
		if( Config.constrain() )
		{
			System.out.println( "Constraining" );
			Constrainer c = new Constrainer(HelperManager.getProperties(), srcBean, dstBean );
			c.constrainDB();
		}
		if( Config.diff() )
		{
			System.out.println( "Diffing" );
			Diff d = new Diff( srcBean, dstBean );
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
				Config.setProperties( p );
				HelperManager.setProperties( p );
				new Main();
			}
			catch( Throwable th )
			{
				th.printStackTrace();
				th.printStackTrace( System.out );
				try
				{
					Thread.sleep( 500 );
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
