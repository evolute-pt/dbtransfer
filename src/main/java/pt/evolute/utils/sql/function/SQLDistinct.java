package pt.evolute.utils.sql.function;

import pt.evolute.utils.sql.Operand;
import pt.evolute.utils.sql.backend.Backend;

/**
 *
 * @author  fpalma
 */
public class SQLDistinct extends SQLFunction
{
	protected Operand parameter;
	protected Operand parameters[];
	
	/** Creates a new instance of SQLDistinct */
	public SQLDistinct(Operand parameter)
	{
		super( "DISTINCT" );
		this.parameter = parameter;
	}
	
	public SQLDistinct(Object parameters[])
	{
		super( "DISTINCT" );
		this.parameters = new Operand[ parameters.length ];
		for( int n = 0; n < parameters.length; n++ )
		{
			this.parameters[ n ] = new Operand( parameters[ n ] );
		}
	}
	
	public SQLDistinct(Operand parameters[])
	{
		super( "DISTINCT" );
		this.parameters = parameters;
	}
	
	@Override
	public String getSymbol()
	{
		return "DISTINCT ";
	}
	
	@Override
	public String toString()
	{
		if( parameters == null || parameters.length == 0 )
		{
			return "DISTINCT " + parameter;
		}
		else
		{
			String str = "DISTINCT " + parameters[ 0 ];
			
			for( int n = 1; n < parameters.length; n++ )
			{
				str += ", " + parameters[ n ];
			}
			return str;
		}
	}
	
	@Override
	public void setBackend( Backend backend )
	{
		super.setBackend( backend );
		if( parameter != null )
		{
			parameter.setBackend( backend );
		}
		if( parameters != null && parameters.length > 0 )
		{
			for( Operand p: parameters )
			{
				p.setBackend( backend );
			}
		}
	}
}