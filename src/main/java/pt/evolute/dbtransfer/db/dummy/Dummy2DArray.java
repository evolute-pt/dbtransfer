package pt.evolute.dbtransfer.db.dummy;

import pt.evolute.utils.arrays.Virtual2DArray;
import pt.evolute.utils.arrays.exception.EndOfArrayException;

/**
 *
 * @author lflores
 */
public class Dummy2DArray implements Virtual2DArray
{
	private final int ROWS;
	private final String COLUMNS[];

	public Dummy2DArray( int rows, String cols[] )
	{
		ROWS = rows;
		COLUMNS = cols;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Long get(int r, int c)
	{
		if( r >= ROWS )
		{
			throw new EndOfArrayException();
		}
		return ( long )r;
	}

	public void set(int i, int i1, Object o)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public int rowLength()
	{
		return columnCount();
	}
	
	public int columnCount()
	{
		return COLUMNS.length;
	}

	public int columnLength()
	{
		return rowCount();
	}

	public int rowCount()
	{
		return ROWS;
	}
	
	public Object[][] getObjects()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void deleteRow(int i)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void appendEmptyRow()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
}
