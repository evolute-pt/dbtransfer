package pt.evolute.arrays;


public interface Virtual2DArray
{
	public <RETURN_TYPE extends Object> RETURN_TYPE get( int row, int col );
	
	public void set( int row, int col, Object obj );
	
	@Deprecated
	public int rowLength();
	
	public int columnCount();
	
	@Deprecated
	public int columnLength();
	
	public int rowCount();
	
	public Object[][] getObjects();
	
	public void deleteRow( int row );
	
	public void appendEmptyRow();
}