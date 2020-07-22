package pt.evolute.dbtransfer;

import java.util.Properties;

public class Config implements Constants{
	private static Properties PROPS = new Properties();
	
	public static boolean ignoreEmpty()
	{
		return getValue( ONLY_NOT_EMPTY );
	}

	public static boolean analyse()
	{
		return getValue( ANALYSE );
	}
	
	private static boolean getValue( String name ) {
		return Boolean.parseBoolean( PROPS.getProperty( name, "false" ) );
	}
	
	public static void setProperties(Properties p) {
		PROPS.putAll( p );
	}

	public static boolean debug() {
		return getValue( DEBUG );
	}

	public static boolean transfer() {
		return getValue( TRANSFER );
	}

	public static boolean constrain() {
		return getValue( CONSTRAIN );
	}

	public static boolean diff() {
		return getValue( DIFF );
	}
	
	public static int getParallelThreads()
	{
		int t = 1;
		String s = PROPS.getProperty( TRANSFER_THREADS );
		if( s != null && !s.isEmpty() )
		{
			try
			{
				t = Integer.parseInt( s );
			}
			catch( NumberFormatException ex )
			{
				System.err.println( "Error in property: TRANSFER_THREADS=" + s );
			}
		}
		return t; 
	}

	public static boolean checkDependencies() {
		return getValue( TRANSFER_CHECK_DEPS );
	}

	public static String getDiffComment() {
		return PROPS.getProperty( DIFF_COMMENT );
	}

	public static boolean escapeUnicode() {
		return getValue( TRANSFER_ESCAPE_UNICODE );
	}
}
