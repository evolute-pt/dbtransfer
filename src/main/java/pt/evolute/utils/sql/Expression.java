package pt.evolute.utils.sql;

import pt.evolute.utils.error.ErrorLogger;
import pt.evolute.utils.sql.backend.Backend;
import pt.evolute.utils.sql.expression.And;
import pt.evolute.utils.sql.expression.Or;

public abstract class Expression
{
	protected final Condition iCondition;
	protected final Expression iLeft;
	protected final Expression iRight;

	protected Backend backend = null;
	
	protected boolean unicode = false;
	
	public Expression( Condition cond )
	{
		iCondition = cond;
		iLeft = null;
		iRight = null;
	}
	
	public Expression( Expression left, Expression right )
	{
		iCondition = null;
		iLeft = left;
		iRight = right;
	}
	
	@Override
	public String toString()
	{
		if( iCondition != null )
		{
			iCondition.setBackend( backend );
		}
		return "( " +  getLeft() + " " + getSymbol() + " " + getRight() + " )";
	}
	
	public String getLeft()
	{
		iLeft.setBackend( backend );
		return iLeft.toString();
	}
	
	public String getRight()
	{
		String right = null;
		if( iRight != null )
		{
			iRight.setBackend( backend );
			right = iRight.toString();
		}
		else
		{
			ErrorLogger.logException( new Exception( "NULL right expression!!! getSymbol: " 
						+ getSymbol() + " left: " + getLeft() ) );
			right = " TRUE ";
		}
		return right;
	}
	
	public abstract String getSymbol();
	
	public And and( Expression other )
	{
		return new And( this, other );
	}
	
	public Or or( Expression other )
	{
		return new Or( this, other );
	}
	
	public void setBackend( Backend backend )
	{
		this.backend = backend;
	}
	
	public void setUnicode( boolean translate )
	{
		unicode = translate;
	}
}