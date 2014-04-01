package pt.evolute.textsearch.condition;

import pt.evolute.sql.Condition;
import pt.evolute.sql.Operand;

public final class TSQuery extends Condition
{
	
	private String leftStr = null;
	private String rightStr = null;

	public TSQuery( Operand left, Operand right )
	{
		super( left, right );
	}

	@Override
	public String getSymbol( )
	{
		return "@@";
	}
	
	@Override
	public String getLeft()
	{
		if( leftStr == null )
		{
			leftStr = super.getLeft();
			return leftStr;
		}
		else
		{
			return leftStr;
		}
	}
	
	@Override
	public String getRight( )
	{
		if( rightStr == null )
		{
			rightStr = "to_tsquery(' " + super.getRight( ) + " ')";;
			return rightStr;
		}
		else
		{
			return rightStr;
		}
	}
}
