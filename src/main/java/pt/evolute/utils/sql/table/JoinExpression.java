package pt.evolute.utils.sql.table;

import pt.evolute.utils.Singleton;
import pt.evolute.utils.db.DBConstants;
import pt.evolute.utils.sql.Expression;

/**
 *
 * @author  fpalma
 */
public class JoinExpression 
	implements DBConstants, TableExpression
{
//	public static final int LEFT_OUTER = 0;
	
	protected final String table;
	protected final String outer[][];
	protected final JoinExpression joins[][];
	protected final Expression conditions[];
	
	protected String joinDatabaseType;
	protected JoinBuilder joinBuilder;
	
	/** Creates a new instance of JoinExpression */
	public JoinExpression( String table, String outer, Expression joinCondition,
							String databaseType )
	{
		this( table, new String[][]{ { outer } }, null, new Expression[]{ joinCondition }, databaseType );
	}
	
	public JoinExpression( String table, String outer[], JoinExpression joins[], Expression joinCondition,
							String databaseType )
	{
		this( table, new String[][]{ outer }, new JoinExpression[][]{ joins }, 
				new Expression[]{ joinCondition }, databaseType );
	}
	
	public JoinExpression( String table, String outer[][], JoinExpression joins[][], Expression joinConditions[],
							String databaseType )
	{
		this.table = table;
		this.outer = outer;
		if( joins != null )
		{
			this.joins = joins;
		}
		else
		{
			this.joins = new JoinExpression[ outer.length ][ 0 ];
		}
		conditions = joinConditions;
		if( databaseType == null )
		{
			joinDatabaseType = ( String ) Singleton.getInstance( Singleton.DEFAULT_DATABASE_TYPE );
		}
		else
		{
			joinDatabaseType = databaseType;
		}
	}
	
	public void setDatabaseType( String type )
	{
		joinDatabaseType = type;
	}
	
	@Override
	public String toString()
	{
		initBuilder();
		return joinBuilder.getHeader();
	}
	
	public String getHeader()
	{
		initBuilder();
		return joinBuilder.getHeader();
	}
	
	public Expression getWhereExpression()
	{
		initBuilder();
		return joinBuilder.getFilter();
	}
	
	private void initBuilder()
	{
		if( joinBuilder == null )
		{
			joinBuilder = JoinBuilderFactory.getJoinBuilder( this );
		}
	}
}
