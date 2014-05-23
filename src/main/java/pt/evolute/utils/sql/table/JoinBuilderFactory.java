package pt.evolute.utils.sql.table;

import pt.evolute.utils.db.DBConstants;
/**
 *
 * @author  fpalma
 */
public class JoinBuilderFactory implements DBConstants
{
	
	/** Creates a new instance of JoinBuilderFactory */
	private JoinBuilderFactory()
	{
	}
	
	public static JoinBuilder getJoinBuilder( JoinExpression join )
	{
		if( DB_INFORMIX.equals( join.joinDatabaseType ) )
		{
			return new InformixJoinBuilder( join.table, join.outer, join.joins, join.conditions );
		}
		else if( DB_POSTGRESQL.equals( join.joinDatabaseType ) )
		{
			return new PostgreSQLJoinBuilder( join.table, join.outer, join.joins, join.conditions );
		}
		else if( DB_HSQL.equals( join.joinDatabaseType ) )
		{
			return new PostgreSQLJoinBuilder( join.table, join.outer, join.joins, join.conditions );
		}
		throw new RuntimeException( "Database not implemented: " + join.joinDatabaseType );
	}
}
