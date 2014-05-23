package pt.evolute.utils.sql.condition;

import pt.evolute.utils.sql.Operand;

public class NotIn extends In
{
	private static final String connector_string = "AND";
	private static final String between_prefix_string = "NOT ";
	private static final String in_string = "NOT IN";

	public NotIn( Operand left, Operand right )
	{
		super( left, right );
	}

	@Override
	public String getConnector()
	{
		return connector_string;
	}

	@Override
	public String getBetweenPrefix()
	{
		return between_prefix_string;
	}

	@Override
	public String getInString()
	{
		return in_string;
	}
}