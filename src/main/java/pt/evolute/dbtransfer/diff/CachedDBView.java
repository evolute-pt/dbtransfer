package pt.evolute.dbtransfer.diff;

import java.util.HashMap;
import java.util.Map;

import pt.evolute.dbtransfer.db.DBConnection;
import pt.evolute.dbtransfer.db.PrimaryKeyValue;
import pt.evolute.utils.dbmodel.DBTable;

public class CachedDBView {
	private final int ROWS;
//	private final DBTable TABLE;
//	private final DBConnection CONNECTION;
//	private final Boolean GET_ACTION;
	private final Map<PrimaryKeyValue,TableRow> MAP = new HashMap<PrimaryKeyValue,TableRow>();
	
	private PrimaryKeyValue minLoadedKey = null;
	private PrimaryKeyValue maxLoadedKey = null;
	
	private boolean loadedAll = false;
	
	public CachedDBView( int rows, DBTable table, DBConnection conn, boolean getAction )
	{
		ROWS = rows;
//		TABLE = table;
//		CONNECTION = conn;
//		GET_ACTION = getAction;
	}
	
	public TableRow get( PrimaryKeyValue key )
	{
		if( !loadedAll 
				&& ( key.compareTo( minLoadedKey ) < 0
				|| key.compareTo( maxLoadedKey ) > 0 ) )
		{
			loadRows();
		}
		TableRow row = MAP.get( key );
		if( row == null )
		{
			
		}
		return row;
	}

	private void loadRows() {
		MAP.clear();
		
		
		
		if( MAP.size() != ROWS )
		{
			loadedAll = true;
		}
	}
}
