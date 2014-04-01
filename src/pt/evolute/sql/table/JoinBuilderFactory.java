/*
 * JoinBuilderFactory.java
 *
 * Created on 30 de Maio de 2005, 16:31
 */

package pt.evolute.sql.table;

import pt.evolute.db.DBConstants;
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
