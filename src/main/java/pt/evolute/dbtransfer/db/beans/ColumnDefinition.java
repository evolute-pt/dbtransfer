package pt.evolute.dbtransfer.db.beans;

/**
 *
 * @author lflores
 */
public class ColumnDefinition
{
	public Name name;
	public String sqlTypeName;
	public int sqlType;
	public Integer sqlSize;
	public String defaultValue;
	public boolean isNotNull;
	public TableDefinition referencedTable;
	public ColumnDefinition referencedColumn;
	public boolean isPrimaryKey;
	public String foreignKeyName;
}
