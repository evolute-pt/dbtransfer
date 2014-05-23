/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pt.evolute.utils.arrays;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author lflores
 */
public class EvoArrays
{
	@SuppressWarnings("unchecked")
	public static <T> T[] sortAndDistinct( T[] a )
	{
		Arrays.sort( a );
		List<T> list = new LinkedList<T>();
		T old = null;
		boolean inited = false;
		for( T e: a )
		{
			if( inited )
			{
				if( old != e
					&& ( e == null || !e.equals( old ) ) )
				{
					list.add( e );
				}
			}
			else
			{
				list.add( e );
				inited = true;
			}
			old = e;
		}
		return list.toArray( ( T[] )Array.newInstance( old.getClass(), list.size() ) );
	}
}
