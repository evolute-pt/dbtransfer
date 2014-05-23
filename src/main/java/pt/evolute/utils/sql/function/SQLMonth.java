package pt.evolute.utils.sql.function;

import pt.evolute.utils.sql.Operand;
import pt.evolute.utils.sql.backend.Backend;
/**
 *
 * @author  fpalma
 */
public class SQLMonth extends SQLFunction
{
	protected Operand parameter;
	
	/** Creates a new instance of SQLMonth */
	public SQLMonth( Operand parameter )
	{
		super( "MONTH" );
		this.parameter = parameter;
	}
	
	@Override
	public String getSymbol()
	{
		return "MONTH";
	}
	
	@Override
	public String toString()
	{
		return getSymbol() + "(" + parameter + ")";
	}
	
	@Override
	public void setBackend( Backend backend )
	{
		super.setBackend( backend );
		if( parameter != null )
		{
			parameter.setBackend( backend );
		}
	}
}
