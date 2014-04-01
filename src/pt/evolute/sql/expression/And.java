package pt.evolute.sql.expression;

import pt.evolute.sql.Expression;

public class And extends Expression
{
	public And( Expression left, Expression right )
	{
		super( left, right );
	}

	@Override
	public String getSymbol()
	{
		return "AND";
	}
}