package pt.evolute.dbtransfer.db.helper;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import pt.evolute.dbtransfer.db.DBConnection;

public class SQLServerHelper extends NullHelper
{
	private static final int MAX_VARCHAR_BYTES = 8000;
	
	private static final Map< String, String > OUTPUT = new HashMap< String, String >();
	private static final Map< String, String > RESERVED = new HashMap< String, String >();
	private static final Map< String, String > NORMALIZE = new HashMap< String, String >();
	private static final Map<String,String> DEFAULTS = new HashMap<String,String>();
	private static final Map<String,String> NORMALIZE_DEFAULTS = new HashMap<String,String>();
	
	static
	{
		OUTPUT.put( "serial", "int identity" );
		OUTPUT.put( "boolean", "bit" );
		OUTPUT.put( "blob", "image" );
		OUTPUT.put( "datetimetz", "datetime" );
		OUTPUT.put( "timestamp", "datetime" );
		OUTPUT.put( "int8", "bigint" );
		
		RESERVED.put( "database", "database" );
		RESERVED.put( "order", "order" );
		RESERVED.put( "key", "key" );
		
		NORMALIZE.put( "bit", "boolean" );
		NORMALIZE.put( "int identity", "serial" );
		NORMALIZE.put( "decimal() identity", "serial" );
		NORMALIZE.put( "numeric() identity", "serial" );
		NORMALIZE.put( "numeric", "bigint" );
		NORMALIZE.put( "decimal", "bigint" );
		NORMALIZE.put( "bigint identity", "serial" );
		NORMALIZE.put( "smallint identity", "serial" );
//		NORMALIZE.put( "uniqueidentifier", "uuid" );
		NORMALIZE.put( "uniqueidentifier", "varchar" );
//		NORMALIZE.put( "tinyint", "smallint" );
		NORMALIZE.put( "image", "bytea" );
		NORMALIZE.put( "nvarchar", "varchar" );
		NORMALIZE.put( "ntext", "text" );
		NORMALIZE.put( "binary", "bytea" );
		NORMALIZE.put( "smalldatetime", "datetime" );
		NORMALIZE.put( "sysname", "varchar" );
		NORMALIZE.put( "varbinary", "bytea" );
		
		DEFAULTS.put( "(getutcdate())", "CURRENT_DATE" );
		
		NORMALIZE_DEFAULTS.put( "getutcdate()", "CURRENT_DATE" );
		NORMALIZE_DEFAULTS.put( "getdate()", "CURRENT_DATE" );
		NORMALIZE_DEFAULTS.put( "[dbo].[Date](getdate())", "CURRENT_DATE" );
		NORMALIZE_DEFAULTS.put( "CONVERT([char](10),getdate(),(102))", "CURRENT_DATE" );
	}
	
	private static SQLServerHelper translator = null;
	
	private SQLServerHelper()
	{
	}
	
	public static SQLServerHelper getTranslator()
	{
		if( translator == null )
		{
			translator = new SQLServerHelper();
		}
		return translator;
	}
	
	@Override
	public String outputType( String type, Integer size )
	{
		String output = OUTPUT.get( type );
		if( output == null )
		{
			output = type;
		}
		if( size != null && size >= MAX_VARCHAR_BYTES )
		{
			if( output.startsWith( "text" ) )
			{
				size = null;
			}
			else if( output.startsWith( "varchar" ) )
			{
				size = MAX_VARCHAR_BYTES;
			}
		}
		if( size != null )
		{
			output = output + "( " + ( size >= MAX_VARCHAR_BYTES ? "max" : size ) + ")";
		}
		return output;
	}
	
	@Override
	public String outputName( String name )
	{
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
			if( type.contains( "identity" ) )
			{
				System.out.println( "SQLServer original type: " + type + " - press enter" );
				try
				{
					System.in.read();
				}
				catch( Exception ex )
				{}
			}
		}
		return normalize;
	}
	
	@Override
	public String preLoadSetup( String table )
	{
		return "SET IDENTITY_INSERT " + table + " ON;";
	}
	
	@Override
	public String postLoadSetup( String table )
	{
		return "SET IDENTITY_INSERT " + table + " OFF;";
	}
	
        @Override
	public void setDefaultValue( DBConnection con, String table, String typeName, String column, String value )
		throws Exception
	{
		
		if( !"int identity".equals( typeName ) )
		{
//			alter table dbo.CustomerReport
	//		add constraint df_CustomerReport_rundate default getutcdate() for rundate
			StringBuilder buff = new StringBuilder("ALTER TABLE ");
			buff.append( table );
			buff.append(" ADD CONSTRAINT ");
			buff.append( table );
			buff.append( "_" );
			buff.append( column );
			buff.append( "_default DEFAULT " );
			buff.append( outputValue( value ) );
			buff.append(" FOR " );
			buff.append( outputName(column) );
			try
			{
				con.executeQuery(buff.toString());
			}
			catch(SQLException ex)
			{
				System.out.println("C: " + buff);
				throw ex;
			}
		}
	}
	
        @Override
	public void setNotNull( DBConnection con, String table, String typeName, String column, Integer size )
		throws Exception
	{
		if( !"int identity".equals( typeName ) )
		{
			StringBuilder buff = new StringBuilder("ALTER TABLE ");
			buff.append( table );
			buff.append( " ALTER COLUMN " );
			buff.append( outputName(column) );
			buff.append( " " );
			buff.append( typeName );
			if( size != null )
			{
				if ( typeName.startsWith( "varchar" )  )
				{
					buff.append( " (" );
					buff.append( ( size >= MAX_VARCHAR_BYTES ? "max" : size ) );
					buff.append( ")" );
				}
			}
			buff.append( " NOT NULL" );
			try
			{
				con.executeQuery(buff.toString());
			}
			catch(SQLException ex)
			{
				System.out.println("C: " + buff);
				if( ex.getMessage().contains("ultiple") )
				{
					System.out.println("EX: " + table + "-" + column + ": " + ex.getMessage());
					//							throw ex;
				}
			}
		}
	}
	
	@Override
	public String outputValue( String value )
	{
		return outputValue( ( Object )value ).toString();
	}
	
	@Override
	public Object outputValue( Object value )
	{
		if( "true".equals( value ) )
		{
			value = 1;
		}
		else if( "false".equals( value ) )
		{
			value = 0;
		}
		return value;
	}
	
        @Override
	public String normalizeDefault( String str )
	{
		String norm = str;
		if( str != null )
		{
			if( str.startsWith( "(" ) && str.endsWith( "" ) )
			{
				str = str.substring( 1, str.length() - 1 );
			}
			norm = NORMALIZE_DEFAULTS.get( str );
			if( norm == null )
			{
				norm = str;
			}
		}
		return norm;
	}
}