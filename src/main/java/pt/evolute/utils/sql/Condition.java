package pt.evolute.utils.sql;

import pt.evolute.utils.sql.backend.Backend;


public abstract class Condition
{
	protected Operand iLeft;
	protected Operand iRight;

	private Backend backend = null;
	
	public Condition( Operand left, Operand right )
	{
		iLeft = left;
		iRight = right;
	}

	@Override
	public String toString()
	{
		return getLeft() + " " + getSymbol() + " " + getRight();
	}
	
	public String getLeft()
	{
		iLeft.setBackend( backend );
		return iLeft.toString();
	}
	
	public String getRight()
	{
		iRight.setBackend( backend );
		return iRight.toString();
	}
	
	public void setBackend( Backend backend )
	{
		this.backend = backend;
	}
	
	public Backend getBackend()
	{
		return backend;
	}
	
	public abstract String getSymbol();
}