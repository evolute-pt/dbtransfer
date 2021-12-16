package pt.evolute.dbtransfer.db;

import pt.evolute.dbtransfer.db.beans.ConnectionDefinitionBean;
import pt.evolute.dbtransfer.db.dummy.DummyConnection;
import pt.evolute.dbtransfer.db.jackcess.JackcessConnection;
import pt.evolute.dbtransfer.db.jdbc.JDBCConnection;

/**
 *
 * @author lflores
 */
public class DBConnector
{
    public static DBConnection getConnection( ConnectionDefinitionBean bean, boolean onlyNotEmpty )
            throws Exception
    {
        return getConnection( bean.getUrl(), bean.getUser(), bean.getPassword(), onlyNotEmpty );
    }
    
	public static DBConnection getConnection( String url, String usr, String pass, boolean onlyNotEmpty )
			throws Exception
	{
		DBConnection con = null;
		if( url.startsWith( "jdbc:" ) )
		{
			con = new JDBCConnection( url, usr, pass, onlyNotEmpty );
		}
		else if( url.startsWith( "jackcess:" ) )
		{
			con = new JackcessConnection( url, usr, pass, onlyNotEmpty );
		}
		else if( url.startsWith( "dummy:" ) )
		{
			con = new DummyConnection( url, usr, pass, onlyNotEmpty );
		}
		if( con == null )
		{
			System.out.println( "Couldn't get connection for URL: " + url );
		}
		return con;
	}
}
