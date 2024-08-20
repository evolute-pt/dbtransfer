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
	
    @Override
	public String toString()
	{
		return name.saneName;
	}
        
    @Override
    public boolean equals( Object o )
    {
    	boolean eq = o instanceof ColumnDefinition;
    	if( eq )
    	{
    		eq = name.originalName.equals( ((ColumnDefinition)o).name.originalName );
    	}
        return eq;
    }

    @Override
    public int hashCode() 
    {
        int hash = 3;
        hash = 53 * ( hash + (this.name.originalName != null ? this.name.originalName.hashCode() : 0) );
        return hash;
    }
}
