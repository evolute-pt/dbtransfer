package pt.evolute.dbtransfer.db.helper;

import java.util.Properties;

public class HelperManager 
{
	private static Properties properties = null;
	
	public static Helper getTranslator( String url )
	{
		Helper tr = null;
		if( url.startsWith( "jdbc:sqlserver:" ) )
		{
			System.out.println( "SQL Server translator" );
			tr = SQLServerHelper.getTranslator();
		}
		else if( url.startsWith( "jdbc:postgresql:" ) )
		{
			System.out.println( "PostgreSQL translator" );
			tr = PostgreSQLServerHelper.getTranslator();
		}
		else if( url.startsWith( "jdbc:mysql:" ) )
		{
			System.out.println( "MySQL translator" );
			tr = MySQLServerHelper.getTranslator();
		}
                else if( url.startsWith( "jdbc:oracle:" ) )
		{
			System.out.println( "Oracle translator" );
			tr = OracleServerHelper.getTranslator();
		}
		if( tr == null )
		{
			System.out.println( "Default translator" );
			tr = NullHelper.getTranslator();
		}
		return tr;
	}

	public static void setProperties( Properties props )
	{
		properties = props;
	}
	
	public static Properties getProperties() 
	{
		return properties;
	}
}
