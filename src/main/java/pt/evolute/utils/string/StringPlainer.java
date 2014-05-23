package pt.evolute.utils.string;

public class StringPlainer
{
	public static String convertString( String s )
	{
		return convertString( s, false, false );
	}
	
	public static String convertString( String s, boolean preserveCase,
						boolean preserveNewLine )
	{
		if( s != null )
		{
			StringBuilder sb = new StringBuilder();
			for( int i = 0; i < s.length(); i++ )
			{
				Character c = convertCharacter( s.charAt( i ) );
				if( c != null )
				{
					sb.append( c );
				}
				else if( preserveNewLine && s.charAt( i ) == '\n' )
				{
					sb.append( new Character( s.charAt( i ) ) );
				}
			}
			
			if( preserveCase )
			{
				return sb.toString();
			}
			else
			{
				return sb.toString().toLowerCase();
			}
		}
		else
		{
			return null;
		}
	}
	
	public static Character convertCharacter( char c )
	{
		if( c == '\'' ) // apostrofe
		{
			return new Character( ' ' );
		}
		if( c >= '\u0021' && c <= '\u007e' )
		{
			return new Character( c );
		}
		if( c >= '\u0300' && c <= '\u0360' )
		{
			return null;
		}
		char cc = '\u0000';
		
		switch( c )
		{
			case ' ':
			{
				cc = ' ';
				break;
			}
			case '\u00c0': // A grave
			case '\u00c1': // A agudo
			case '\u00c2': // A circunflexo
			case '\u00c3': // A til
			case '\u00c4': // A trema
			case '\u00c5': // A bolinha
			{
				cc = 'A';
				break;
			}
			case '\u00c7': // C cedilha
			{
				cc = 'C';
				break;
			}
			case '\u00c8': // E grave
			case '\u00c9': // E agudo
			case '\u00ca': // E circunflexo
			case '\u00cb': // E trema
			{
				cc = 'E';
				break;
			}
			case '\u00cc': // I grave
			case '\u00cd': // I agudo
			case '\u00ce': // I circunflexo
			case '\u00cf': // I trema
			{
				cc = 'I';
				break;
			}
			case '\u00d1': // N til
			{
				cc = 'N';
				break;
			}
			case '\u00d2': // O grave
			case '\u00d3': // O agudo
			case '\u00d4': // O circunflexo
			case '\u00d5': // O til
			case '\u00d6': // O trema
			{
				cc = 'O';
				break;
			}
			case '\u00d9': // U grave
			case '\u00da': // U agudo
			case '\u00db': // U circunflexo
			case '\u00dc': // U trema
			{
				cc = 'U';
				break;
			}
			case '\u00dd': // Y agudo
			{
				cc = 'Y';
				break;
			}
			case '\u00e0': // a grave
			case '\u00e1': // a agudo
			case '\u00e2': // a circunflexo
			case '\u00e3': // a til
			case '\u00e4': // a trema
			case '\u00e5': // a bolinha
			case '\u00aa': // a pequeno usado em abreviaturas
			{
				cc = 'a';
				break;
			}
			case '\u00e7': // c cedilha
			{
				cc = 'c';
				break;
			}
			case '\u00e8': // e grave
			case '\u00e9': // e agudo
			case '\u00ea': // e circunflexo
			case '\u00eb': // e trema
			{
				cc = 'e';
				break;
			}
			case '\u00ec': // i grave
			case '\u00ed': // i agudo
			case '\u00ee': // i circunflexo
			case '\u00ef': // i trema
			{
				cc = 'i';
				break;
			}
			case '\u00f1': // n til
			{
				cc = 'n';
				break;
			}
			case '\u00f2': // o grave
			case '\u00f3': // o agudo
			case '\u00f4': // o circunflexo
			case '\u00f5': // o til
			case '\u00f6': // o trema
			{
				cc = 'o';
				break;
			}
			case '\u00f9': // u grave
			case '\u00fa': // u agudo
			case '\u00fb': // u circunflexo
			case '\u00fc': // u trema
			{
				cc = 'u';
				break;
			}
			case '\u00fd': // y agudo
			case '\u00ff': // y trema
			{
				cc = 'y';
				break;
			}
			case '\u00ba': // o pequeno usado em abreviaturas
			{
				cc = '.';
				break;
			}
			default:
			{
				return ' ';
			}
		}
		
		return new Character( cc );
	}
}
