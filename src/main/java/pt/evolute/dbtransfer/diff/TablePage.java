package pt.evolute.dbtransfer.diff;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import pt.evolute.dbtransfer.db.PrimaryKeyValue;

public class TablePage 
{
	private PrimaryKeyValue pkvFirst = null;
	private PrimaryKeyValue pkvLast = null;
	private final Map<PrimaryKeyValue,TableRow> map = new HashMap<PrimaryKeyValue,TableRow>();
	private final List<PrimaryKeyValue> keys = new LinkedList<PrimaryKeyValue>();
	
	public void put( PrimaryKeyValue pkv, TableRow row )
	{
		if( pkvFirst == null )
		{
			pkvFirst = pkv;
		}
		map.put( pkv, row );
		pkvLast = pkv;
		keys.add( pkv );
	}
	
	public TableRow get( PrimaryKeyValue pkv )
	{
		return map.get( pkv );
	}
	
	public PrimaryKeyValue getFirst()
	{
		return pkvFirst;
	}
	
	public PrimaryKeyValue getLast()
	{
		return pkvLast;
	}
	
	public List<PrimaryKeyValue> allKeysOrdered()
	{
		return keys;
	}
	
	public Set<PrimaryKeyValue> keySet()
	{
		return map.keySet();
	}
	
	public boolean isPrimaryKeyValueInsidePage( PrimaryKeyValue pkv )
	{
//		System.out.println( "pkvFirst: " + pkvFirst + " pkvLast: " + pkvLast + " pkv? " + pkv + " inside: " + ( pkvFirst != null && pkvFirst.compareTo( pkv ) <= 0 && pkvLast.compareTo( pkv ) >= 0 ) );
		return pkvFirst != null && pkvFirst.compareTo( pkv ) <= 0 && pkvLast.compareTo( pkv ) >= 0;
	}
	
	public int size()
	{
		return keys.size();
	}
	
	public boolean containsKey( PrimaryKeyValue pkv )
	{
		return map.containsKey( pkv );
	}
	
	public TableRow remove( PrimaryKeyValue pkv )
	{
		return map.remove( pkv );
	}
}
