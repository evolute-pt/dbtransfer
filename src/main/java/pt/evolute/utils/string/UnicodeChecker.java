package pt.evolute.utils.string;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class UnicodeChecker
{
	private static final int TAB = 9; 
	
	private static final Pattern PREFIX_0300 = Pattern.compile( "\\\\u03" );
	
	private static final Pattern BACKSLASH_ESCAPED_BACKSLASH_CHAR = Pattern.compile( "(\\\\u005c)+\\\\u00" );
	private static final Pattern BACKSLASH_ESCAPED_BACKSLASH = Pattern.compile( "(\\\\\\\\u005c)+" );
	private static final Pattern BACKSLASH_ESCAPED_UNICODE = Pattern.compile( "(\\\\\\\\u00)+" );
//	private static final Pattern DOUBLE_BACKSLASH_ESCAPED_BACKSLASH = Pattern.compile( "(\\\\\\\\u005c){3,}+" );
	
	private static final HashMap<Pattern,String> UNICODE_0300_0080 = new HashMap<Pattern,String>();
	private static final HashMap< String, String > UNICODE_0100 = new HashMap< String, String >();
	private static final HashMap< String, String > UNICODE_LATIN_1_SUPPLEMENT = new HashMap< String, String >();
	private static final HashMap< String, String > UNICODE_0200 = new HashMap< String, String >();
	private static final HashMap< String, String > UNICODE_2000 = new HashMap< String, String >();
	private static final HashMap< String, String > UNICODE_SPECIALS = new HashMap< String, String >();
	
	public static final int MAX_SUBSTITUTION_CHARACTERS = 6;
	public static final char UNICODE_CHARACTER_START = '\\';
	
	private static boolean parsePlica = false;
	private static boolean useDoubleSlash = false;
	
	static
	{
//		System.out.println( "\n\n  UNICODE \n" );
		
		UNICODE_0300_0080.put( Pattern.compile( " \\\\u0301" ), "\u00b4" );
		UNICODE_0300_0080.put( Pattern.compile( "\\\\u0301 " ), "\u00b4" );
		UNICODE_0300_0080.put( Pattern.compile( "\\\\u0301" ), "\u00b4" );
		UNICODE_0300_0080.put( Pattern.compile( " \\\\u0303" ), "&#771;" );
		UNICODE_0300_0080.put( Pattern.compile( "\\\\u0303 " ), "&#771;" );
		UNICODE_0300_0080.put( Pattern.compile( "\\\\u0303" ), "&#771;" );
		
		UNICODE_0300_0080.put( Pattern.compile( "\\\\u03b1" ), "&#945;" );
		UNICODE_0300_0080.put( Pattern.compile( "\\\\u03b2" ), "&#946;" );
		
		// a
		UNICODE_0300_0080.put( Pattern.compile( "a\\\\u0300" ), "\u00e0" );
		UNICODE_0300_0080.put( Pattern.compile( "a\\\\u0301" ), "\u00e1" );
		UNICODE_0300_0080.put( Pattern.compile( "a\\\\u0302" ), "\u00e2" );
		UNICODE_0300_0080.put( Pattern.compile( "a\\\\u0303" ), "\u00e3" );
		UNICODE_0300_0080.put( Pattern.compile( "a\\\\u0308" ), "\u00e4" );
		// A
		UNICODE_0300_0080.put( Pattern.compile( "A\\\\u0300" ), "\u00c0" );
		UNICODE_0300_0080.put( Pattern.compile( "A\\\\u0301" ), "\u00c1" );
		UNICODE_0300_0080.put( Pattern.compile( "A\\\\u0302" ), "\u00c2" );
		UNICODE_0300_0080.put( Pattern.compile( "A\\\\u0303" ), "\u00c3" );
		UNICODE_0300_0080.put( Pattern.compile( "A\\\\u0308" ), "\u00c4" );
		// e
		UNICODE_0300_0080.put( Pattern.compile( "e\\\\u0300" ), "\u00e8" );
		UNICODE_0300_0080.put( Pattern.compile( "e\\\\u0301" ), "\u00e9" );
		UNICODE_0300_0080.put( Pattern.compile( "e\\\\u0302" ), "\u00ea" );
//		UNICODE_0300_0080.put( Pattern.compile( "e\\u0303", "\u00eb" );
		UNICODE_0300_0080.put( Pattern.compile( "e\\\\u0308" ), "\u00eb" );
		// E
		UNICODE_0300_0080.put( Pattern.compile( "E\\\\u0300" ), "\u00c8" );
		UNICODE_0300_0080.put( Pattern.compile( "E\\\\u0301" ), "\u00c9" );
		UNICODE_0300_0080.put( Pattern.compile( "E\\\\u0302" ), "\u00ca" );
//		UNICODE_0300_0080.put( Pattern.compile( "e\\u0303", "\u00eb" );
		UNICODE_0300_0080.put( Pattern.compile( "E\\\\u0308" ), "\u00cb" );
		// i
		UNICODE_0300_0080.put( Pattern.compile( "i\\\\u0300" ), "\u00ec" );
		UNICODE_0300_0080.put( Pattern.compile( "i\\\\u0301" ), "\u00ed" );
		UNICODE_0300_0080.put( Pattern.compile( "i\\\\u0302" ), "\u00ee" );
//		UNICODE_0300_0080.put( Pattern.compile( "e\\u0303", "\u00eb" );
		UNICODE_0300_0080.put( Pattern.compile( "i\\\\u0308" ), "\u00ef" );
		// I
		UNICODE_0300_0080.put( Pattern.compile( "I\\\\u0300" ), "\u00cc" );
		UNICODE_0300_0080.put( Pattern.compile( "I\\\\u0301" ), "\u00cd" );
		UNICODE_0300_0080.put( Pattern.compile( "I\\\\u0302" ), "\u00ce" );
//		UNICODE_0300_0080.put( Pattern.compile( "e\\u0303", "\u00eb" );
		UNICODE_0300_0080.put( Pattern.compile( "I\\\\u0308" ), "\u00cf" );
		// o
		UNICODE_0300_0080.put( Pattern.compile( "o\\\\u0300" ), "\u00f2" );
		UNICODE_0300_0080.put( Pattern.compile( "o\\\\u0301" ), "\u00f3" );
		UNICODE_0300_0080.put( Pattern.compile( "o\\\\u0302" ), "\u00f4" );
		UNICODE_0300_0080.put( Pattern.compile( "o\\\\u0303" ), "\u00f5" );
		UNICODE_0300_0080.put( Pattern.compile( "o\\\\u0308" ), "\u00f6" );
		// O
		UNICODE_0300_0080.put( Pattern.compile( "O\\\\u0300" ), "\u00d2" );
		UNICODE_0300_0080.put( Pattern.compile( "O\\\\u0301" ), "\u00d3" );
		UNICODE_0300_0080.put( Pattern.compile( "O\\\\u0302" ), "\u00d4" );
		UNICODE_0300_0080.put( Pattern.compile( "O\\\\u0303" ), "\u00d5" );
		UNICODE_0300_0080.put( Pattern.compile( "O\\\\u0308" ), "\u00d6" );
		// u
		UNICODE_0300_0080.put( Pattern.compile( "u\\\\u0300" ), "\u00f9" );
		UNICODE_0300_0080.put( Pattern.compile( "u\\\\u0301" ), "\u00fa" );
		UNICODE_0300_0080.put( Pattern.compile( "u\\\\u0302" ), "\u00fb" );
//		UNICODE_0300_0080.put( Pattern.compile( "u\\u0303", "\u00f5" );
		UNICODE_0300_0080.put( Pattern.compile( "u\\\\u0308" ), "\u00fc" );
		// U
		UNICODE_0300_0080.put( Pattern.compile( "U\\\\u0300" ), "\u00d9" );
		UNICODE_0300_0080.put( Pattern.compile( "U\\\\u0301" ), "\u00da" );
		UNICODE_0300_0080.put( Pattern.compile( "U\\\\u0302" ), "\u00db" );
//		UNICODE_0300_0080.put( Pattern.compile( "U\\u0303", "\u00d5" );
		UNICODE_0300_0080.put( Pattern.compile( "U\\\\u0308" ), "\u00dc" );
		// c
		UNICODE_0300_0080.put( Pattern.compile( "c\\\\u0327" ), "\u00e7" );
		// C
		UNICODE_0300_0080.put( Pattern.compile( "C\\\\u0327" ), "\u00C7" );
		
		
		/*** 0100 table ( Latin Extended A ) ***/
//		System.out.println( "BUILDING UNICODE 0100 TO HTML ENTITY TABLE ..." );
		
		String prefix_key = "\\\\u01";
		String prefix_value = "&#";
		for ( int i = 256, n = 0, h = 0; i <= 383; i++ )
		{
			String key = prefix_key + n + Integer.toHexString( h++ );
			String value = prefix_value + i;
			
//			System.out.println( "\t" + key + " => " + value );
			UNICODE_0100.put( key, value );
			
			if ( h == 16 )
			{
				h = 0;
				n++;
			}
		}

		
		/*** Latin 1 Supplement ***/
		prefix_key = "\\\\u0";
		prefix_value = "&#";
		for ( int i = 160, d = 0, n = 10, h = 0; i <= 383; i++ )
		{
			String key = prefix_key + d + Integer.toHexString( n ) + Integer.toHexString( h++ );
			String value = prefix_value + i;
			
			UNICODE_LATIN_1_SUPPLEMENT.put( key, value );
			
			if ( h == 16 )
			{
				if ( n == 16 )
				{
					d++;
					n = -1;
				}
				h = 0;
				n++;
			}
		}
		
		
		UNICODE_0200.put( "\\\\u02c6", "&#710;" );
		UNICODE_0200.put( "\\\\u02c7", "&#711;" );
		
		UNICODE_2000.put( "\\\\u2009", "&#8201;" );
		UNICODE_2000.put( "\\\\u2028", "&#8232;" );
		UNICODE_2000.put( "\\\\u201e", "&#8222;" );
		UNICODE_2000.put( "\\\\u2011", "&#8209;" );
		
		UNICODE_SPECIALS.put( "\\\\ufffd", "&#65533;" );
		UNICODE_SPECIALS.put( "\\\\u2044", "&#8260;" );
	}
	
	public static void setUseDoubleSlash( boolean newState )
	{
		useDoubleSlash = newState;
	}
	
	public static boolean getUseDoubleSlash()
	{
		return useDoubleSlash;
	}
	
	public static String parseToUnicode(CharSequence str)
	{
		return parseToUnicode( str, parsePlica );
	}
	
	public static String parseToUnicode(CharSequence str, boolean parsePlica)
	{
		return parseToUnicode( str, parsePlica, useDoubleSlash );
	}
	
	public static String parseToUnicode(CharSequence str, boolean parsePlica, boolean doubleSlash )
	{
		str = fixUnicode0300FromUser( str );
		return parseToUnicode( (doubleSlash?"\\":"")+"\\u", "", str, parsePlica );
	}
	
	public static String parseToUnicode(String unicodePrefix, String unicodeSuffix, CharSequence str, boolean parsePlica)
	{
		return parseToUnicode( unicodePrefix, 4, unicodeSuffix, str, parsePlica );
	}
	
	public static String parseToUnicode(String unicodePrefix, int digits, String unicodeSuffix, CharSequence str,
											boolean parsePlica )
	{
		return parseToUnicode( unicodePrefix, digits, unicodeSuffix, str, parsePlica, null );
	}
	
	public static String parseToUnicode(String unicodePrefix, int digits, String unicodeSuffix, CharSequence str,
											boolean parsePlica, String specialChars)
	{
		HashMap<Integer,Object> specialTable = null;
		if( specialChars != null )
		{
			specialTable = new HashMap<Integer,Object>();
			for( int n = 0; n < specialChars.length(); n++ )
			{
				int cnum = specialChars.charAt( n );
				specialTable.put( new Integer( cnum ), null );
			}
		}
		StringBuilder newStr = new StringBuilder();
		String theChar = new String();
//		String theZeros = new String();
		for(int i=0; i<str.length(); i++)
		{
			// check for non valid characters
			int cnum = str.charAt( i );
			if( cnum < 0)
			{
				throw new RuntimeException("Caracter n\u00e3o v\u00e0lido: <" + str.charAt( i ) + "/" + ( int )str.charAt( i ) + ">" );
			}

			if( cnum == TAB || (
				( specialChars == null || !specialTable.containsKey( new Integer( cnum ) ) ) 
				&& cnum < 127 && cnum >= 32  && cnum != '\\'
				&& cnum != '{' && cnum != '}' && ( !parsePlica || cnum != '\'' ) ) )
			{
				newStr.append( ( char )cnum );
			}
			else
			{
				theChar = Integer.toHexString( cnum );
				newStr.append( unicodePrefix );
				switch( digits - theChar.length())
				{
					case 3:
						newStr.append( "000" );
						break;
					case 2:
						newStr.append( "00" );
						break;
					case 1:
						newStr.append( "0" );
						break;
					case 0:
						break;
					default:
				}
				
				newStr.append( theChar );
				newStr.append( unicodeSuffix );
			}
		}
		return newStr.toString();
	}
	
	public static String parseFromUnicode( String str )
	{
		return parseFromUnicode( str, parsePlica );
	}
	
	public static String parseFromUnicode( String str, boolean parsePlica )
	{
		str = BACKSLASH_ESCAPED_BACKSLASH_CHAR.matcher( str ).replaceAll( "\\\\u00" );
//		System.out.println( "Fix \\: " + BACKSLASH_ESCAPED_BACKSLASH_CHAR.toString() + " -> " +  str );
		
		str = BACKSLASH_ESCAPED_BACKSLASH.matcher( str ).replaceAll( "\\\\" );
//		System.out.println( "Fix \\: " + BACKSLASH_ESCAPED_BACKSLASH.toString() + " -> " +  str );
		
		str = BACKSLASH_ESCAPED_UNICODE.matcher( str ).replaceAll( "\\\\u00" );
//		System.out.println( "Fix \\: " + BACKSLASH_ESCAPED_UNICODE.toString() + " -> " +  str );
		
/*		
		str = DOUBLE_BACKSLASH_ESCAPED_BACKSLASH.matcher( str ).replaceAll( "\\\\" );
		System.out.println( "Fix \\: " + DOUBLE_BACKSLASH_ESCAPED_BACKSLASH.toString() + " -> " +  str );*/
		
//		System.out.println( "str: " + str.replaceAll( , "\\\\" ) );
		
		str = fixUnicode0300FromDB( str );
		/* BEGIN, CHAR_UNICODE, REST */
		String holder[] = new String[] { str, null, null };
		innerParseFromUnicode( holder, parsePlica );
		StringBuilder buff = new StringBuilder( holder[ 0 ] );
		String parsed = holder[ 1 ];
		while( parsed != null )
		{
			buff.append( parsed );
			holder[ 0 ] = holder[ 2 ];
			innerParseFromUnicode( holder, parsePlica );
			parsed = holder[ 1 ];
			buff.append( holder[ 0 ] );
		}
		return buff.toString();
	}

	private static void innerParseFromUnicode(String holder[], boolean parsePlica)
	{
		String str = holder[ 0 ];
		holder[ 1 ] = null;
		if( str == null || str.length() < 6 )
		{
			return;
		}
		String	token	= "\\u";
		String	word	= new String();
		String	end	= new String();

		int	index	= 0;
		char value	= 0;

		index = str.indexOf( token );
		if( index > -1 && ( index + 6 <= str.length() ) )
		{
			// check if is unicode
			holder[ 0 ] = str.substring(0, index );
			word = str.substring(index + 2, index + 6);

			if( index + 6 <= str.length() )
			{
				end = str.substring( index + 6, str.length() );
			}
			for( int i = 0; i < word.length(); ++i )
			{
				if ( Character.digit(word.charAt(i), 16) < 0 )
				{
					holder[ 1 ] = token;
					holder[ 2 ] = str.substring( index + 2, str.length() );
					return;
				}
			}
			value = ( char )Integer.parseInt( word, 16 );

			holder[ 1 ] = "" + value;
			if( end.length() >= 6 && value != '\\' )
			{
				end = end.replaceAll( "[\\\\]u" + word , "" + value );
			}
			holder[ 2 ] = end;
		}
	}
	
	public static String convertUnicode0100ToHTMLFromUser( String source )
	{
		return convertUnicodeFromUser( source, UNICODE_0100 );
	}
	
	public static String convertUnicodeLatin1ExtendedToHTMLFromUser( String source )
	{
		return convertUnicodeFromUser( source, UNICODE_LATIN_1_SUPPLEMENT );
	}
	
	public static String convertUnicode0200ToHTMLFromUser( String source )
	{
		return convertUnicodeFromUser( source, UNICODE_0200 );
	}
	
	public static String convertUnicode2000ToHTMLFromUser( String source )
	{
		return convertUnicodeFromUser( source, UNICODE_2000 );
	}
	
	public static String convertUnicodeSpecialsToHTMLFromUser( String source )
	{
		return convertUnicodeFromUser( source, UNICODE_SPECIALS );
	}
	
	private static String convertUnicodeFromUser( String source, Map< String, String > map )
	{
		String dest = source;
		for ( Map.Entry< String, String > entry : map.entrySet() )
		{
			String key = entry.getKey();
			String value = entry.getValue();
			key = "" + key.charAt( 0 ) + key.substring( 2 );
			dest = dest.replaceAll( key, value );
		}
		return dest;
	}
	
	public static String fixUnicode0300FromDB( String source )
	{
		String dest = source;
		if( PREFIX_0300.matcher( dest ).matches() )
		{
			for( Map.Entry<Pattern,String> entry: UNICODE_0300_0080.entrySet() )
			{
				dest = entry.getKey().matcher( dest ).replaceAll( entry.getValue() );
//				dest = dest.replaceAll( entry.getKey(), entry.getValue() );
				
				if( !PREFIX_0300.matcher( dest ).matches() )
				//if( dest.contains(prefix ) == -1 )
				{
					break;
				}
			}
		}
		
		return dest;
	}
	
	public static String fixUnicode0300FromUser( CharSequence source )
	{
		String dest = source.toString();
		if( source != null )
		{
			for( Map.Entry<Pattern,String> entry: UNICODE_0300_0080.entrySet() )
			{
				String key0300 = entry.getKey().pattern();
				String value0080 = entry.getValue();
				key0300 = "" + key0300.charAt( 0 ) + key0300.substring( 2 );
				dest = dest.replaceAll( key0300, value0080 );
			}
		}
		return dest;
	}
	
	
	
	public static void main( String args[] ) throws Exception
	{
//		String source = "ol\u00e1 : \u02c6 - \u02c7 - \u0301 - a\u0303 - \u03b1 - \u03b2 - \u2009 - \u2028 - \u201e - \ufffd .";
		String source = "Amoxicilina e \\\\u00c1cido Clavul\\\\u00e2nico, 2000mg e 200mg, P\\\\u00f3 Para Solu\\\\u00e7\\\\u00e3o Para Perfus\\\\u00e3o";
//		String source = "amoxicilina e \\\\u005c\\\\u005cacido clavul\\\\u005c\\u005canico, 2000mg e 200mg, p\\\\u005c\\\\u005co para solu\\\\u005c\\\\u005cc\\\\u005c\\\\u005cao para perfus\\\\u005c\\\\u005cao";
		
		System.out.println( "Source : " + source );
		
/*		source = convertUnicode0100ToHTMLFromUser( source );
		source = convertUnicode0200ToHTMLFromUser( source );
		source = convertUnicode2000ToHTMLFromUser( source );
		source = convertUnicodeLatin1ExtendedToHTMLFromUser( source );
		source = convertUnicodeSpecialsToHTMLFromUser( source );
	*/	
		
		String result = parseFromUnicode( source );
		System.out.println( "Result from: " + result );
		setUseDoubleSlash( true );
		result = parseFromUnicode( source );
		System.out.println( "Result from double slash: " + result );
		
		String toResult = parseToUnicode( result );
		System.out.println( "Result to double slash: " + toResult );
	}
	
	public static void setParsePlica( boolean parse )
	{
		parsePlica = parse;
	}
}
