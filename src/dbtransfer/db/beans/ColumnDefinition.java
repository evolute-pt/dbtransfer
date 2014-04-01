/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dbtransfer.db.beans;

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
	public Name referencedTable;
	public Name referencedColumn;
	public boolean isPrimaryKey;
	public String foreignKeyName;
}
