package pt.evolute.dbtransfer.db.beans;

public class TableDefinition extends Name
{
	public TableDefinition( String original )
	{
		super( original );
	}

    @Override
	public String toString()
	{
		return saneName;
	}
        
    @Override
    public boolean equals( Object o )
    {
    	boolean eq = o instanceof TableDefinition;
    	if( eq )
    	{
    		eq = originalName.equals( ((TableDefinition)o).originalName );
    	}
        return eq;
    }

    @Override
    public int hashCode() 
    {
        int hash = 3;
        hash = 53 * ( hash + (this.originalName != null ? this.originalName.hashCode() : 0) );
        return hash;
    }
}
