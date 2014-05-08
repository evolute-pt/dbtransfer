package pt.evolute.arrays;

import java.sql.ResultSet;
import java.sql.SQLException;

import pt.evolute.error.ErrorLogger;

public class CursorResultSet2DArray implements Virtual2DArray
{
	private final ResultSet results;
	
	private final int columns;
	private final Object currentRowData[];
	
	private int rows = -1;
	
	private int currentRow = -1;
	
	
	public CursorResultSet2DArray( ResultSet rs )
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
                        if( currentRow + 1 == row )
                        {
                            currentRow = row;
                        
                            try
                            {
                               if(  results.next() )
                               {
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
                               else
                               {
                               rows = currentRow;
                                throw new RuntimeException( "ENDOFRESULTSET" );
                               }
                            }
                            catch( SQLException ex )
                            {
                                    ex.printStackTrace( System.out );
                                    ErrorLogger.logException( ex );
                            }
                        }
                        else
                        {
                        throw new RuntimeException( "invalid index " + currentRow + " get: " + row );
                        }
		}
		return obj;
	}

	@Override
	public void set(int row, int col, Object obj)
	{
		throw new RuntimeException( "Not implemented yet" );
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
                throw new RuntimeException( "not supported" );
            }
            return rows;
	}


	@Override
	public Object[][] getObjects()
	{
		throw new RuntimeException( "Not implemented yet" );
	}

	@Override
	public void deleteRow(int row)
	{
		throw new RuntimeException( "Not implemented yet" );
	}

	@Override
	public void appendEmptyRow()
	{
		throw new RuntimeException( "Not implemented yet" );
	}

}
