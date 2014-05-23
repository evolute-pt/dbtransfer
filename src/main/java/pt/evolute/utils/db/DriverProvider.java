package pt.evolute.utils.db;

import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DriverProvider
{
	private final static Map<String, String> drivers = new HashMap<String, String>();
	private final static Map<String, String> nameRegex = new HashMap<String, String>();

	private static boolean driverManagerSilent = false;
	
	static
	{
		drivers.put("firebirdsql", "org.firebirdsql.jdbc.FBDriver");
		drivers.put("hsqldb", "org.hsqldb.jdbcDriver");
		drivers.put("informix-sqli", "com.informix.jdbc.IfxDriver");
		drivers.put("jackcess", "org.tiyuk.jackcessjdbc.JackcessDriver");
		drivers.put("mysql", "com.mysql.jdbc.Driver");
		drivers.put("odbc", "sun.jdbc.odbc.JdbcOdbcDriver");
		drivers.put("oracle", "oracle.jdbc.OracleDriver"); // oracle:thin
		drivers.put("postgresql", "org.postgresql.Driver");
		drivers.put("rmi", "org.objectweb.rmijdbc.Driver");
		drivers.put("sqlserver", "com.microsoft.sqlserver.jdbc.SQLServerDriver");
		drivers.put("db2", "com.ibm.db2.jcc.DB2Driver");
		drivers.put("as400", "com.ibm.as400.access.AS400JDBCDriver");
		
		//name parser
		nameRegex.put("org.postgresql.Driver", "(/+[a-zA-Z0-9_-]+[?])");
	}
	
	public static String getDatabaseTypeFromUrl( String url )
	{
		StringTokenizer st = new StringTokenizer(url, ":", false);
		st.nextToken();
		return(st.nextToken());
	}
	
	public static boolean isDriverManagerSilent()
	{
		return driverManagerSilent;
	}
	
	public static void setDriverManagerSilent(boolean driverManagerSilent)
	{
		DriverProvider.driverManagerSilent = driverManagerSilent;
	}
	
	public static boolean loadDriverForURL(String url)
	{
		boolean result = false;
		try
		{
			Class.forName( getDriverForURL( url ) );
			result = true;
		}
		catch (ClassNotFoundException ex)
		{
			System.err.println("Couldn't find driver for URL: " + url + " class: " + getDriverForURL( url ) );
			System.out.println("Couldn't find driver for URL: " + url + " class: " + getDriverForURL( url ) );
		}
		catch( ExceptionInInitializerError ex )
		{
			System.err.println("Couldn't find driver for URL: " + url + " class: " + getDriverForURL( url ) );
			System.out.println("Couldn't find driver for URL: " + url + " class: " + getDriverForURL( url ) );
		}
		
		if( isDriverManagerSilent() )
		{
			DriverManager.setLogWriter( null );
		}
		
		return result;
	}

	public static String getDriverForURL(String url)
	{
		StringTokenizer st = new StringTokenizer(url, ":", false);
//		if (st.hasMoreTokens())
//		{
			st.nextToken();
//		}
		String driver = null;
//		if (st.hasMoreTokens())
//		{
			driver = drivers.get(st.nextToken());
//			driver = st.nextToken();
			if (driver == null)
			{
				System.err.println("Couldn't find driver for URL: " + url);
				System.out.println("Couldn't find driver for URL: " + url);
			}
//		}
		return driver;
	}

	public static String getDataBaseNameFromUrl( String url ) throws Exception
	{
		String result = null;
		
		String driver = getDriverForURL( url );
		Pattern p = Pattern.compile( nameRegex.get( driver ) );
		Matcher m = p.matcher( url );
		if( m.find() )
		{
			result = url.substring( m.start()+1, m.end()-1 );
		}
		else
		{
			throw new Exception( "No Match found" );
		}
		return result;
	}
}
