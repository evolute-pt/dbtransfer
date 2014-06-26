package pt.evolute.dbtransfer.db;

import pt.evolute.dbtransfer.db.beans.ConnectionDefinitionBean;
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
        return getConnection( bean.getUrl(), bean.getUser(), bean.getPassword(), onlyNotEmpty, bean.getSchema() );
    }
    
	public static DBConnection getConnection( String url, String usr, String pass, boolean onlyNotEmpty, String schema )
			throws Exception
	{
		DBConnection con = null;
		if( url.startsWith( "jdbc:" ) )
		{
			con = new JDBCConnection( url, usr, pass, onlyNotEmpty, schema );
		}
		else if( url.startsWith( "jackcess:" ) )
		{
			con = new JackcessConnection( url, usr, pass, onlyNotEmpty );
		}
		return con;
	}
}
