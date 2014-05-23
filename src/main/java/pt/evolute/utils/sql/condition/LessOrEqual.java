package pt.evolute.utils.sql.condition;

import pt.evolute.utils.sql.Condition;
import pt.evolute.utils.sql.Operand;

public class LessOrEqual extends Condition
{
	public LessOrEqual( Operand left, Operand right )
	{
		super( left, right );
	}

	@Override
	public String getSymbol()
	{
		return "<=";
	}
}