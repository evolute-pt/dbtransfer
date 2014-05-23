package pt.evolute.utils.jdbc;

import pt.evolute.utils.arrays.Virtual2DArray;
import pt.evolute.utils.sql.SQLQuery;

public class StatementExecuterFactory
{
	private static StatementExecuter executer = null;
	
	// default initialization
	static
	{
//		executer = new DALStatementExecuter();
	}

	public static boolean isInitialized()
	{
		return executer != null;
	}

	public static void initialize( StatementExecuter exe )
	{
		executer = exe;
	}

	public static  Virtual2DArray executeSelectStatementWithCursor( String stm, SQLQuery query )
		throws Exception
	{
		if( executer == null )
		{
			throw new Exception( "StatementExecuterFactory: StatementExecuter is null\n" +
							stm + "\n" + query );
		}
		return executer.executeSelectStatementWithCursor( stm, query );
	}
	
	
	public static Virtual2DArray executeUpdateStatementWithCursor( String stm, SQLQuery query )
		throws Exception
	{
		if( executer == null )
		{
			throw new Exception( "StatementExecuterFactory: StatementExecuter is null\n" +
							stm + "\n" + query );
		}
		return executer.executeUpdateStatementWithCursor( stm, query );
	}
	
	public static Object[][] executeSelectStatement( String stm, SQLQuery query )
		throws Exception
	{
		if( executer == null )
		{
			throw new Exception( "StatementExecuterFactory: StatementExecuter is null\n" +
							stm + "\n" + query );
		}
		return executer.executeSelectStatement( stm, query );
	}
	
	public static Object[][] executeUpdateStatement( String stm, SQLQuery query )
		throws Exception
	{
		if( executer == null )
		{
			throw new Exception( "StatementExecuterFactory: StatementExecuter is null\n" +
							stm + "\n" + query );
		}
		return executer.executeUpdateStatement( stm, query );
	}	
}
