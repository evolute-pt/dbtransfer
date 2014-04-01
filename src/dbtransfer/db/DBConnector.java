/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dbtransfer.db;

import dbtransfer.db.jackcess.JackcessConnection;
import dbtransfer.db.jdbc.JDBCConnection;

/**
 *
 * @author lflores
 */
public class DBConnector
{
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
		return con;
	}
}
