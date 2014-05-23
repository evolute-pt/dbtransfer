package pt.evolute.utils.sql.condition;

import pt.evolute.utils.sql.Condition;
import pt.evolute.utils.sql.Operand;

public class NotLike extends Condition
{

	public NotLike( Operand left, Operand right )
	{
		super( left, right );
	}
	
	@Override
	public String getSymbol()
	{
		return "NOT LIKE";
	}
}
