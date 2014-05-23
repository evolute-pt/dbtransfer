package pt.evolute.utils.sql.backend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.evolute.utils.error.ErrorLogger;

public class BackendProvider
{
	private static final Class<? extends Backend> DEFAULT_BACKEND_CLASS = DefaultBackend.class; 
	
	private static final Map<String,Class<? extends Backend>> BACKEND_CLASS_BY_PROTOCOL = new HashMap<String,Class<? extends Backend>>();
	
	private static final Map<Class<? extends Backend>,Boolean> BACKEND_ESCAPES_UNICODE_BY_CLASS = new HashMap<Class<? extends Backend>, Boolean>();

	static
	{
		BACKEND_CLASS_BY_PROTOCOL.put( "postgresql", PostgreSQLBackend.class );
		BACKEND_CLASS_BY_PROTOCOL.put( "sqlserver", SQLServerBackend.class );
		BACKEND_CLASS_BY_PROTOCOL.put( "hsqldb", HSQLDBBackend.class );
	}

	private static Map<String,Backend> BACKEND_BY_URL = new HashMap<String,Backend>();
	private static Map<Class<? extends Backend>,List<Backend>> BACKENDS_BY_CLASS = new HashMap<Class<? extends Backend>,List<Backend>>();
	
	public static Backend getBackend( String url )
	{
		Backend backend = BACKEND_BY_URL.get( url );
		if( backend == null )
		{
			String protocol = url.split( ":" )[ 1 ];
			Class<? extends Backend> backendClass = BACKEND_CLASS_BY_PROTOCOL.get( protocol );
			if( backendClass == null )
			{
				System.out.println( "Using default db backend for protocol / url: " + protocol + " / " + url );
				backendClass = DEFAULT_BACKEND_CLASS;
			}
			try
			{
				backend = backendClass.newInstance();
				if( BACKEND_ESCAPES_UNICODE_BY_CLASS.containsKey( backendClass ) )
				{
					backend.setEscapeUnicode( BACKEND_ESCAPES_UNICODE_BY_CLASS.get( backendClass ) );
				}
				BACKEND_BY_URL.put( url, backend );
				if( !BACKENDS_BY_CLASS.containsKey(backendClass) )
				{
					BACKENDS_BY_CLASS.put(backendClass, new ArrayList<Backend>() );
				}
				BACKENDS_BY_CLASS.get( backendClass ).add( backend );
			}
			catch( IllegalAccessException ex )
			{
				ErrorLogger.logException( ex );
			} 
			catch( InstantiationException ex )
			{
				ErrorLogger.logException( ex );
			}
		}
		return backend;
	}
	
	public static Backend getDefaultBackend()
	{
		Backend backend = null;
		try
		{
			backend = DEFAULT_BACKEND_CLASS.newInstance();
		}
		catch( IllegalAccessException ex )
		{
			ErrorLogger.logException( ex );
		} 
		catch( InstantiationException ex )
		{
			ErrorLogger.logException( ex );
		}
		return backend;
	}
	
	public static void setBackendEscapesUnicodeByClass( Class<? extends Backend> clazz, Boolean escapes )
	{
		BACKEND_ESCAPES_UNICODE_BY_CLASS.put( clazz, escapes );
		List<Backend> backends = BACKENDS_BY_CLASS.get( clazz );
		if( backends != null )
		{
			for( Backend backend : backends )
			{
				backend.setEscapeUnicode( escapes );
			}
		}
	}
}
