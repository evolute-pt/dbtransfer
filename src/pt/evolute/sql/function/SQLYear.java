/*
 * Year.java
 *
 * Created on 6 de Junho de 2005, 12:54
 */

package pt.evolute.sql.function;

import pt.evolute.sql.Operand;
import pt.evolute.sql.backend.Backend;
/**
 *
 * @author  fpalma
 */
public class SQLYear extends SQLFunction
{
	protected Operand parameter;
	
	/** Creates a new instance of SQLYear */
	public SQLYear( Operand parameter )
	{
		super( "YEAR" );
		this.parameter = parameter;
	}
	
	@Override
	public String getSymbol()
	{
		return "YEAR";
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
