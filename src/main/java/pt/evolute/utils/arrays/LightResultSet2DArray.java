package pt.evolute.utils.arrays;

import java.sql.ResultSet;
import java.sql.SQLException;

import pt.evolute.utils.error.ErrorLogger;

public class LightResultSet2DArray implements Virtual2DArray
{
	private final ResultSet results;
	
	private final int columns;
	private final Object currentRowData[];
	
	private int rows = -1;
	
	private int currentRow = -1;
	
	
	public LightResultSet2DArray( ResultSet rs )
		throws SQLException
	{
		results = rs;
		columns = results.getMetaData().getColumnCount();
		currentRowData = new Object[ columns ];
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <RETURN_TYPE extends Object> RETURN_TYPE get(int row, int col)
	{
		RETURN_TYPE obj = null;
		if( row == currentRow )
		{
			obj = (RETURN_TYPE)currentRowData[ col ];
		}
		else
		{
			currentRow = row;
			try
			{
				results.absolute( row + 1 );
				for( int i = 0; i < columns; ++i )
				{
					currentRowData[ i ] = results.getObject( i + 1 );					
				}
				obj = (RETURN_TYPE)currentRowData[ col ];
				if( row + 1 == rows )
				{
					System.out.println( "\nClosing ResultSet (" + row + ")" );
					results.getStatement().close();
				}
			}
			catch( SQLException ex )
			{
				ex.printStackTrace( System.out );
				ErrorLogger.logException( ex );
			}
		}
		return obj;
	}
        
	@Override
	public int columnCount()
	{
		return columns;
	}
	
	@Override
	public int rowCount()
	{
		if( rows == -1 )
		{
			try
			{
				results.last();
				rows = results.getRow();
			}
			catch( SQLException ex )
			{
				ErrorLogger.logException( ex );
			}
		}
		return rows;
	}


	@Override
	public Object[][] getObjects()
	{
		int cols = columnCount();
		int rows = rowCount();
		Object o[][] = new Object[ rows ][];
		for( int i = 0; i < rows; ++i )
		{
			o[ i ] = new Object[ cols ];
			for( int j = 0; j < cols; ++j )
			{
				o[ i ][ j ] = get( i, j );
			}
		}
		return o;
	}
}
