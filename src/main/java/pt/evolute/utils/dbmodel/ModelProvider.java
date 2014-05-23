package pt.evolute.utils.dbmodel;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;

import pt.evolute.utils.db.ColumnMetadataConstants;
import pt.evolute.utils.db.Connector;
import pt.evolute.utils.db.ForeignKeyMetadataConstants;
import pt.evolute.utils.db.PrimaryKeyMetadataConstants;

/**
 *
 * @author fpalma
 */
public class ModelProvider extends Connector
{
	protected Connection connection;
	protected DatabaseMetaData metadata;
	protected String catalog;
	protected String schema;
	private static ModelProvider provider = null;
	
	private final Map<String,DBTable> TABLE_CACHE = new HashMap<String,DBTable>();
	private final Map<String,List<DBReference>> EXPORTED_KEY_CACHE = new HashMap<String,List<DBReference>>();
	private final Map<String,List<DBReference>> IMPORTED_KEY_CACHE = new HashMap<String,List<DBReference>>();
	private final Map<String,List<DBColumn>> PRIMARY_KEY_CACHE = new HashMap<String,List<DBColumn>>();
	private final Map<String,List<DBColumn>> COLUMN_CACHE = new HashMap<String,List<DBColumn>>();
	private final Map<String,HashMap<String,DBColumn>> COLUMN_BY_NAME_CACHE = new HashMap<String,HashMap<String,DBColumn>>();
	
	/** Creates a new instance of ModelProvider */
	public ModelProvider( Connection connection )
		throws Exception
	{
		this.connection = connection;
		metadata = connection.getMetaData();
		catalog = getCatalog( metadata.getURL() );
		schema = getSchema( metadata.getURL() );
	}
	
	public static ModelProvider getProvider( Connection connection )
		throws Exception
	{
		if( provider == null )
		{
			provider = new ModelProvider( connection );
		}
		else
		{
			provider.TABLE_CACHE.clear();
			provider.EXPORTED_KEY_CACHE.clear();
			provider.IMPORTED_KEY_CACHE.clear();
			provider.PRIMARY_KEY_CACHE.clear();
			provider.COLUMN_CACHE.clear();
			provider.COLUMN_BY_NAME_CACHE.clear();
		}
		return provider;
	}
	
	public void readFullMetadata()
		throws Exception
	{
		List<DBTable> tables = getTables();
		for( int t = 0; t < tables.size(); t++ )
		{
			getColumnsForTable( tables.get( t ) );
			getPrimaryKeyForTable( tables.get( t ) );
			getImportedKeysForTable( tables.get( t ) );
			getExportedKeysForTable( tables.get( t ) );
		}
	}

	public List<DBTable> getTables()
		throws Exception
	{
		if( TABLE_CACHE.size() == 0 )
		{
			List<DBTable> tables = new Vector<DBTable>();
			ResultSet rs = metadata.getTables( catalog, schema, null, new String[] { "TABLE" } );
			while( rs.next() )
			{
				DBTable dbt = new DBTable( this );
				String table = rs.getString( 3 );
				dbt.set( DBTable.NAME, table );
				TABLE_CACHE.put( table, dbt );
				tables.add( dbt );
			}
			rs.close();
		}
		List<DBTable> tables = new Vector<DBTable>();
		tables.addAll( TABLE_CACHE.values() );
		return tables;
	}
	
	public List<DBColumn> getColumnsForTable( DBTable table )
		throws Exception
	{
		String tableName = ( String )table.get( DBTable.NAME );
		if( !COLUMN_CACHE.containsKey( tableName ) )
		{
			
			COLUMN_BY_NAME_CACHE.put( tableName, new HashMap<String,DBColumn>() );
			ResultSet rs = metadata.getColumns( catalog, schema, (String) table.get( DBTable.NAME ), null );
			List<DBColumn> columns = new Vector<DBColumn>();
			while( rs.next() )
			{
				String name = rs.getString( ColumnMetadataConstants.COLUMN_NAME );
				int sqlDataType = rs.getInt( ColumnMetadataConstants.DATA_TYPE );
				String typeName = rs.getString( ColumnMetadataConstants.TYPE_NAME );
				int length = rs.getInt( ColumnMetadataConstants.COLUMN_SIZE );
				DBColumn column = new DBColumn();
				column.set( DBColumn.TABLE, table );
				column.set( DBColumn.NAME, name );
				column.set( DBColumn.TYPE, typeName );
				column.set( DBColumn.TYPE_ID, new Integer( sqlDataType ) );
				column.set( DBColumn.LENGTH, new Integer( length ) );
				columns.add( column );
				COLUMN_BY_NAME_CACHE.get( tableName ).put( name, column );
			}
			COLUMN_CACHE.put( tableName, columns );
		}
		return COLUMN_CACHE.get( tableName );
	}
	
	public DBColumn getColumnByTableAndName( DBTable table, String name )
		throws Exception
	{
		getColumnsForTable( table );
		return COLUMN_BY_NAME_CACHE.get( table.get( DBTable.NAME ) ).get( name );
	}

	public List<DBColumn> getPrimaryKeyForTable( DBTable table )
		throws Exception
	{
		String tableName = ( String ) table.get( DBTable.NAME );
		if(!PRIMARY_KEY_CACHE.containsKey( tableName ) )
		{
			ResultSet rs = metadata.getPrimaryKeys( null, schema, ( String ) table.get( DBTable.NAME ) );
			List<int[]> seqs = new Vector<int[]>();
			List<String> names = new Vector<String>();
			for( int n = 0; rs.next(); n++ )
			{
				String name = rs.getString( PrimaryKeyMetadataConstants.COLUMN_NAME );
				int seq = rs.getInt( PrimaryKeyMetadataConstants.KEY_SEQ );
				names.add( name );
				seqs.add( new int[]{ seq, n } );
				
			}
			Collections.sort( seqs, new Comparator<int[]>(){
					public int compare( int o1[], int o2[] )
					{
						if( o1[ 0 ] == o2[ 0 ] )
						{
							return 0;
						}
						else if( o1[ 0 ] > o2[ 0 ] )
						{
							return 1;
						}
						else
						{
							return -1;
						}
					}
				} );
			List<DBColumn> columns = new Vector<DBColumn>();
			for( int n = 0; n < seqs.size(); n++ )
			{
				DBColumn column = getColumnByTableAndName( table, names.get( seqs.get( n )[ 1 ] ) );
				column.set( DBColumn.IS_PRIMARY_KEY, "y" );
				columns.add( column );
			}
			PRIMARY_KEY_CACHE.put( tableName, columns );
		}
		return PRIMARY_KEY_CACHE.get( tableName );
	}

	public DBTable getTableByName( String tableName )
		throws Exception
	{
		getTables();
		return TABLE_CACHE.get( tableName );
	}
	
	protected List<DBReference> readForeignKeys( ResultSet rs )
		throws Exception
	{
		HashMap<String,String[]> tableNames = new HashMap<String,String[]>();
		HashMap<String,List<String>> columnNames = new HashMap<String,List<String>>();
		HashMap<String,List<String>> destColumnNames = new HashMap<String,List<String>>();
		HashMap<String,List<int[]>> seqs = new HashMap<String,List<int[]>>();
		while( rs.next() )
		{

			String reference = rs.getString( ForeignKeyMetadataConstants.FK_NAME );
			String srcTable = rs.getString( ForeignKeyMetadataConstants.FKTABLE_NAME );
			String srcColumn = rs.getString( ForeignKeyMetadataConstants.FKCOLUMN_NAME );
			String destTable = rs.getString( ForeignKeyMetadataConstants.PKTABLE_NAME );
			String destColumn = rs.getString( ForeignKeyMetadataConstants.PKCOLUMN_NAME );
			int seq = rs.getInt( ForeignKeyMetadataConstants.KEY_SEQ );
			if( !tableNames.containsKey( reference ) )
			{
				tableNames.put( reference, new String[]{ srcTable, destTable } );
				columnNames.put( reference, new Vector<String>() );
				destColumnNames.put( reference, new Vector<String>() );
				seqs.put( reference, new Vector<int[]>() );
			}
			columnNames.get( reference ).add( srcColumn );
			destColumnNames.get( reference ).add( destColumn );
			seqs.get( reference ).add( new int[]{ seq, seqs.get( reference ).size() } );
		}
		rs.close();
		List<DBReference> references = new Vector<DBReference>();
		String keys[] = tableNames.keySet().toArray( new String[ 0 ] );
		for( int k = 0; k < keys.length; k++ )
		{
			List<int[]> referenceSeqs = seqs.get( keys[ k ] );
			Collections.sort( referenceSeqs, new Comparator<int[]>(){
				public int compare( int o1[], int o2[] )
				{
					if( o1[ 0 ] == o2[ 0 ] )
					{
						return 0;
					}
					else if( o1[ 0 ] > o2[ 0 ] )
					{
						return 1;
					}
					else
					{
						return -1;
					}
				}
			} );
			String srcTableName = tableNames.get( keys[ k ] )[ 0 ];
			String destTableName = tableNames.get( keys[ k ] )[ 1 ];
			DBTable srcTable = getTableByName( srcTableName );
			DBTable destTable = getTableByName( destTableName );
			List<DBColumn> srcColumns = new Vector<DBColumn>();
			List<DBColumn> destColumns = new Vector<DBColumn>();
			for( int n = 0; n < referenceSeqs.size(); n++ )
			{
//				System.out.println( srcTable.get( DBTable.NAME ) + "." + columnNames.get( keys[ k ] ).get( referenceSeqs.get( n )[ 1 ] ) );
				DBColumn srcColumn = getColumnByTableAndName( srcTable, columnNames.get( keys[ k ] ).get( referenceSeqs.get( n )[ 1 ] ) );
				if( srcColumn == null )
				{
					continue;
				}
				srcColumn.set( DBColumn.IS_FOREIGN_KEY, "y" );
				srcColumns.add( srcColumn );
				destColumns.add( getColumnByTableAndName( destTable, destColumnNames.get( keys[ k ] ).get( referenceSeqs.get( n )[ 1 ] ) ) );
			}
			DBReference dbr = new DBReference( this );
			dbr.set( DBReference.NAME, keys[ k ] );
			dbr.set( DBReference.SRC_COLUMNS, srcColumns );
			dbr.set( DBReference.SRC_TABLE, srcTable );
			dbr.set( DBReference.DEST_COLUMNS, destColumns );
			dbr.set( DBReference.DEST_TABLE, destTable );
			references.add( dbr );
		}
		return references;
	}
	
	public List<DBReference> getExportedKeysForTable( DBTable table )
		throws Exception
	{
		String tableName = ( String )table.get( DBTable.NAME );
		if( !EXPORTED_KEY_CACHE.containsKey( tableName ) )
		{
			ResultSet rs = metadata.getExportedKeys( catalog, schema, tableName );
			
			EXPORTED_KEY_CACHE.put( tableName, readForeignKeys( rs ) );
		}
		return EXPORTED_KEY_CACHE.get( tableName );
	}
	
	public List<DBReference> getImportedKeysForTable( DBTable table )
		throws Exception
	{
		String tableName = ( String )table.get( DBTable.NAME );
		if( !IMPORTED_KEY_CACHE.containsKey( tableName ) )
		{
			ResultSet rs = metadata.getImportedKeys( catalog, schema, ( String )table.get( DBTable.NAME ) );
			
			IMPORTED_KEY_CACHE.put( tableName, readForeignKeys( rs ) );
		}
		return IMPORTED_KEY_CACHE.get( tableName );
	}
	
	public List<DBTable> getSortedTables()
		throws Exception
	{
		getTables();
		Map<DBTable,DBTable> done = new HashMap<DBTable,DBTable>();
		List<DBTable> ordered = new ArrayList<DBTable>();
		Map<String,DBTable> cacheClone = new HashMap<String,DBTable>();
		cacheClone.putAll( TABLE_CACHE );
		int lastSize = -1;
		while( cacheClone.size() > 0 )
		{
			String keys[] = cacheClone.keySet().toArray( new String[ cacheClone.size() ] );
			if( lastSize == cacheClone.size() )
			{
				System.out.println( "Unorderable tables:" );
				for( int k = 0; k < keys.length; k++ )
				{
					DBTable table = cacheClone.get( keys[ k ] );
					System.out.println( "\t" + table.get( DBTable.NAME ) );
				}
				break;
			}
			lastSize = cacheClone.size();
			
//System.out.println( "Unclean: " + cacheClone.size() );
			for( int k = 0; k < keys.length; k++ )
			{
				DBTable table = cacheClone.get( keys[ k ] );
				List<DBReference> references = table.getImportedForeignKeys();
				boolean clean = true;
				if( references.size() > 0 )
				{
					for( int r = 0; r < references.size(); r++ )
					{
						DBTable dest = ( DBTable ) references.get( r ).get( DBReference.DEST_TABLE );
						if( !done.containsKey( dest ) && !dest.equals( table ) )
						{
							clean = false;
							break;
						}
					}
				}
				if( clean )
				{

					cacheClone.remove( keys[ k ] );
					ordered.add( table );
					done.put( table, table );
				}
			}
		}
		return ordered;
	}
	
	public List<DBHierarchy> getHierarchysForTable( DBTable table )
		throws Exception
	{
	// create the tree
		DefaultMutableTreeNode root = new DefaultMutableTreeNode( table );
		List<DBHierarchy> hierarchysList = new Vector<DBHierarchy>();
		buildTree( root, hierarchysList );
		return hierarchysList;
	}

	private void buildTree( DefaultMutableTreeNode node, List<DBHierarchy> list )
		throws Exception
	{
		DBTable table = ( DBTable )node.getUserObject();
		List<DBReference> refs = table.getImportedForeignKeys();
		if( refs.isEmpty() )
		{
			DBHierarchy hierarchy = new DBHierarchy( this );
			Object tables[] = node.getUserObjectPath();
			for( Object o: tables )
			{
				hierarchy.addTable( ( DBTable )o );
			}
			list.add( hierarchy );
		}
		else
		{
			for( DBReference ref: refs )
			{
				DBTable childTable = ref.getDestinationTable();
				// remove recursive tables
				if( !childTable.get( DBTable.NAME ).equals( table.get( DBTable.NAME ) ) )
				{
					DefaultMutableTreeNode child = new DefaultMutableTreeNode( childTable );
					node.add( child );
					buildTree( child, list );
				}
			}
		}
	}
}
