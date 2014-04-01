package pt.evolute.sql.condition;

import pt.evolute.sql.Condition;
import pt.evolute.sql.Operand;

public class Less extends Condition
{
	public Less( Operand left, Operand right )
	{
		super( left, right );
	}

	@Override
	public String getSymbol()
	{
		return "<";
	}
}