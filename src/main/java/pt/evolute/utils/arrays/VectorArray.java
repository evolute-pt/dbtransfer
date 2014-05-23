package pt.evolute.utils.arrays;

import java.util.ArrayList;
import java.util.List;

public class VectorArray
{
/*	public static Vector arrayToVector( Object obj[] )
	{
		return new Vector( Arrays.asList( obj ) );
	}
	*/
	public static List<String> arrayToStringVector( Object obj[] )
	{
		List<String> v = new ArrayList<String>( obj.length );
		for( int i = 0; i < obj.length; i++ )
		{
			v.add( obj[i].toString() );
		}
		
		return v;
	}
	
	public static int []mergeArrays( int array[][] )
	{
		int total = 0;
		for( int i = 0; i < array.length; i++ )
		{
			total += array[i].length;
		}
		
		int a[] = new int[total];
		int current = 0;
		for( int i = 0; i < array.length; i++ )
		{
			for( int j = 0; j < array[i].length; j++ )
			{
				a[current++] = array[i][j];
			}
		}
		
		return a;
	}
	
	public static int []integerToIntArray( Object array[] )
	{
		int a[] = new int[array.length];
		for( int i = 0; i < a.length; i++ )
		{
			a[i] = (( Integer )array[i]).intValue();
		}
		
		return a;
	}
	
	public static String intArrayToList( int array[] )
	{
		if( array.length == 0 )
		{
			return "";
		}
		
		StringBuilder list = new StringBuilder();
		list.append( array[0] );
		
		for( int i = 1; i < array.length; i++ )
		{
			list.append( ", " );
			list.append( array[i] );
		}
		
		return list.toString();
	}
	
	public static String objectArrayToList( Object array[] )
	{
		return objectArrayToList( array, false );
	}
	
	public static String objectArrayToList( Object array[], boolean compact )
	{
		if( array.length == 0 )
		{
			return "";
		}
		
		StringBuilder list = new StringBuilder();
		list.append( array[0] );
		
		for( int i = 1; i < array.length; i++ )
		{
			list.append( compact ? "," : ", " );
			list.append( array[i] );
		}
		
		return list.toString();
	}
	
	public static String []objectArrayToStringArray( Object o[] )
	{
		String s[] = new String[o.length];
		for( int i = 0; i < o.length; i++ )
		{
			if( o[i] == null )
			{
				s[i] = null;
			}
			else
			{
				s[i] = o[i].toString();
			}
		}
		
		return s;
	}
	
	public static int []integerVectorToIntArray( List<Integer> v )
	{
		int a[] = new int[v.size()];
		
		for( int i = 0; i < v.size(); i++ )
		{
			a[i] = v.get( i ).intValue();
		}
		
		return a;
	}
	
	public static List<Integer> intArrayToIntegerVector( int a[] )
	{
		List<Integer> v = new ArrayList<Integer>( a.length );
		for( int i = 0; i < a.length; i++ )
		{
			v.add( new Integer( a[i] ) );
		}
		
		return v;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static List removeVectorNulls( List v )
	{
		List vv = new ArrayList();
		vv.addAll( v );
		
		int rc = 0;
		for( int i = 0; i < v.size(); i++ )
		{
			if( v.get( i ) == null )
			{
				vv.remove( i - rc );
				rc++;
			}
		}
		
		return vv;
	}
	
/*	public static Integer []integerVectorToIntegerArray( List<Integer> v )
	{
		return v.toArray( new Integer[ 0 ] );
	}*/
}
