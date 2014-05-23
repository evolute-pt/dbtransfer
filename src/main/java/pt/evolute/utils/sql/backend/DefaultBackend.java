package pt.evolute.utils.sql.backend;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import pt.evolute.utils.ddl.DDLDefaultValue;
import pt.evolute.utils.sql.Expression;
import pt.evolute.utils.sql.Select2;
import pt.evolute.utils.sql.function.SQLFunction;
import pt.evolute.utils.string.EvoStringUtils;
import pt.evolute.utils.string.UnicodeChecker;

public class DefaultBackend implements Backend
{
	protected static final int CONNECTION_CHECK_TIMEOUT_MS = 400;
	
	private final Pattern pPoint = Pattern.compile( "\\." );
	private final Pattern pComma = Pattern.compile( "\\," );
	private final Pattern pPipes = Pattern.compile( "\\|\\|" );
	
	private final Map<String,String> RESERVED_KEYWORDS = new HashMap<String,String>();

	private boolean escapeUnicode = true;


	protected void registerReservedKeyword( String keyword )
	{
		RESERVED_KEYWORDS.put( keyword, keyword );
	}

	@Override
	public String[] getInitQuery()
	{
		return null;
	}
	
	@Override
	public CharSequence getEscapedFieldName( final CharSequence name )
	{
		CharSequence result = name;
		if( name != null )
		{
			String nameStr = name.toString();
			if( nameStr.startsWith( "DISTINCT " ) )
			{
				result = "DISTINCT " + getEscapedFieldName(nameStr.substring( 9 ).trim() );
			}
			else if( nameStr.startsWith( "COALESCE(" ) ) 
			{
				result = "COALESCE(" + getEscapedFieldName( nameStr.substring( 9, nameStr.length() ).trim() )/*  + ")"*/;
			}
			else if( nameStr.indexOf( ',' ) > -1 )
			{
				String [] tokens = pComma.split( name );
				StringBuilder builder = new StringBuilder( getEscapedFieldName( tokens[ 0 ] ) );
				for( int i = 1; i < tokens.length; ++i )
				{
					builder.append( ", " );
					builder.append( getEscapedFieldName( tokens[ i ] ) );
				}
				result = builder;
			}
			else if( nameStr.indexOf( "||" ) > -1 )
			{
				String [] tokens = pPipes.split( name );
				StringBuilder builder = new StringBuilder( getEscapedFieldName( tokens[ 0 ] ) );
				for( int i = 1; i < tokens.length; ++i )
				{
					builder.append( "||" );
					builder.append( getEscapedFieldName( tokens[ i ] ) );
				}
				result = builder;
			}
			else if( !".".equals( name ) )
			{
				if( RESERVED_KEYWORDS.containsKey( name ) )
				{
					result = "\"" + RESERVED_KEYWORDS.get( name ) + "\"";
				}
				else if( nameStr.indexOf( '.' ) > -1 )
				{
					String [] tokens = pPoint.split( name );
					if( tokens.length == 1 )
					{
						if( RESERVED_KEYWORDS.containsKey( tokens[0] ) )
						{
							result = "\"" + RESERVED_KEYWORDS.get(tokens[0]) + "\"";
						}
					}
					else if( tokens.length == 2 )
					{
						boolean changed = false;
						if( RESERVED_KEYWORDS.containsKey( tokens[0] ) )
						{
							tokens[0] =  "\"" + RESERVED_KEYWORDS.get( tokens[0] ) + "\"";
							changed = true;
						}
						if( RESERVED_KEYWORDS.containsKey( tokens[1] ) )
						{
							tokens[1] =  "\"" + RESERVED_KEYWORDS.get( tokens[1] ) + "\"";
							changed = true;
						}
						if( changed )
						{
							result = tokens[0] + "." + tokens[1];
						}
					}
					else
					{
						// TODO support expressions, etc ... SELECT ( version / table ) FROM table_a
//						ErrorLogger.logException( new Exception( "Invalid Name: " + name ) );
					}
				}
			}
		}
		return result;
	}
	
	@Override
	public String getLimitFieldsPrefix(int limit)
	{
		return "";
	}
	
	@Override
	public String getLimitQuerySuffix(int limit)
	{
		return " LIMIT " + limit;
	}
	
	@Override
	public String getBoolean( boolean bool )
	{
		return "" + bool; 
	}
	
	@Override
	public String getBegin()
	{
		return "BEGIN";
	}
	
	@Override
	public CharSequence getUserFunctionPrefix()
	{
		return "";
	}

	public String portSyntax( CharSequence query )
	{
		return query.toString();
	}

	@Override
	public String getOffsetQueryPrefix( Select2 query )
	{
		return "";
	}

	@Override
	public String getOffsetQuerySuffix( int offset )
	{
		return " OFFSET " + offset;
	}

	@Override
	public void setEscapeUnicode( boolean escapeUnicode )
	{
		this.escapeUnicode = escapeUnicode;
	}

	@Override
	public boolean getEscapeUnicode()
	{
		return escapeUnicode;
	}

	@Override
	public CharSequence escapeUnicode( CharSequence str )
	{
		return escapeUnicode ? UnicodeChecker.parseToUnicode( str, true, false ) : EvoStringUtils.parsePlica( str );
	}

	@Override
	public CharSequence getUserFunctionName(SQLFunction fun)
	{
		return fun.getName();
	}

	@Override
	public String getDDLConstraintDefinitionPrefix()
	{
		return "CONSTRAINT ";
	}

	@Override
	public String getDDLCheckConstraint(Expression expression)
	{
		expression.setBackend( this );
		return "CHECK ( " + expression + " )";
	}

	@Override
	public String getDDLDefaultValueConstraint(String fieldName,
			DDLDefaultValue defaultValue)
	{
		return null;
	}

	@Override
	public String getDDLForeignKeyConstraint(List<String> fieldNames,
			String foreignTableName, List<String> foreignFieldNames)
	{
		String fieldNamesStr = "";
		for( String fieldName : fieldNames )
		{
			if( fieldNamesStr.length() > 0 )
			{
				fieldNamesStr += ", ";
			}
			fieldNamesStr += fieldName;
		}
		
		String foreignFieldNamesStr = "";
		for( String foreignFieldName : foreignFieldNames )
		{
			if( foreignFieldNamesStr.length() > 0 )
			{
				foreignFieldNamesStr += ", ";
			}
			foreignFieldNamesStr += foreignFieldName;
		}
		return "FOREIGN KEY ( " + fieldNamesStr + " ) REFERENCES " + foreignTableName + " ( " + foreignFieldNamesStr + " )";
	}

	@Override
	public String getDDLPrimaryKeyConstraint(List<String> fieldNames)
	{
		String fieldNamesStr = "";
		for( String fieldName : fieldNames )
		{
			if( fieldNamesStr.length() > 0 )
			{
				fieldNamesStr += ", ";
			}
			fieldNamesStr += fieldName;
		}
		return "PRIMARY KEY ( " + fieldNamesStr + " )";
	}

	@Override
	public String getDDLUniqueConstraint(List<String> fieldNames)
	{
		String fieldNamesStr = "";
		for( String fieldName : fieldNames )
		{
			if( fieldNamesStr.length() > 0 )
			{
				fieldNamesStr += ", ";
			}
			fieldNamesStr += fieldName;
		}
		return "UNIQUE ( " + fieldNamesStr + " )";
	}

	@Override
	public boolean getDDLDefaultValueIsConstraint()
	{
		return false;
	}

	@Override
	public boolean supportsILike()
	{
		return true;
	}
	
	public static void main( String arg[] )
	{
		DefaultBackend df = new DefaultBackend();
		df.registerReservedKeyword( "version" );
		System.out.println( "getEscapedFieldName: " + df.getEscapedFieldName( "COALESCE(''||tickets_pacientes.numero,tickets_espera.serie||tickets_espera.numero)" ) );
		System.out.println( "getEscapedFieldName: " + df.getEscapedFieldName( "DISTINCT dados_resultados.id, dados_resultados.super" ) );
		System.out.println( "getEscapedFieldName: " + df.getEscapedFieldName( "db.version" ) );
		try
		{
			Thread.sleep( 3000 );
		}
		catch( InterruptedException ex )
		{
		}
	}

	@Override
	public void config( Connection con )
	{
	}

	@Override
	public boolean supportsReturnGeneratedKeys()
	{
		return true;
	}

/*	@Override
	public String getKeepAliveQuery()
	{
		return "SELECT 1";
	}
*/
	@Override
	public boolean isValid(Connection con)
		throws SQLException
	
	{
		return con.isValid( CONNECTION_CHECK_TIMEOUT_MS );
	}
}
