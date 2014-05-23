package pt.evolute.utils.sql.table;

import pt.evolute.utils.db.DBConstants;
import pt.evolute.utils.sql.Expression;
/**
 *
 * @author  fpalma
 */
public class PostgreSQLJoinBuilder 
	implements JoinBuilder, DBConstants
{
	protected String table;
	protected String outer[][];
	protected JoinExpression joins[][];
	protected JoinBuilder builders[][];
	protected Expression joinConditions[];
	
	protected Expression whereExpression = null;
	protected String header = null;
	
	/** Creates a new instance of PostgreSQLJoinBuilder */
	public PostgreSQLJoinBuilder( String table, String outer[][], JoinExpression joins[][], 
									Expression joinConditions[] )
	{
		this.table = table;
		this.outer = outer;
		this.joins = joins;
		this.joinConditions = joinConditions;
		
		builders = new JoinBuilder[ joins.length ][];
		for( int i = 0; i < builders.length; i++ )
		{
			if( joins[ i ] != null )
			{
				builders[ i ] = new JoinBuilder[ joins[ i ].length ];
			}
			else
			{
				builders[ i ] = new JoinBuilder[ 0 ];
			}
			for( int j = 0; j < builders[ i ].length; j++ )
			{
				builders[ i ][ j ] = JoinBuilderFactory.getJoinBuilder( joins[ i ][ j ] );
			}
		}
	}
	
	public String getDatabaseType()
	{
		return DB_POSTGRESQL;
	}
	
	public Expression getFilter()
	{
		return null;
	}
	
	public String getHeader()
	{
		if( header == null )
		{
			header = table;
			for( int i = 0; i < outer.length; i++ )
			{
				header += " LEFT OUTER JOIN ";
				if( ( outer[ i ] != null && outer[ i ].length > 1 ) 
						|| builders[ i ].length > 0 )
				{
					header += "( ";
				}
				if( outer[ i ] != null && outer[ i ].length > 0 )
				{
					header += outer[ i ][ 0 ];
					for( int j = 1; j < outer[ i ].length; j++ )
					{
						header += ", " + outer[ i ][ j ];
					}
				}
				if( builders[ i ].length > 0 )
				{
					if( outer[ i ] != null && outer[ i ].length > 0 )
					{
						header += ", ";
					}
					header += builders[ i ][ 0 ].getHeader();
					for( int j = 1; j < builders[ i ].length; j++ )
					{
						header += ", " + builders[ i ][ j ].getHeader();
					}
				}
				if( ( outer[ i ] != null && outer[ i ].length > 1 ) 
						|| builders[ i ].length > 0 )
				{
					header += " )";
				}
				header += " ON ( " + joinConditions[ i ] + " )";
			}
		}
		return header;
	}
	
}
