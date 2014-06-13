package pt.evolute.dbtransfer.db;

import java.sql.PreparedStatement;
import java.util.List;
import pt.evolute.dbtransfer.db.beans.ColumnDefinition;
import pt.evolute.dbtransfer.db.beans.ForeignKeyDefinition;
import pt.evolute.dbtransfer.db.beans.Name;
import pt.evolute.dbtransfer.db.beans.PrimaryKeyDefinition;
import pt.evolute.dbtransfer.db.beans.UniqueDefinition;
import pt.evolute.dbtransfer.db.helper.Helper;
import pt.evolute.utils.arrays.Virtual2DArray;
import pt.evolute.utils.dbmodel.DBTable;

/**
 *
 * @author lflores
 */
public interface DBConnection
{
	public List<Name> getTableList()
			throws Exception;

	public List<ColumnDefinition> getColumnList(Name table)
			throws Exception;

	public PrimaryKeyDefinition getPrimaryKey(Name table)
			throws Exception;

	public List<ForeignKeyDefinition> getForeignKeyList(Name table)
			throws Exception;

	public Virtual2DArray getFullTable(Name table)
			throws Exception;

	public Virtual2DArray executeQuery(String sql)
			throws Exception;

	public PreparedStatement prepareStatement(String sql)
			throws Exception;
	
	public List<DBTable> getSortedTables()
		throws Exception;
	
	public List<UniqueDefinition> getUniqueList(Name table)
		throws Exception;
	
	public int getRowCount(Name table)
			throws Exception;
        
        public Helper getHelper();
}
