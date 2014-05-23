package pt.evolute.utils.sql;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import pt.evolute.utils.arrays.VectorArray;
import pt.evolute.utils.error.ErrorLogger;
import pt.evolute.utils.sql.backend.Backend;
import pt.evolute.utils.sql.backend.BackendProvider;
import pt.evolute.utils.sql.condition.Between;
import pt.evolute.utils.sql.condition.Different;
import pt.evolute.utils.sql.condition.Equal;
import pt.evolute.utils.sql.condition.Greater;
import pt.evolute.utils.sql.condition.GreaterOrEqual;
import pt.evolute.utils.sql.condition.ILike;
import pt.evolute.utils.sql.condition.In;
import pt.evolute.utils.sql.condition.Less;
import pt.evolute.utils.sql.condition.LessOrEqual;
import pt.evolute.utils.sql.condition.Like;
import pt.evolute.utils.sql.condition.NotILike;
import pt.evolute.utils.sql.condition.NotIn;
import pt.evolute.utils.sql.condition.NotLike;
import pt.evolute.utils.sql.expression.Atom;
import pt.evolute.utils.sql.function.SQLAlias;
import pt.evolute.utils.sql.function.SQLCount;
import pt.evolute.utils.sql.function.SQLDistinct;
import pt.evolute.utils.sql.function.SQLFunction;
import pt.evolute.utils.sql.function.SQLMax;
import pt.evolute.utils.sql.function.SQLMonth;
import pt.evolute.utils.sql.function.SQLYear;

public class Operand
{	
	private static final DateFormat D_F = new SimpleDateFormat( "yyyy-MM-dd" );
	private static final DateFormat DT_F = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss.SSS" );
	private static final DateFormat T_F = new SimpleDateFormat( "HH:mm:ss.SSS" );
	
	private Object iValue;
	
	private Object batchValue[] = null;
	
	private Backend backend = null;
	
	private boolean unicode = true;
	
	public Operand( Operand value )
	{
		if( value == null )
		{
			iValue = null;
		}
		else
		{
			iValue = value.iValue;
		}
	}
	
	public Operand( Object value )
	{
		if( value instanceof int[] )
		{
			value = VectorArray.intArrayToIntegerVector( 
					( int[] )value ).toArray( new Integer[ 0 ] );
		}
		iValue = value;
	}
	
	public Operand( int value )
	{
		this( new Integer( value ) );
	}
	
	public Operand( float value )
	{
		this( new Float( value ) );
	}
	
	public Operand( double value )
	{
		this( new Double( value ) );
	}
	
	public Operand( char value )
	{
		this( "" + value );
	}
	
	public Operand( boolean value )
	{
		this( new Boolean( value ) );
	}
	
	public boolean isNull()
	{
		return iValue == null;
	}
	
	public void setUnicode( boolean translate )
	{
		unicode = translate;
	}
	
	@Override
	public String toString()
	{
		if( iValue == null )
		{
			return "NULL";
		}
		if( iValue instanceof String )
		{
			String str = ( String )iValue;
			if( unicode )
			{
				if( backend != null )
				{
					str = backend.escapeUnicode( str ).toString();
				}
				else
				{
//					str = ( String )iValue;
					ErrorLogger.logException( new Exception( "No backend" ) );
				}
			}
			else
			{
				if( str.indexOf( '\'' ) != -1 )
				{
					str = str.replaceAll( "[\']", "\\\\'" );
				}
			}
			return "'" + str + "'";
		}
		if( iValue instanceof java.sql.Timestamp )
		{
			return "'" + DT_F.format( ( Date ) iValue ) + "'";
		}
		if( iValue instanceof java.sql.Time )
		{
			return "'" + T_F.format( ( Date ) iValue ) + "'";
		}
		if( iValue instanceof Date )
		{
			return "'" + D_F.format( ( Date ) iValue ) + "'";
		}
		if( iValue instanceof Boolean )
		{
/*			// TODO fix boolean on query!!!!
			return "'" + ( ( ( Boolean )iValue ).booleanValue() ? 'y' : 'n' ) + "'";*/
			boolean bool = ( ( Boolean )iValue ).booleanValue();
			return getBackend().getBoolean( bool );
		}
		if( iValue instanceof Collection<?> )
		{
			iValue = ((Collection<?>)iValue).toArray();
		}
		if( iValue instanceof Object[] )
		{
			Object values[] = ( Object [] ) iValue;
			String strs[] = new String[ values.length ];
			for( int n = 0; n < values.length; n++ )
			{
				Operand operand = new Operand( values[ n ] );
				operand.setBackend( getBackend() );
				strs[ n ] = operand.toString();
			}
			return "(" + VectorArray.objectArrayToList( strs, true ) + ")";
		}
		if( iValue instanceof Select )
		{
			(( Select )iValue).setBackend( backend );
			return "( " + iValue + " )";
		}
		return iValue.toString();
	}

	public Expression isEqual( Operand other )
	{
		if( other != null )
		{
			return new Atom( new Equal( this, other ) );
		}
		else
		{
			return new Atom( new Equal( this, new Operand( other ) ) );
		}
	}
	
	public Expression isEqual( Object other )
	{
		if( other instanceof Operand )
		{
			return isEqual( (Operand) other );
		}
		else
		{
			return isEqual( new Operand( other ) );
		}
	}
	
	public Expression isDifferent( Operand other )
	{
		if( other != null )
		{
			return new Atom( new Different( this, other ) );
		}
		else
		{
			return new Atom( new Different( this, new Operand( other ) ) );
		}
	}
	
	public Expression isDifferent( Object other )
	{
		if( other instanceof Operand )
		{
			return isDifferent( (Operand) other );
		}
		else
		{
			return isDifferent( new Operand( other ) );
		}
	}
	
	public Expression isGreater( Operand other )
	{
		return new Atom( new Greater( this, other ) );
	}
	
	public Expression isGreater( Object other )
	{
		if( other instanceof Operand )
		{
			return isGreater( (Operand) other );
		}
		else
		{
			return isGreater( new Operand( other ) );
		}
	}
	
	public Expression isGreaterOrEqual( Operand other )
	{
		return new Atom( new GreaterOrEqual( this, other ) );
	}
	
	public Expression isGreaterOrEqual( Object other )
	{
		if( other instanceof Operand )
		{
			return isGreaterOrEqual( (Operand) other );
		}
		else
		{
			return isGreaterOrEqual( new Operand( other ) );
		}
	}
	
	public Expression isLess( Operand other )
	{
		return new Atom( new Less( this, other ) );
	}
	
	public Expression isLess( Object other )
	{
		if( other instanceof Operand )
		{
			return isLess( (Operand) other );
		}
		else
		{
			return isLess( new Operand( other ) );
		}
	}
	
	public Expression isLessOrEqual( Operand other )
	{
		return new Atom( new LessOrEqual( this, other ) );
	}
	
	public Expression isLessOrEqual( Object other )
	{
		if( other instanceof Operand )
		{
			return isLessOrEqual( (Operand) other );
		}
		else
		{
			return isLessOrEqual( new Operand( other ) );
		}
	}
	
	public Expression in( Operand other )
	{
		return new Atom( new In( this, other ) );
	}
	
	public Expression in( Object other )
	{
		if( other instanceof Operand )
		{
			return in( (Operand) other );
		}
		else
		{
			return in( new Operand( other ) );
		}
	}

	public Expression notIn( Operand other )
	{
		return new Atom( new NotIn( this, other ) );
	}
	
	public Expression notIn( Object other )
	{
		if( other instanceof Operand )
		{
			return notIn( (Operand) other );
		}
		else
		{
			return notIn( new Operand( other ) );
		}
	}
	
	public Expression between( Operand other )
	{
		return new Atom( new Between( this, other ) );
	}
	
	public Expression between( Object other )
	{
		if( other instanceof Operand )
		{
			return between( (Operand) other );
		}
		else
		{
			return between( new Operand( other ) );
		}
	}
	
	public Expression isLike( Object other )
	{
		if( other instanceof Operand )
		{
			return new Atom( new Like( this, ( Operand )other ) );
		}
		else
		{
			return isLike( new Operand( other ) );
		}
	}
	
	public Expression isNotLike( Object other )
	{
		if ( other instanceof Operand )
		{
			return new Atom( new NotLike( this, ( Operand ) other ) );
		}
		else
		{
			return isNotLike( new Operand( other ) );
		}
	}
	
	public Expression isILike( Object other )
	{
		if( other instanceof Operand )
		{
			return new Atom( new ILike( this, ( Operand )other ) );
		}
		else
		{
			return isILike( new Operand( other ) );
		}
	}
	
	public Expression isNotILike( Object other )
	{
		if ( other instanceof Operand )
		{
			return new Atom( new NotILike( this, ( Operand ) other ) );
		}
		else
		{
			return isNotILike( new Operand( other ) );
		}
	}
	
	public boolean isBatch()
	{
		if( iValue instanceof Collection<?> )
		{
			iValue = ((Collection<?>) iValue).toArray();
		}
		if( iValue instanceof Object[] )
		{
			batchValue = ( Object[] )iValue;
			return true;
		}
		return batchValue != null;
	}
	
	public int getBatchSize()
	{
		if( batchValue == null )
		{
			batchValue = ( Object[] )iValue;
		}
		return batchValue.length;
	}
	
	public void currentBatch( int index )
	{
		if( batchValue == null )
		{
			batchValue = ( Object[] )iValue;
		}
		iValue = batchValue[ index ];
	}
	
	public Object[] getInnerData()
	{
		if( batchValue == null )
		{
			batchValue = ( Object[] )iValue;
		}
		return batchValue;
	}
	
	public SQLFunction max()
	{
		return new SQLMax( this );
	}
	
	public SQLFunction year()
	{
		return new SQLYear( this );
	}
	
	public SQLFunction month()
	{
		return new SQLMonth( this );
	}
	
	public SQLFunction alias( String alias )
	{
		return new SQLAlias( this, alias );
	}
	
	public SQLFunction count()
	{
		return new SQLCount( this );
	}
	
	public SQLFunction distinct()
	{
		return new SQLDistinct( this );
	}
	
	public String toHeaderString()
	{
		return toString();
	}

	protected Object getInnerObject()
	{
		return iValue;
	}
	
	public void setBackend( Backend backend )
	{
		this.backend = backend;
	}
	
	protected Backend getBackend()
	{
		if( backend == null )
		{
//			new Exception( "NO BACKEND!!!!" ).printStackTrace( System.out );
			setBackend( BackendProvider.getDefaultBackend() );
		}
		return backend;
	}
	
/*	public static void main( String arg[] )
	{
		System.out.println( "rep: " + "ola'ole".replaceAll( "[\']", "\\\\'" ) );
	}*/
}
