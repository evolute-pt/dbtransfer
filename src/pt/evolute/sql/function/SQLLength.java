package pt.evolute.sql.function;

import pt.evolute.sql.Operand;
import pt.evolute.sql.backend.Backend;

public class SQLLength extends SQLFunction
{
	protected Operand parameter;
		
	public SQLLength( Operand parameter )
	{
		super( "LENGTH" );
		this.parameter = parameter;
	}
	
	@Override
	public String getSymbol()
	{
		return "" + getBackend().getUserFunctionName( this );
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
