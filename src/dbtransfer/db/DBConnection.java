/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dbtransfer.db;

import java.sql.PreparedStatement;
import java.util.List;

import pt.evolute.arrays.Virtual2DArray;
import pt.evolute.dbmodel.DBTable;
import dbtransfer.db.beans.ColumnDefinition;
import dbtransfer.db.beans.ForeignKeyDefinition;
import dbtransfer.db.beans.Name;
import dbtransfer.db.beans.PrimaryKeyDefinition;
import dbtransfer.db.beans.UniqueDefinition;

/**
 *
 * @author lflores
 */
public interface DBConnection
{
	public List<Name> getTableList()
			throws Exception;

	public List<ColumnDefinition> getColumnList( Name table )
			throws Exception;

	public PrimaryKeyDefinition getPrimaryKey( Name table )
			throws Exception;

	public List<ForeignKeyDefinition> getForeignKeyList( Name table )
			throws Exception;

	public Virtual2DArray getFullTable( Name table )
			throws Exception;

	public Virtual2DArray executeQuery( String sql )
			throws Exception;

	public PreparedStatement prepareStatement( String sql )
			throws Exception;
	
	public List<DBTable> getSortedTables()
		throws Exception;
	
	public List<UniqueDefinition> getUniqueList( Name table )
		throws Exception;
	
	public int getRowCount( Name table )
			throws Exception;
}
