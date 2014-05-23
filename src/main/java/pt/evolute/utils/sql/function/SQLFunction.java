package pt.evolute.utils.sql.function;

import pt.evolute.utils.sql.Field;
/**
 *
 * @author  fpalma
 */
public abstract class SQLFunction extends Field
{
	public SQLFunction( String representation )
	{
		super( representation );
	}
	
	public abstract String getSymbol();
}
