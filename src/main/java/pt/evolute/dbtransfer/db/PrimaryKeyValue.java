package pt.evolute.dbtransfer.db;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

public class PrimaryKeyValue 
{
	private final static DateFormat D_F = DateFormat.getDateInstance();
	private final static DateFormat T_F = DateFormat.getTimeInstance();
	private final static DateFormat TS_F = DateFormat.getDateTimeInstance();
	
	private final List<Object> v = new ArrayList<Object>();
	
	public void add( Object o )
	{
		v.add( o );
	}
	
	public Object get( int p )
	{
		return v.get( p );
	}
	
	public Object[] toArray()
	{
		return v.toArray();
	}
	
	public List<Object> getList()
	{
		return new ArrayList<Object>( v );
	}
	
	@Override
	public int hashCode()
	{
		int i = 0;
		int shift = 1;
		for( Object o: v )
		{
			if( o == null )
			{
				o = "null";
			}
			if( o instanceof java.sql.Date )
			{
				o = D_F.format( o );
			}
			else if( o instanceof java.sql.Time )
			{
				o = T_F.format( o );
			}
			else if( o instanceof java.sql.Timestamp )
			{
				o = TS_F.format( o );
			}
			++shift;
			i += o.hashCode() << shift;
		}
		return i;
	}
	
	@Override
	public boolean equals( Object o )
	{
		boolean b = true;
		if( o == null || !( o instanceof PrimaryKeyValue ) )
		{
			b = false;
		}
		else
		{
			PrimaryKeyValue o1 = (PrimaryKeyValue)o;
			return o1.hashCode() == hashCode();
		}
		return b;
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		for( Object o: v )
		{
			sb.append( "*" );
			sb.append( "" + o );
		}
		return sb.toString();
	}
}
