/*
 * SQLMonth.java
 *
 * Created on 6 de Junho de 2005, 12:55
 */

package pt.evolute.sql.function;

import pt.evolute.sql.Operand;
import pt.evolute.sql.backend.Backend;
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
