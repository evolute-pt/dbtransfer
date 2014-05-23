package pt.evolute.utils.sql.backend;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import pt.evolute.utils.ddl.DDLDefaultValue;
import pt.evolute.utils.error.ErrorLogger;
import pt.evolute.utils.sql.Select2;
import pt.evolute.utils.sql.function.SQLCast;
import pt.evolute.utils.sql.function.SQLConcat;
import pt.evolute.utils.sql.function.SQLFunction;
import pt.evolute.utils.sql.function.SQLLength;
import pt.evolute.utils.sql.function.SQLMax;
import pt.evolute.utils.string.EvoStringUtils;

public class SQLServerBackend extends DefaultBackend
{
	private static final String INIT_QUERY[] = new String[]{ "SET DATEFORMAT ymd" };
	private static final String SQLSERVER_PLICA_ESCAPED = "''";
	
	private static final String OFFSET_COLUMN_NAME = "column_number_for_offset";
	private static final String OFFSET_TABLE_ALIAS = "table_name_for_offset";
	
	private boolean userLengthFunction = false;
	
	private boolean isEvoluteSupportedDatabase = false;
	
	public SQLServerBackend()
	{
		registerReservedKeyword("database");
		registerReservedKeyword("order");
		registerReservedKeyword("key");
	}
	
	@Override
	public void config( Connection con )
	{
		userLengthFunction = false;
		try
		{
/*			if( !functionExists( con,"len" ) )
			{
				
				if( !functionExists( con,"dbo.length" ) )
				{
//					userLengthFunction = true;
//					if( !functionExists( con,"length" ) )
//					{
						isEvoluteSupportedDatabase = false;
						userLengthFunction = false;
						System.out.println( "No function LENGTH found" );
						ErrorLogger.logException(new Exception("Function dbo.length and len don't exist!"));
//					}
//					else
//					{
//						userLengthFunction = false;
//						isEvoluteSupportedDatabase = true;
//					}
				}
				else
				{
					userLengthFunction = true;
					isEvoluteSupportedDatabase = true;
				}
			}
			else*/
			if( functionExists( con,"plain" ) )
			{
				isEvoluteSupportedDatabase = true;
			}
			
		}
		catch (Exception e)
		{
			ErrorLogger.logException(e);
		}
	}
	
	/*
	public void config( Connection con )
	{
		System.out.println( "TESTING LENGTH!" );
	
		try
		{
			Statement stm = con.createStatement();
			stm.execute( "SELECT LENGTH(1);" );
			System.out.println( "LENGTH EXISTS" );
			stm.close();
			isEvoluteSupportedDatabase = true;
		}
		catch( SQLException ex )
		{
			System.out.println( "TESTING dbo.LENGTH!" );
			userLengthFunction = true;
			try
			{
				Statement stm = con.createStatement();
				stm.execute( "SELECT dbo.LENGTH(1);" );
				System.out.println( "dbo.LENGTH EXISTS" );
				stm.close();
				isEvoluteSupportedDatabase = true;
			}
			catch( SQLException ex1 )
			{
				userLengthFunction = false;
				ErrorLogger.logException( ex );
				ErrorLogger.logException( ex1 );
				isEvoluteSupportedDatabase = false;
			}
		}
	}*/
	
	public boolean functionExists(Connection con, String function) throws Exception{
		Statement stm = con.createStatement();
		stm.execute( "select * from Information_schema.Routines where Specific_schema='dbo' and Routine_Type='FUNCTION'" +
				" and Specific_Name='"+function+"'");
		ResultSet s = stm.getResultSet();
		
		boolean hasNext = s.next();
		
		s.close();
		stm.close();
		
		System.out.println("Function " + function + " exists: " + hasNext);
		
		return hasNext;
	}
	
	@Override
	public String[] getInitQuery()
	{
		return INIT_QUERY;
	}
	
	@Override
	public String getBoolean( boolean bool )
	{
		return "" + ( bool ? 1: 0 ); 
	}
	
	@Override
	public String getBegin()
	{
		return "BEGIN TRANSACTION;";
	}
	
	@Override
	public String getUserFunctionPrefix()
	{
		return "dbo.";
	}
	
	@Override
	public CharSequence getUserFunctionName( SQLFunction fun )
	{
		String prefix = getUserFunctionPrefix();
		String name = fun.getName();
		if( fun instanceof SQLMax
				|| ( !userLengthFunction && fun instanceof SQLLength )
				|| fun instanceof SQLConcat
				|| fun instanceof SQLCast )
		{
			prefix = "";
		}
		if( fun instanceof SQLLength )
		{
			name = "LEN";
		}
		else if( fun instanceof SQLConcat )
		{
			name = "+";
		}
		else
		{
			name = fun.getName();
		}
		return prefix + name;
	}
	
	@Override
	public String getLimitFieldsPrefix(int limit)
	{
		return "TOP "+ limit;
	}
	
	@Override
	public String getLimitQuerySuffix(int limit)
	{
		return "";
	}
	
	@Override
	public String portSyntax( CharSequence sql )
	{
		String query = sql.toString();
		if( query != null && !query.isEmpty() )
		{
			query = query.replaceAll( "(?i) type ", " " );
			query = query.replaceAll( "(?i)add column", "add" );
			query = query.replaceAll( "(?i)now\\(\\)", "getutcdate()" );
			query = query.replaceAll( "(?i)serial", "int identity" );
			query = query.replaceAll( "(?i)timestamp without time zone", "datetime" );
			query = query.replaceAll( "(?i)timestamp", "datetime" );
			query = query.replaceAll( "(?i)double precision", "float" );
			query = query.replaceAll( "(?i)boolean", "bit" );
			query = query.replaceAll( "(?i)true", "1" );
			query = query.replaceAll( "(?i)false", "0" );
			query = query.replaceAll( "(?i)current_date", "getdate()" );
			query = query.replaceAll( "(?i)[||]", "+" );
			query = query.replaceAll( "(?i)nulls first", "" );
			query = query.replaceAll( "(?i)nulls last", "" );
//			query = query.replaceAll( " (?i)date\\(", " " + getUserFunctionPrefix() + "DATE(" );
			query = EvoStringUtils.removeAfter( query, "()", "drop function" );
		}
		return query;
	}
	
/*	public static void main( String arg[] )
	{
		System.out.println( new SQLServerBackend().portSyntax( "DISTINCT DATE( created_stamp )" ) );
	}*/
	
	@Override
	public String getOffsetQueryPrefix( Select2 query )
	{
		String result = "";
		if( query != null )
		{
			CharSequence order = query.getOrderExpression();
			if( order != null && order.length() > 0 )
			{
				result = "SELECT * FROM (";
				query.insertFieldBefore( "row_number() over("+order+" ) " + OFFSET_COLUMN_NAME );
			}
		}
		return result;
	}
	
	@Override
	public String getOffsetQuerySuffix(int offset)
	{
		return ") " + OFFSET_TABLE_ALIAS + " WHERE " + OFFSET_COLUMN_NAME + " > " + offset;
	}
	
	@Override
	public String getDDLDefaultValueConstraint(String fieldName,
			DDLDefaultValue defaultValue)
	{
		//TODO
		return null;
	}
	
	@Override
	public boolean getDDLDefaultValueIsConstraint()
	{
		return true;
	}

	@Override
	public boolean supportsILike()
	{
		return false;
	}
	
	@Override
	public CharSequence escapeUnicode( CharSequence str )
	{
		return isEvoluteSupportedDatabase? super.escapeUnicode( str ) : EvoStringUtils.parsePlica( str, SQLSERVER_PLICA_ESCAPED );
	}
}
