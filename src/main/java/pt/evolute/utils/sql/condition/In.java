package pt.evolute.utils.sql.condition;

import java.util.List;

import pt.evolute.utils.arrays.EvoArrays;
import pt.evolute.utils.arrays.IntArrayIntervalFinder;
import pt.evolute.utils.sql.Condition;
import pt.evolute.utils.sql.Operand;

public class In extends Condition
{
	private final static int OPTIMIZE_SIZE = 20;
//	private final static int BETWEEN_SIZE = 5;
//	private final static double GAP_PERCENT = 0.33;

	private final static String connector_string = "OR";
	private final static String between_prefix_string = "";
	private final static String in_string = "IN";

	private String leftStr = null;
	protected String symbolStr = null;
	private String rightStr = null;
	private boolean many = false;
	
	public In( Operand left, Operand right )
	{
		super( left, right );
		checkMany( left, right, getConnector() );
	}

	public String getConnector()
	{
		return connector_string;
	}

	public String getBetweenPrefix()
	{
		return between_prefix_string;
	}

	public String getInString()
	{
		return in_string;
	}

	private void checkMany( Operand left, Operand right, String expressionConnector )
	{
		if( right.isBatch() )
		{
			Object data[] = right.getInnerData();
			
			if( data instanceof Integer[] )
			{
				Integer ints[] = ( Integer[] )data;
				if( ints.length < OPTIMIZE_SIZE )
				{
					return;
				}
				ints = EvoArrays.sortAndDistinct( ints );
				
				List<Integer[]> intervals = IntArrayIntervalFinder.findIntervals( ints );

				if( intervals.size() <= 1 )
				{
					return;
				}
				many = true;
				ints = intervals.get( intervals.size() - 1 );
				intervals.remove( intervals.size() - 1 );
				if( ints.length > 0 )
				{
					Operand newRight = new Operand( ints );
					newRight.setBackend( getBackend() );
					rightStr = newRight.toString();
				}
				else
				{
					symbolStr = getBetweenPrefix() + "BETWEEN";
					ints = intervals.get( intervals.size() - 1 );
					intervals.remove( intervals.size() - 1 );
					Operand op = new Operand( ints[ 0 ] );
					op.setBackend( getBackend() );
					Operand op2 = new Operand( ints[ 1 ] );
					op2.setBackend( getBackend() );
					rightStr = op + " AND " + op2;
				}
				for( int n = 0; n < intervals.size(); n++ )
				{
					Between bet = new Between( left, new Operand( intervals.get( n ) ) );
					bet.setBackend( getBackend() );
					rightStr += " " + expressionConnector + " (" + getBetweenPrefix() + bet.toString() + ")";
				}
				rightStr += ")";
			}
		}
	}
	
	@Override
	public String getSymbol()
	{
		if( symbolStr == null )
		{
			return getInString();
		}
		else
		{
			return symbolStr;
		}
	}
	
	@Override
	public String getLeft()
	{
		if( leftStr == null )
		{
			leftStr = super.getLeft();
			if( many )
			{
				leftStr = "(" + leftStr;
			}
		}
		return leftStr;
	}
	
	@Override
	public String getRight()
	{
		if( rightStr == null )
		{
			rightStr = super.getRight();
		}
		return rightStr;
	}
}