package pt.evolute.utils.sql.condition;

import pt.evolute.utils.sql.Condition;
import pt.evolute.utils.sql.Operand;

public class Equal extends Condition
{
	public Equal( Operand left, Operand right )
	{
		super( left, right );
	}

	@Override
	public String getSymbol()
	{
		if( iRight.isNull() )
		{
			return "IS";
		}
		else
		{
			return "=";
		}
	}
}