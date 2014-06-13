package pt.evolute.utils.arrays;

import java.sql.ResultSet;
import java.sql.SQLException;
import pt.evolute.utils.arrays.exception.EndOfArrayException;

import pt.evolute.utils.error.ErrorLogger;

public class CursorResultSet2DArray implements Virtual2DArray
{
    private final ResultSet results;

    private final int columns;
    private final Object currentRowData[];

    private int rows = -1;

    private int currentRow = -1;
    private boolean validRow;

    public CursorResultSet2DArray( ResultSet rs )
            throws SQLException
    {
        results = rs;
        columns = results.getMetaData().getColumnCount();
        currentRowData = new Object[ columns ];
        validRow = results.next();
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
                    if( validRow )
                    {
                        for( int i = 0; i < columns; ++i )
                        {
                                currentRowData[ i ] = results.getObject( i + 1 );					
                        }
                        obj = (RETURN_TYPE)currentRowData[ col ];
                        if( !results.next() )
                        {
                            rows = currentRow;
                            validRow = false;
             //                   System.out.println( "\nClosing ResultSet (" + row + ")" );
                            results.getStatement().close();
                        }
                    }
                    else
                    {
                        rows = currentRow;
                        throw new EndOfArrayException();
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
}
