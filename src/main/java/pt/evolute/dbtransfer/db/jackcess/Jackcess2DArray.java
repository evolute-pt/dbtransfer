package pt.evolute.dbtransfer.db.jackcess;

import java.util.List;
import java.util.Map;

import pt.evolute.utils.arrays.Virtual2DArray;

import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.Table;

/**
 *
 * @author lflores
 */
public class Jackcess2DArray implements Virtual2DArray
{
	private final Table table;
//	private final List<Column> columns;
	private final String columnNames[];
	private int currentRow = -1;
	private Map<String,Object> currentRowMap;
	

	public Jackcess2DArray( Table t )
	{
		table = t;
		List<Column> columns = table.getColumns();
		columnNames = new String[ columns.size() ];
		for( int i = 0; i < columns.size(); ++i )
		{
			columnNames[ i ] = columns.get( i ).getName();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <RETURN_TYPE extends Object> RETURN_TYPE get(int r, int c)
	{
		Object o = null;
		if( r == currentRow + 1 )
		{
			currentRow = r;
			try
			{
				currentRowMap = table.getNextRow();
			}
			catch( Exception ex )
			{
				ex.printStackTrace();
			}
		}
		if( r == currentRow )
		{
			o = currentRowMap.get( columnNames[ c ] );
		}
		else
		{
			System.out.println( "Invalid ROW: current: " + currentRow + " requested: " + r );
		}
		return ( RETURN_TYPE )o;
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
		return table.getColumnCount();
	}

	public int columnLength()
	{
		return rowCount();
	}

	public int rowCount()
	{
		return table.getRowCount();
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
