package pt.evolute.utils.sql.condition;

import pt.evolute.utils.sql.Condition;
import pt.evolute.utils.sql.Operand;
/**
 *
 * @author  fpalma
 */
public class Between extends Condition
{
	private String leftStr = null;
	private String rightStr = null;
	private Operand right;
	
	/** Creates a new instance of Between */
	public Between( Operand left, Operand right )
	{
		super( left, right );
		this.right = right;
	}
	
	@Override
	public String getSymbol()
	{
		return "BETWEEN";
	}
	
	@Override
	public String getLeft()
	{
		if( leftStr == null )
		{
			leftStr = super.getLeft();
			return leftStr;
		}
		else
		{
			return leftStr;
		}
	}
	
	@Override
	public String getRight()
	{
		if( rightStr == null )
		{
			right.setBackend( getBackend() );
			Object []array = right.getInnerData();
			if( array == null || array.length != 2)
			{
				rightStr = "";
			}
			else
			{
				Operand first = array[ 0 ] instanceof Operand ? ( Operand )array[ 0 ] : new Operand( array[ 0 ] );
				first.setBackend( getBackend() );
				Operand second = array[ 1 ] instanceof Operand ? ( Operand )array[ 1 ] : new Operand( array[ 1 ] );
				second.setBackend( getBackend() );
				rightStr = first + " AND " + second;
			}
			return rightStr;
		}
		else
		{
			return rightStr;
		}
	}
}
