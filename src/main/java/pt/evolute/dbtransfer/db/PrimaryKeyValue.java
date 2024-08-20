package pt.evolute.dbtransfer.db;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class PrimaryKeyValue 
{
	public final static DateFormat D_F = new SimpleDateFormat( "yyyy-MM-dd" );
	public final static DateFormat T_F = new SimpleDateFormat( "HH:mm:ss" );
	public final static DateFormat TS_F = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
	
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
//		int i = 0;
//		int shift = 1;
//		for( Object o: v )
//		{
//			if( o == null )
//			{
//				o = "null";
//			}
//			if( o instanceof java.sql.Date )
//			{
//				o = D_F.format( o );
//			}
//			else if( o instanceof java.sql.Time )
//			{
//				o = T_F.format( o );
//			}
//			else if( o instanceof java.sql.Timestamp )
//			{
//				o = TS_F.format( o );
//			}
//			++shift;
//			i += o.hashCode() << shift;
//		}
//		return i;
		return toString().hashCode();
	}
	
	@Override
	public boolean equals( Object o )
	{
		boolean b = false;
		if( o != null && o instanceof PrimaryKeyValue )
		{
			PrimaryKeyValue o1 = (PrimaryKeyValue)o;
			b = o1.toString().equals( toString() );
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
			if( o instanceof java.sql.Date )
			{
				sb.append( PrimaryKeyValue.D_F.format( o ) );
			}
			else if( o instanceof java.sql.Time )
			{
				sb.append( PrimaryKeyValue.T_F.format( o ) );
			}
			else if( o instanceof java.sql.Timestamp )
			{
				sb.append( PrimaryKeyValue.TS_F.format( o ) );
			}
			else
			{
				sb.append( "" + o );
			}
		}
		return sb.toString();
	}

	public int compareTo(PrimaryKeyValue otherKey) {
		int c = 0;
		for( int i = 0; i < v.size(); ++i )
		{
			String s = "" + v.get( i );
			String s2 = "" + otherKey.v.get( i );
			c = s.compareTo( s2 );
			if( c != 0 )
			{
				break;
			}
		}
		return c;
	}
}
