/*
 * SQLCount.java
 *
 * Created on 6 de Junho de 2005, 13:15
 */

package pt.evolute.utils.sql.function;

import pt.evolute.utils.sql.Field;
import pt.evolute.utils.sql.Operand;
import pt.evolute.utils.sql.backend.Backend;
/**
 *
 * @author  fpalma
 */
public class SQLCount extends SQLFunction
{
	protected Operand parameter;
	
	public SQLCount()
	{
		this( new Field( "*" ) );
	}
	
	/** Creates a new instance of SQLCount */
	public SQLCount( Operand parameter )
	{
		super( "COUNT" );
		this.parameter = parameter;
	}
	
	@Override
	public String getSymbol()
	{
		return "COUNT";
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
