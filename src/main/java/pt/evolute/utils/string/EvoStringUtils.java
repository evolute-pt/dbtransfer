/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pt.evolute.utils.string;

import java.util.Locale;

/**
 *
 * @author lflores
 */
public class EvoStringUtils
{

	public static String parsePlica( CharSequence str )
	{
		String result = str.toString();
		if ( result != null )
		{
			result = result.replaceAll( "'", "\\\\'" );
		}
		return result;
	}

	public static String parsePlica( CharSequence str, String plicaEscaped )
	{
		String result = str.toString();
		if ( result != null )
		{
			result = result.replaceAll( "'", plicaEscaped );
		}
		return result;
	}

	public static String trimChar( String str, char ch )
	{
		return trimChar( str, ch, true, true );
	}

	public static String trimChar( String str, char ch, boolean left, boolean right )
	{
		String out = str;
		if( left )
		{
			int i = 0;
			while( out.charAt( i ) == ch )
			{
				++i;
			}
			if( i > 0 )
			{
				out = str.substring( i );
			}
		}
		if( right )
		{
			int i = out.length() - 1;
			while( out.charAt( i ) == ch )
			{
				--i;
			}
			if( i < out.length() - 1 )
			{
				out = out.substring( 0, i );
			}
		}
		return out;
	}
	
	/**
	 *removes the first occurrence of removeThis after afterThis (NOT case sensitive)
	 * 
	 */
	public static String removeAfter( String source, String removeThis, String afterThis )
	{
		String ret = source;
		int pos = source.toLowerCase(Locale.ENGLISH).indexOf( afterThis.toLowerCase(Locale.ENGLISH) );
		if( pos != -1 )
		{
			pos = source.indexOf( removeThis, pos );
			if( pos != -1 )
			{
				ret= source.substring( 0, pos ) + source.substring( pos + removeThis.length(), source.length() );
			}
		}
		return ret;
	}
	
	/**
	 *removes the first occurrence of removeThis after afterThis (case sensitive)
	 * 
	 */
	public static String removeAfterSensitive( String source, String removeThis, String afterThis )
	{
		String ret = source;
		int pos = source.indexOf( afterThis );
		if( pos != -1 )
		{
			pos = source.indexOf( removeThis, pos );
			ret= source.substring( 0, pos ) + source.substring( pos + removeThis.length(), source.length() );
		}
		return ret;
	}
	
	public static boolean isNum( String target )
	{
		boolean ret = true;
		for( int i = 0 ; i < target.length(); i++ )
		{
			char x = target.charAt( i );
			if( x != '.' && x != ',' && ( x < '0' || x > '9' ) )
			{
				ret = false;
				break;
			}
		}
		return ret;
	}




   	private static int findFirstDigitBackwards( String str )
	{
		int index = -1;
		if ( str != null )
		{
			if ( Character.isDigit( str.charAt( str.length() - 1 ) ) )
			{
				for ( int i = str.length() - 1; i >= 0; i-- )
				{
					char charAtIndex = str.charAt( i );
					if ( Character.isDigit( charAtIndex ) )
					{
//						index = ( i == str.length() - 1 ) ? i : i + 1;
						index = i;
					}
					else
					{
						break;
					}
				}
			}
		}
		return index;
	}

	private static String incrementAsLong( String str )
	{
		String result = null;
		if ( str != null )
		{
			try
			{
				Long asInteger = Long.parseLong( str );
				Long incremented = asInteger + 1;
				result = incremented.toString();
			}
			catch ( Exception e )
			{
				result = null;
			}
		}
		return result;
	}

	private static String increment( char[] str )
	{
	    for( int pos = str.length - 1; pos >= 0; pos-- )
	    {
	        if ( Character.toUpperCase( str[ pos ] ) != 'Z' )
	        {
	        	if ( str[ pos ] == '9' )
	        	{
	        		String word = new String( str );
	        		String begin = word.substring( 0, pos + 1 );
	        		String rest = word.substring( pos + 1, word.length() );

	        		int index = findFirstDigitBackwards( begin );
	        		String left = begin.substring( 0, index );
	        		String number = begin.substring( index, begin.length() );

	        		return left + incrementAsLong( number ) + rest;
	        	}
	        	else
	        	{
	        		str[ pos ]++;
	        		break;
	        	}
	        }
	        else
	        {
	            str[ pos ] = 'a';
	        }
	    }
	    return new String( str );
	}

	private static boolean isAllZs( String str )
	{
		boolean result = false;
		if ( str != null )
		{
			StringBuilder builder = new StringBuilder( str.length() );
			for ( int i = 0; i < str.length(); i++ )
			{
				builder.append( "Z" );
			}
			result = str.equals( builder.toString() );
		}
		return result;
	}

	public static String increment( String str )
	{
		String result = null;
		if ( str != null )
		{
			// try integer
			result = incrementAsLong( str );

			// try string + integer
			if ( result == null )
			{
				int index = findFirstDigitBackwards( str );
				if ( index > -1 )
				{
					String left = str.substring( 0, index );
					String right = str.substring( index, str.length() );
					result = left + incrementAsLong( right );
				}
			}

			// increment chars
			if ( result == null )
			{
				result = increment( str.toCharArray() );
				if ( isAllZs( str ) )
				{
					result = "1" + result;
				}
			}
		}
		return result;
	}




	private static void test( String str )
	{
		System.out.println( "Test case : " + str );
		System.out.println( "\tString incremented : " + increment( str ) );
//		System.out.println( " " );
	}

	public static void main( String ... args )
	{
		test( "9" );
		test( "Z" );
		test( "9Z" );
		test( "10a" );
		test( "Z9" );
		test( "0Z" );
//		System.out.println( "original : " + incrementOriginal( toIncrement.toCharArray() ) );
	}

}
