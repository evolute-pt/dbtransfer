package pt.evolute.utils.sql.function;

import pt.evolute.utils.sql.Operand;
import pt.evolute.utils.sql.backend.Backend;

public class SQLConcat extends SQLFunction
{
	protected Operand parameters[];
	
	public SQLConcat( Operand parameters[] )
	{
		super( "CONCAT" );
		this.parameters = parameters;
		if( this.parameters != null )
		{
			for( int p = 0; p < this.parameters.length; p++ )
			{
				this.parameters[ p ] = new SQLCast( this.parameters[ p ], "VARCHAR" );
			}
		}
	}
	
	public SQLConcat( Operand parameter )
	{
		this( parameter != null ? new Operand[]{ parameter } : null );
	}
	
	@Override
	public String getSymbol()
	{
		return "" + getBackend().getUserFunctionName( this );
	}
	
	@Override
	public String toString()
	{
		String str = "";
		if( parameters != null && parameters.length > 0 )
		{
			str += parameters[ 0 ];
			for( int n = 1; n < parameters.length; n++ )
			{
				str += getSymbol() + parameters[ n ];
			}
		}
		return str;
	}
	
	@Override
	public void setBackend( Backend backend )
	{
		super.setBackend( backend );
		if( parameters != null  )
		{
			for( Operand parameter : parameters )
			{
				parameter.setBackend( backend );
			}
		}
	}
}
