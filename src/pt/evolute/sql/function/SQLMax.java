/*
 * Max.java
 *
 * Created on 2 de Junho de 2005, 19:44
 */

package pt.evolute.sql.function;

import pt.evolute.sql.Operand;
import pt.evolute.sql.backend.Backend;
/**
 *
 * @author  fpalma
 * 
 * Use {@link com.evolute.utils.sql.function.Max} instead
 */
public class SQLMax extends SQLFunction
{
	protected Operand parameter;
	
	/** Creates a new instance of Max */
	public SQLMax( Operand parameter )
	{
		super( "Max" );
		this.parameter = parameter;
	}
	
	@Override
	public String getSymbol()
	{
		return "" + getBackend().getUserFunctionName( this );
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
