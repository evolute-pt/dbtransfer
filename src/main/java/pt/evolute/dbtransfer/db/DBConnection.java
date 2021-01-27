package pt.evolute.dbtransfer.db;

import java.sql.PreparedStatement;
import java.util.List;

import pt.evolute.dbtransfer.db.beans.ColumnDefinition;
import pt.evolute.dbtransfer.db.beans.ForeignKeyDefinition;
import pt.evolute.dbtransfer.db.beans.PrimaryKeyDefinition;
import pt.evolute.dbtransfer.db.beans.TableDefinition;
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
	public List<TableDefinition> getTableList()
			throws Exception;

	public List<ColumnDefinition> getColumnList(TableDefinition table)
			throws Exception;

	public PrimaryKeyDefinition getPrimaryKey(TableDefinition table)
			throws Exception;

	public List<ForeignKeyDefinition> getForeignKeyList(TableDefinition table)
			throws Exception;

	public Virtual2DArray getFullTable(TableDefinition table)
			throws Exception;

	public Virtual2DArray executeQuery(String sql)
			throws Exception;

	public PreparedStatement prepareStatement(String sql)
			throws Exception;
	
	public List<DBTable> getSortedTables()
		throws Exception;
	
	public List<UniqueDefinition> getUniqueList(TableDefinition table)
		throws Exception;
	
	public int getRowCount(TableDefinition table)
			throws Exception;
        
        public Helper getHelper();
}
