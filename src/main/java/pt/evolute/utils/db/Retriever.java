package pt.evolute.utils.db;

import pt.evolute.utils.arrays.Virtual2DArray;

public interface Retriever
{
	public void setResults(Virtual2DArray results);
	
	public void setException(DBException ex);
}