package pt.evolute.dbtransfer.db.beans;

import pt.evolute.utils.string.StringPlainer;

public class Name 
{
	public final String saneName;
	public final String originalName;
	
	public Name( String original )
	{
		originalName = original;
		String sane = original;
		if( sane.contains( " " ) )
		{
			sane = sane.replace( ' ', '_' );
		}
		if( sane.contains( "." ) )
		{
			sane = sane.replace( '.', '_' );
		}
		saneName = StringPlainer.convertString( sane );
	}
	
	public String toString()
	{
		return saneName;
	}
}
