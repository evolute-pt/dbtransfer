package pt.evolute.sql.expression;

import pt.evolute.sql.Expression;

public class Or extends Expression
{
	public Or( Expression left, Expression right )
	{
		super( left, right );
	}

	@Override
	public String getSymbol()
	{
		return "OR";
	}
}