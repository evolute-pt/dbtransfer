package pt.evolute.db;

import pt.evolute.arrays.Virtual2DArray;

public interface Retriever
{
	public void setResults( Virtual2DArray results );
	
	public void setException( DBException ex );
}