package pt.evolute.utils.sql;

import pt.evolute.utils.arrays.Virtual2DArray;
import pt.evolute.utils.db.ExecuterProvider;
import pt.evolute.utils.sql.backend.Backend;

public interface SQLQuery
{
	public void execute()
		throws Exception;

	public void execute(ExecuterProvider provider)
		throws Exception;
	
	public Object[][] getObjects();
	
	public Virtual2DArray getCursorObjects();
	
	public void setBackend(Backend backend);
}