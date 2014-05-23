package pt.evolute.utils.sql.function;

import pt.evolute.utils.sql.Operand;
import pt.evolute.utils.sql.backend.Backend;
/**
 *
 * @author  fpalma
 * 
 * Use {@link com.evolute.utils.sql.function.Max} instead
 */
public class SQLCast extends SQLFunction
{
	protected Operand parameter;
	protected String type;
	
	/** Creates a new instance of Max */
	public SQLCast( Operand parameter, String type )
	{
		super( "CAST" );
		this.parameter = parameter;
		this.type = type;
	}
	
	@Override
	public String getSymbol()
	{
		return "" + getBackend().getUserFunctionName( this );
	}
	
	@Override
	public String toString()
	{
		return getSymbol() + "( " + parameter + " AS " + type + " )";
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
