package pt.evolute.sql.expression;

import pt.evolute.sql.Condition;
import pt.evolute.sql.Expression;


public class Atom extends Expression
{
	public Atom( Condition cond )
	{
		super( cond );
	}

	@Override
	public String toString()
	{
		iCondition.setBackend( backend );
		return iCondition.toString();
	}
	
	@Override
	public String getSymbol()
	{
		return "";
	}
}