package pt.evolute.utils.sql.backend;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;


public class HSQLDBBackend extends DefaultBackend
{
	// must drop 1.8 support (used in querbio)
//	private static final String INIT_QUERY[] = new String[]{ "SET DATABASE DEFAULT TABLE TYPE CACHED" };
	
	public HSQLDBBackend()
	{
	}
	
/*	@Override
	public String[] getInitQuery()
	{
		return INIT_QUERY;
	}
	*/
	
	@Override
	public boolean supportsReturnGeneratedKeys()
	{
		return false;
	}
	
	@Override
	public String portSyntax( CharSequence sql )
	{
		String query = sql.toString();
		if( query != null && !query.isEmpty() )
		{
			query = query.replaceAll( "(?i)serial", "identity" );
		}
		return query;
	}
	
	@Override
	public boolean isValid(Connection con)
		throws SQLException
	
	{
		Statement stm = con.createStatement();
		stm.execute( "SELECT COUNT(*) FROM INFORMATION_SCHEMA.SYSTEM_TABLES" );
		stm.close();
		return true;
	}
}
