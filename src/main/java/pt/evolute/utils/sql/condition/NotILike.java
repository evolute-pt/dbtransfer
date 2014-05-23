package pt.evolute.utils.sql.condition;

import pt.evolute.utils.sql.Condition;
import pt.evolute.utils.sql.Operand;

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
