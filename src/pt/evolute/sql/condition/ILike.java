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
public class ILike extends Condition
{
	
	/** Creates a new instance of ILike */
	public ILike( Operand left, Operand right )
	{
		super( left, right );
	}
	
	@Override
	public String getSymbol()
	{
		return getBackend().supportsILike() ? "ILIKE" : "LIKE";
	}
	// SQLServer '=' && 'LIKE' are case INsensitive !
	
	
//	public String getLeft()
//	{
//		String str = super.getLeft();
//		if( !getBackend().supportsILike() )
//		{
//			if( iLeft instanceof Field )
//			{
//				str = "lower( " + str + " )";
//			}
//			else
//			{
//				str = str.toLowerCase();
//			}
//		}
//		return str;
//	}
//
//	public String getRight()
//	{
//		String str = super.getRight();
//		if( !getBackend().supportsILike() )
//		{
//			if( str != null )
//			{
//				str = str.toLowerCase();
//			}
//		}
//		return str;
//	}
}