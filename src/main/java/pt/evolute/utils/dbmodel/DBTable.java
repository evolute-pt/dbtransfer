package pt.evolute.utils.dbmodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import pt.evolute.dbtransfer.Config;
import pt.evolute.utils.object.DefaultLightPropertyObject;
import pt.evolute.utils.tables.ColumnizedObject;

/**
 *
 * @author lflores
 */

public class DBTable extends DefaultLightPropertyObject 
		implements ColumnizedObject, Cloneable
{
	public static final String NAME = "NAME";
	private final ModelProvider provider;
	protected List<DBReference> importedReferences = null;
	protected List<DBReference> exportedReferences = null;
	protected List<DBHierarchy> hierarchys = null;
	protected List<DBTable> components = null;
	protected List<DBColumn> columns = null;
	protected List<DBColumn> primaryKey = null;

	/** Creates a new instance of DBTable */
	public DBTable( ModelProvider provider )
	{
		super( new String[] { NAME }, false );
		this.provider = provider;
	}

	public List<DBReference> getImportedForeignKeys()
		throws Exception
	{
		if( importedReferences == null )
		{
			importedReferences = Collections.unmodifiableList( provider.getImportedKeysForTable( this ) );
		}
		return importedReferences;
	}

	public List<DBReference> getExportedForeignKeys()
		throws Exception
	{
		if( exportedReferences == null )
		{
			exportedReferences = Collections.unmodifiableList( provider.getExportedKeysForTable( this ) );
		}
		return exportedReferences;
	}

	public List<DBColumn> getPrimaryKey()
		throws Exception
	{
		if( primaryKey == null )
		{
			primaryKey = Collections.unmodifiableList( provider.getPrimaryKeyForTable( this ) );
		}
		return primaryKey;
	}

	@SuppressWarnings("unchecked")
	public Object getValue( int col )
	{
		return get( getPropertyNames()[ col ] );
	}

	public List<DBHierarchy> getHierarchys()
		throws Exception
	{
		if( hierarchys == null )
		{
			hierarchys = Collections.unmodifiableList( provider.getHierarchysForTable( this ) );
		}
		return hierarchys;
	}

	public List<DBColumn> getColumns()
		throws Exception
	{
		if( columns == null )
		{
			columns = Collections.unmodifiableList( provider.getColumnsForTable( this ) );
		}
		return columns;
	}
	
	public List<DBColumn> getColumnsNoPK()
		throws Exception
	{
		Map<DBColumn,DBColumn> pk = new HashMap<DBColumn,DBColumn>();
		for( DBColumn c: getPrimaryKey() )
		{
			pk.put( c, c );
		}
		List<DBColumn> cols = new LinkedList<DBColumn>();
		for( DBColumn c: getColumns() )
		{
			if( !pk.containsKey( c ) )
			{
				cols.add( c );
			}
		}
		return cols;
	}

	public int getNumberColumns()
		throws Exception
	{
		return getColumns().size();
	}

	public List<DBTable> getComponents()
		throws Exception
	{
		if( components == null )
		{
			getHierarchys();
			components = new ArrayList<DBTable>();
			for( DBHierarchy hierarchy: hierarchys )
			{
				DBTable table = hierarchy.getTables()[ 1 ];
				if( !components.contains( table ) )
				{
					components.add( table );
				}
			}
			components = Collections.unmodifiableList( components );
		}
		return components;
	}
	
	@Override
	public Object clone()
	{
		DBTable table = new DBTable( provider );
		table.setMapData( getMapData() );
		table.importedReferences = importedReferences;
		table.exportedReferences = exportedReferences;
		table.hierarchys = hierarchys;
		table.components = components;
		return table;
	}
	
	@Override
	public String toString()
	{
		return ( String ) get( NAME );
	}
	
	public String getSaneName()
	{
		return ( ( String ) get( NAME ) ).toLowerCase();
	}
	
	public String getDestinationName()
	{
		return Config.getDestinationTablePrefix() + toString();
	}
	
	@Override
    public boolean equals( Object o )
    {
    	boolean eq = o instanceof DBTable;
    	if( eq )
    	{
    		eq = toString().equals( ((DBTable)o).toString() );
    	}
        return eq;
    }

    @Override
    public int hashCode() 
    {
        int hash = 3;
        hash = 53 * ( hash + (this.toString() != null ? this.toString().hashCode() : 0) );
        return hash;
    }
}

