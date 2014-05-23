package pt.evolute.utils.dbmodel;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

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
			importedReferences = provider.getImportedKeysForTable( this );
/*			if( importedReferences.size() > 0 )
			{
				System.out.println( "IMPORTED FOREIGN KEY FOR: " + getField( NAME ) );
				for( DBReference ref: importedReferences )
				{
					System.out.println( "Ref: " + ref.getField( DBReference.NAME ) + " SRC: " 
					+ ref.getField( DBReference.SRC_TABLE ) + "." + ref.getField( DBReference.SRC_COLUMN ) 
					+ " DST: " + ref.getField( DBReference.DEST_TABLE ) );
				}
			}*/
		}
		return importedReferences;
	}

	public List<DBReference> getExportedForeignKeys()
		throws Exception
	{
		if( exportedReferences == null )
		{
			exportedReferences = provider.getExportedKeysForTable( this );
/*			if( exportedReferences.size() > 0 )
			{
				System.out.println( "EXPORTED FOREIGN KEY FOR: " + getField( NAME ) );
				for( DBReference ref: exportedReferences )
				{
					System.out.println( "Ref: " + ref.getField( DBReference.NAME ) + " SRC: " 
					+ ref.getField( DBReference.SRC_TABLE ) + "." + ref.getField( DBReference.SRC_COLUMN ) 
					+ " DST: " + ref.getField( DBReference.DEST_TABLE ) );
				}
			}*/
		}
		return exportedReferences;
	}

	public List<DBColumn> getPrimaryKey()
		throws Exception
	{
		if( primaryKey == null )
		{
			primaryKey = provider.getPrimaryKeyForTable( this );
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
			hierarchys = provider.getHierarchysForTable( this );
		}
		return hierarchys;
	}

	public List<DBColumn> getColumns()
		throws Exception
	{
		if( columns == null )
		{
			columns = provider.getColumnsForTable( this );
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
			components = new Vector<DBTable>();
			for( DBHierarchy hierarchy: hierarchys )
			{
				DBTable table = hierarchy.getTables()[ 1 ];
				if( !components.contains( table ) )
				{
					components.add( table );
				}
			}
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
		return ( String ) get( NAME );// + " - " + hashCode();
	}
}

