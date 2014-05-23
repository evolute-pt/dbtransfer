/*
 * ArrayIntervalFinder.java
 *
 * Created on 21 de Marco de 2005, 17:49
 */

package pt.evolute.utils.arrays;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author  fpalma
 */
public class IntArrayIntervalFinder
{
	public static final int DEFAULT_MIN_INTERVAL = 4;
	
//	public static Vector find( int array[] )
//	{
//		
//	}
	
	public static List<Integer[]> findIntervals( Integer array[] )
	{
		return findIntervals( array, DEFAULT_MIN_INTERVAL );
	}
	
	public static List<Integer[]> findIntervals( Integer array[], int minInterval )
	{
		List<Integer[]> v = new LinkedList<Integer[]>();
		List<Integer> isolated = new LinkedList<Integer>();
		if( array == null || array.length == 0 )
		{
			return v;
		}
		Integer last = null;
		int count = 0;
		for( int n = 0; n < array.length; n++ )
		{
			if( last == null )
			{
				last = array[ n ];
				count = 1;
				continue;
			}
			if( array[ n ].intValue() == array[ n - 1 ].intValue() + 1 )
			{
				count++;
			}
			else if( count == 1 )
			{
				isolated.add( last );
				last = array[ n ];
			}
			else if( count < minInterval )
			{
				for( int i = 0; i < count; i++ )
				{
					isolated.add( new Integer( last.intValue() + i ) );
				}
				count = 1;
				last = array[ n ];
			}
			else
			{
				v.add( new Integer[]{ last, array[ n - 1 ] } );
				count = 1;
				last = array[ n ];
			}
		}
		if( count == 1 )
		{
			isolated.add( last );
		}
		else if( count < minInterval )
		{
			for( int i = 0; i < count; i++ )
			{
				isolated.add( new Integer( last.intValue() + i ) );
			}
		}
		else
		{
			v.add( new Integer[]{ last, array[ array.length - 1 ] } );
		}
//		if( isolated.size() > 0 )
//		{
		v.add( isolated.toArray( new Integer[ isolated.size() ] ) );
//		}
		return v;
	}
}
