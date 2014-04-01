/*
 * SQLFunction.java
 *
 * Created on 2 de Junho de 2005, 19:28
 */

package pt.evolute.sql.function;

import pt.evolute.sql.Field;
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
