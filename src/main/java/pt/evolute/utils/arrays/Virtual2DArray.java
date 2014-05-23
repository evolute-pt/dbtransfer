package pt.evolute.utils.arrays;


public interface Virtual2DArray
{
	public <RETURN_TYPE extends Object> RETURN_TYPE get(int row, int col);
	
	public void set(int row, int col, Object obj);
	
	public int columnCount();
	
	public int rowCount();
	
	public Object[][] getObjects();
	
	public void deleteRow(int row);
	
	public void appendEmptyRow();
}
