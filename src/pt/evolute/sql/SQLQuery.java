package pt.evolute.sql;

import pt.evolute.arrays.Virtual2DArray;
import pt.evolute.db.ExecuterProvider;
import pt.evolute.sql.backend.Backend;

public interface SQLQuery
{
	public void execute()
		throws Exception;

	public void execute( ExecuterProvider provider )
		throws Exception;
	
	public Object[][] getObjects();
	
	public Virtual2DArray getCursorObjects();
	
	public void setBackend( Backend backend );
}