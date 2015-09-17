package pt.evolute.dbtransfer.db.helper;

import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import pt.evolute.dbtransfer.db.DBConnection;
import pt.evolute.utils.arrays.Virtual2DArray;
import pt.evolute.utils.string.UnicodeChecker;

public class PostgreSQLServerHelper extends NullHelper
{
	private static final Map<String,String> OUTPUT = new HashMap<String,String>();
	private static final Map<String,String> RESERVED = new HashMap<String,String>();
	private static final Map<String,String> NORMALIZE = new HashMap<String,String>();
	private static final Map<String,String> DEFAULTS = new HashMap<String,String>();
	private static final Map<String,String> NORMALIZE_DEFAULTS = new HashMap<String,String>();
	
	static
	{
		OUTPUT.put( "boolean", "bool" );
		OUTPUT.put( "datetime", "timestamp" );
		OUTPUT.put( "datetimetz", "timestamptz" );
		OUTPUT.put( "blob", "bytea" );
		OUTPUT.put( "longblob", "bytea" );
		OUTPUT.put( "double", "double precision" );
		OUTPUT.put( "longvarchar", "text" );
		OUTPUT.put( "tinyint", "smallint" );
		
		RESERVED.put( "order", "order" );
		RESERVED.put( "user", "user" );
		
		NORMALIZE.put( "int4", "int" );
		NORMALIZE.put( "timestamp", "datetime" );
		NORMALIZE.put( "timestamptz", "datetimetz" );
		NORMALIZE.put( "bool", "boolean" );
		NORMALIZE.put( "bpchar", "char" );
		NORMALIZE.put( "float8", "float" );
		NORMALIZE.put( "bytea", "blob" );
		
		DEFAULTS.put( "'(newsequentialid())'", "" );
		DEFAULTS.put( "'(' ')'", "' '" );
		DEFAULTS.put( "'('S')'", "'S'" );
		DEFAULTS.put( "'('N')'", "'N'" );
		DEFAULTS.put( "(getdate())", "CURRENT_DATE" );
		DEFAULTS.put( "'(CONVERT([varchar](5),getdate(),(108)))'", "CURRENT_TIME" );
		
		NORMALIZE_DEFAULTS.put( "(now())", "CURRENT_TIMESTAMP" );
	}
	
	private static PostgreSQLServerHelper translator = null;
	
	private PostgreSQLServerHelper()
	{
		System.out.println( "PostgreSQL helper - setting double slash on UnicodeChecker" );
		UnicodeChecker.setUseDoubleSlash( true );
	}
	
	public static PostgreSQLServerHelper getTranslator()
	{
		if( translator == null )
		{
			translator = new PostgreSQLServerHelper();
		}
		return translator;
	}
	
	@Override
	public String outputType( String type, Integer size )
	{
		String output = OUTPUT.get( type.toLowerCase() );
		if( output == null )
		{
			output = type;
		}
		if( "text".equalsIgnoreCase( output ) )
		{
			size = null;
		}
		if( size != null && !"uuid".equalsIgnoreCase( output ) )
		{
			output = output + "( " + size + ")";
		}
		
		if( type.contains( "numeric" ) )
		{
			System.out.println( "NUMERIC <" + type + ">" );
			System.exit(0);
		}
		
		return output;
	}
	
	@Override
	public String outputName( String name )
	{
		if( name.contains( " " ) )
		{
			name = name.replace( ' ', '_' );
		}
		if( name.contains( "." ) )
		{
			name = name.replace( '.', '_' );
		}
//		name = StringPlainer.convertString( name );
		if( RESERVED.containsKey( name ) )
		{
			name = "\"" + name + "\"";
		}
		return name;
	}
	
	@Override
	public String normalizedType( String type )
	{
		String normalize = NORMALIZE.get( type );
		if( normalize == null )
		{
			normalize = type;
		}
		return normalize;
	}
	
	@Override
	public void fixSequences( DBConnection con, String table, String typeName, String column )
		throws Exception
	{
		if( "serial".equals(typeName))
		{
			StringBuilder buff = new StringBuilder("SELECT MAX( ");
			buff.append(column);
			buff.append(" ) FROM ");
			buff.append( table );
			Object value = null;
			try
			{
				//				System.out.println( "C: " + buff );
				con.executeQuery( "BEGIN;" );
				Virtual2DArray rs = con.executeQuery(buff.toString());
				value = rs.getObjects()[0][0];
				con.executeQuery( "COMMIT;" );
			}
			catch(SQLException ex)
			{
				if(ex.getMessage().indexOf("ultiple") == -1)
				{
					throw ex;
				}
			}
			if(value != null)
			{
				buff = new StringBuilder("SELECT setval( '");
				buff.append( table );
				buff.append("_");
				buff.append(column);
				buff.append("_seq', ");
				buff.append(value);
				buff.append(" )");
				try
				{
					System.out.println("C: " + buff);
					con.executeQuery( "BEGIN;" );
					con.executeQuery(buff.toString());
					con.executeQuery( "COMMIT;" );
				}
				catch(Exception ex)
				{
					if(ex.getMessage().indexOf("ultiple") == -1)
					{
						throw ex;
					}
				}
			}
		}
	}
	
	@Override
	public String normalizeValue( String value )
	{
		if( value != null )
		{
			if( value.endsWith( "::bpchar" ) )
			{
				value = value.substring( 1, value.length() - "::bpchar".length() - 1 );
			}
			else if( value.endsWith( "::character varying" ) )
			{
				value = value.substring( 1, value.length() - "::character varying".length() - 1 );
			}
			else if( "now()".equals( value ) )
			{
				value = "CURRENT_TIMESTAMP";
			}
			else if( "('now'::text)::date".equals( value ) )
			{
				value = "CURRENT_DATE";
			}
		}
		return value;
	}
	
	@Override
	public void setDefaultValue( DBConnection con, String table, String typeName, String column, String value )
			throws Exception
	{
		if( DEFAULTS.containsKey( value ) )
		{
			value = DEFAULTS.get( value );
		}
		if( value != null && !value.isEmpty() )
		{
			if( "bit".equalsIgnoreCase( typeName ) )
			{
				value = "CAST( " + value + " AS bit )";
			}
			else if( "boolean".equalsIgnoreCase( typeName )
					|| "bool".equalsIgnoreCase( typeName ) )
			{
				value = "CAST( " + value + " AS boolean )";
			}
			try
			{
				super.setDefaultValue( con, table, typeName, column, value );
			}
			catch( Exception ex )
			{
				System.out.println( "type: <" + typeName + "> value: <" + value + ">" ); 
				throw ex;
			}
		}
	}
	
	@Override
	public int translateType( int type ) 
	{
		int pType = type;
		switch ( type ) 
		{
			case Types.NCHAR:
				pType = Types.CHAR;
				break;	
			case Types.NVARCHAR:
				pType = Types.VARCHAR;
				break;
			case Types.LONGNVARCHAR:
				pType = Types.LONGVARCHAR;
			case Types.BOOLEAN:
				pType = Types.BIT;
			default:
				break;
		}
		return pType;
	}
	
        @Override
	public String normalizeDefault( String str )
	{
		String norm = NORMALIZE_DEFAULTS.get( str );
		if( norm == null )
		{
			norm = str;
		}
		return norm;
	}
    
    @Override
    public void setPreparedValue(PreparedStatement pStm, int col, Object o, int type ) 
            throws SQLException
    {
        if( type == Types.BLOB 
        		|| type == Types.LONGVARBINARY 
        		|| type == Types.VARBINARY
        		|| type == Types.BINARY )
        {
        	if( o != null )
        	{
	            o = outputValue( o );
	            
//	            System.out.println( "BLOB class: " + o.getClass() + " Type: " + type + " Col: " + col );
	            if( o instanceof byte[] )
	            {
	                pStm.setBytes( col + 1, ( byte[] )o );
	            }
	            else
	            {
	            	Blob b = ( Blob )o;
	                pStm.setBytes( col + 1, b.getBytes( 1, ( int )b.length() ) );
	            }
        	}
        	else
        	{
        		pStm.setBytes( col + 1, null );
        	}
        }
        else
        {
        	super.setPreparedValue( pStm, col, o, type );
        }
    }
}
