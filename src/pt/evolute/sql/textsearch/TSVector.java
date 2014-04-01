package pt.evolute.sql.textsearch;

import pt.evolute.sql.Operand;

public class TSVector extends Operand
{
	private final String language;

	public static TSVector createTextSearchVector( String language, Operand... value )
	{
		return new TSVector( language, value );
	}
	
	public TSVector( String language, Operand... value )
	{
		super( value );
		this.language = language;
	}

	@Override
	public String toString( )
	{
		
		Object values[] = ( Object [] ) this.getInnerObject( );
		String strs[] = new String[ values.length ];
		for( int n = 0; n < values.length; n++ )
		{
			Operand operand = new Operand( values[ n ] );
			operand.setBackend( getBackend() );
			strs[ n ] = operand.toString();
		}
		
		StringBuffer sb = new StringBuffer( );
		
		sb.append( "to_tsvector('" + language + "', " );
		
		for( int i = 0; i < strs.length; i++ )
		{
			sb.append("coalesce('" + strs[ i ] + "', '')");
			
			if( i < strs.length - 1 )
			{
				sb.append( " || ' ' || " );
			}
			
		}
		
		sb.append( ")" );
		
		return sb.toString( );
	}
	
}
