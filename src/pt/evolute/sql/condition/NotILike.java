package pt.evolute.sql.condition;

import pt.evolute.sql.Condition;
import pt.evolute.sql.Operand;

public class NotILike extends Condition
{

	public NotILike( Operand left, Operand right )
	{
		super( left, right );
	}
	
	@Override
	public String getSymbol()
	{
		return "NOT ILIKE";
	}
}
