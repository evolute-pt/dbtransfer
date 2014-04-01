/*
 * Like.java
 *
 * Created on 11 de Junho de 2003, 15:58
 */

package pt.evolute.sql.condition;

import pt.evolute.sql.Condition;
import pt.evolute.sql.Operand;
/**
 *
 * @author  lflores
 */
public class Like extends Condition
{
	
	/** Creates a new instance of Like */
	public Like( Operand left, Operand right )
	{
		super( left, right );
	}
	
	@Override
	public String getSymbol()
	{
		return "LIKE";
	}
	
}