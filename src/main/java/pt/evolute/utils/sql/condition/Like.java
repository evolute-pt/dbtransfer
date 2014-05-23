package pt.evolute.utils.sql.condition;

import pt.evolute.utils.sql.Condition;
import pt.evolute.utils.sql.Operand;
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