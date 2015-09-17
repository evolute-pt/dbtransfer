/*
 * Connector.java
 *
 * Created on February 7, 2005, 12:19 AM
 */

package pt.evolute.utils.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import pt.evolute.utils.string.StringPlainer;

/**
 *
 * @author  lflores
 */
public class Connector
{
	private static final String MYSQL = "mysql";
	
	private static final Map<String,String> SCHEMAS = new HashMap<String,String>();
	
	static
	{
		SCHEMAS.put( "postgresql", "public" );
		SCHEMAS.put( "informix-sqli", "informix" );
		SCHEMAS.put( "hsqldb", "PUBLIC" );
		SCHEMAS.put( "sqlserver", "dbo" );
	}
        
	public static Connection getConnection( String url, String user, String passwd )
		throws SQLException
	{
		if( DriverProvider.loadDriverForURL( url ) )
		{
//			System.out.println( "Driver recognized - " 
//					+ DriverManager.getDriver(url).getMajorVersion() + "/"
//					+ DriverManager.getDriver(url).getMinorVersion() );
		}
		else
		{
			System.out.println( "Driver NOT recognized: " + url );
		}
		Connection con = DriverManager.getConnection( url, user, passwd );
//		System.out.println( "DriverName: " + con.getMetaData().getDriverName()
//							+ " DriverVersion: " + con.getMetaData().getDriverVersion() );
//		System.out.println( "AutoComit: " + con.getAutoCommit() );
//		System.out.println( "Catalog: " + getCatalog( url ) + " Schema: " + getSchema( url ) );
		con.setAutoCommit( true );
		return con;
	}
	
	public static String getSchema( String url )
	{
		/*StringTokenizer st = new StringTokenizer( url, ":", false );
		st.nextToken();*/
		String db = url.split( ":" )[ 1 ];
		String result = null;
		if( SCHEMAS.containsKey( db ) )
		{
			result = SCHEMAS.get( db );
		}
		else
		{
			if( MYSQL.equals( db ) )
			{
//				String dbNameAndProps = url.split( "/" )[ 3 ];
//				result = dbNameAndProps.split( "[?]" )[ 0 ];
				result = null;
			}
		}
		return result;
	}
	
	public static String getCatalog( String url )
	{
		if( url.indexOf( "odbc" ) != -1 )
		{
			return null;
		}
		else if( url.indexOf( "sqlserver" ) != -1 )
		{
			String s[] = url.split( "[/;]" );
			for( int n = 1; n < s.length; n++ )
			{
				if( s[ n ].indexOf( "databaseName" ) == 0 )
				{
					return s[ n ].split( "=" )[ 1 ];
				}
			}
			return null;
		}
		else
		{
			String s[] = url.split( "/" );
			String catalog = s[ s.length - 1 ].split( "\\?" )[ 0 ];
			return catalog.split( ":" )[ 0 ];
		}
	}
	
	public static String fixName( String name )
	{
		String orig = name;
/*            if( name.startsWith( "expr" ) )
			{
				System.out.println( "ERROR: " + orig + " - " + name );
				new Exception().printStackTrace();
			}*/
		try
		{
			name = "n" + new Integer( name );
		}
		catch( NumberFormatException ex )
		{
		}
		name = name.replace( '-', '_' );
		name = name.replaceAll( " ", "" );
		name = StringPlainer.convertString( name );
		
		if( name.startsWith( "expr" ) )
		{
			new Exception( "ERROR: " + orig + " - " + name ).printStackTrace();
		}
		
		return name;
	}
}
