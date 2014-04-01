/*
 * SQLAlias.java
 *
 * Created on 6 de Junho de 2005, 12:59
 */

package pt.evolute.sql.function;

import pt.evolute.sql.Operand;
import pt.evolute.sql.backend.Backend;

/**
 *
 * @author  fpalma
 */
public class SQLAlias extends SQLFunction
{
	protected Operand parameter;
	protected String alias;
	
	/** Creates a new instance of SQLAlias */
	public SQLAlias( Operand parameter, String alias )
	{
		super( "" );
		this.parameter = parameter;
		this.alias = alias;
	}
	
	@Override
	public String getSymbol()
	{
		return "";
	}
	
	@Override
	public String toString()
	{
		return alias;
	}
	
	@Override
	public String toHeaderString()
	{
		return parameter.toString()  + " AS " + alias;
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
