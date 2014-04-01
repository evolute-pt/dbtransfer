package pt.evolute.jdbc;

import pt.evolute.arrays.Virtual2DArray;
import pt.evolute.sql.SQLQuery;

public interface StatementExecuter
{
	public Virtual2DArray executeSelectStatementWithCursor( String stm, SQLQuery query )
		throws Exception;
	
	public Virtual2DArray executeUpdateStatementWithCursor( String stm, SQLQuery query )
		throws Exception;
	
	public Object[][] executeSelectStatement( String stm, SQLQuery query )
		throws Exception;
	
	public Object[][] executeUpdateStatement( String stm, SQLQuery query )
		throws Exception;
}