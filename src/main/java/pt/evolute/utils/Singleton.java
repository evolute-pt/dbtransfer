package pt.evolute.utils;

import java.util.HashMap;
import java.util.Map;

import pt.evolute.utils.error.ErrorLogger;

public class Singleton
{
	public static final String USERNAME = "USERNAME";
	public static final String USER_ID = "USER_ID";
	public static final String USERNAME_FULL = "USERNAME_FULL";
	public static final String PASSWORD = "PASSWORD";
	public static final String TRACKER = "TRACKER";
	public static final String DEFAULT_DBMANAGER = "DEFAULT_DBMANAGER";
	public static final String DEFAULT_PERSISTENCE_MANAGER_FACTORY = "DEFAULT_PERSISTENCE_MANAGER_FACTORY";
	public static final String DEFAULT_JDO_PROVIDER = "DEFAULT_JDO_PROVIDER";
	public static final String DEFAULT_OBJECT_PROVIDER = "DEFAULT_OBJECT_PROVIDER";
	public static final String DEFAULT_EVO_DATA_PROVIDER = "DEFAULT_EVO_DATA_PROVIDER";
	public static final String TODAY = "TODAY";
	public static final String DEFAULT_DATABASE_TYPE = "DEFAULT_DATABASE_TYPE";
	public static final String PROPERTIES = "PROPERTIES";
	public static final String PROPERTIES_PATH = "PROPERTIES_PATH";
	public static final String DATABASE_NAME = "db.name";
	
	
	/* End of names */
	private static final Map<String, Object> hash = new HashMap<String, Object>();
	private static final Map<String, Boolean> locked = new HashMap<String, Boolean>();
	
	public static Object setInstance( String name, Object obj )
	{
		if( Boolean.TRUE.equals( locked.get( name ) ) )
		{
			ErrorLogger.logException( new Exception( "Tried to change locked property: " + name + "(old: " + getInstance( name ) + " new: " + obj + ")" ) );
			throw new RuntimeException( "Tried to change locked property: " + name + "(old: " + getInstance( name ) + " new: " + obj + ")" );
		}
		else
		{
			if( obj == null )
			{
				obj = getInstance( name );
				hash.remove( name );
				return obj;
			}
			else
			{
				return hash.put( name, obj );
			}
		}
	}
	
	public static Object getInstance( String name )
	{
		return hash.get( name );
	}
	
	public static void clear()
	{
		hash.clear();
	}
	
	public static void lock( String name )
	{
		locked.put( name, Boolean.TRUE );
	}
	
	public static void unlock( String name )
	{
		locked.put( name, Boolean.FALSE );
	}
}
