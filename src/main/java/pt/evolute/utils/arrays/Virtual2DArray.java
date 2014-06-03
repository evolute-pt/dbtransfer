package pt.evolute.utils.arrays;


public interface Virtual2DArray
{
	public <RETURN_TYPE extends Object> RETURN_TYPE get(int row, int col);
		
	public int columnCount();
	
	public int rowCount();
	
	public Object[][] getObjects();
}
