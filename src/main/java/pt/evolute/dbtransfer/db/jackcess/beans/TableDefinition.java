package pt.evolute.dbtransfer.db.jackcess.beans;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.Index;
import com.healthmarketscience.jackcess.IndexData;
import com.healthmarketscience.jackcess.IndexData.ColumnDescriptor;
import com.healthmarketscience.jackcess.Table;

import pt.evolute.dbtransfer.db.beans.ColumnDefinition;
import pt.evolute.dbtransfer.db.beans.ForeignKeyDefinition;
import pt.evolute.dbtransfer.db.beans.Name;

/**
 *
 * @author lflores
 */
public class TableDefinition
{
	private final Table table;
	
	private final List<ColumnDefinition> primaryKeys = new LinkedList<ColumnDefinition>();
	private final List<ForeignKeyDefinition> foreignKeys = new LinkedList<ForeignKeyDefinition>();
	private final List<ColumnDefinition> columns = new LinkedList<ColumnDefinition>();

	private final Map<String,ColumnDefinition> columns_map = new HashMap<String,ColumnDefinition>();

	public TableDefinition( Table t )
	{
		table = t;
	}

	public Table getTable()
	{
		return table;
	}

	public List<ColumnDefinition> getPrimaryKeys()
			throws SQLException, IOException
	{
		if( columns.isEmpty() )
		{
			initColumns();
		}
		return primaryKeys;
	}

	public List<ForeignKeyDefinition> getForeignKeys()
			throws SQLException, IOException
	{
		if( columns.isEmpty() )
		{
			initColumns();
		}
		return foreignKeys;
	}

	public List<ColumnDefinition> getColumns()
			throws SQLException, IOException
	{
		if( columns.isEmpty() )
		{
			initColumns();
		}
		return columns;
	}

	private void initColumns()
			throws SQLException, IOException
	{
		List<Column> cols = table.getColumns();
		for( Column col: cols )
		{
			ColumnDefinition myCol = new ColumnDefinition();
			myCol.name = new Name( col.getName() );
			fillSQLType( myCol, col );
			if( myCol.sqlType == Types.CHAR
//					|| col.getSQLType() == Types.LONGVARCHAR
					|| col.getSQLType() == Types.VARCHAR )
			{
				myCol.sqlSize = new Integer( col.getLength() );
			}
			myCol.isNotNull = false;
			columns.add( myCol );
			columns_map.put( myCol.name.toString(), myCol );
		}
		List<Index> indexes = table.getIndexes();
		for( Index idx: indexes )
		{
			if( idx.isPrimaryKey() )
			{
				List<ColumnDescriptor> idxCols = idx.getColumns();
				for( ColumnDescriptor idxCol: idxCols )
				{
					ColumnDefinition col = columns_map.get( idxCol.getName().toLowerCase() );
					col.isPrimaryKey = true;
					/* fix double pk to integer */
					if( col.sqlType == Types.DOUBLE && idxCol.getColumn().getPrecision() == 0 )
					{
						col.sqlType = Types.INTEGER;
						col.sqlTypeName = "integer";
					}
				}
			}
			if( idx.isForeignKey() )
			{
				List<ColumnDescriptor> idxCols = idx.getColumns();
				for( ColumnDescriptor idxCol: idxCols )
				{
					ColumnDefinition col = columns_map.get( idxCol.getName().toLowerCase() );
					col.foreignKeyName = idxCol.getName().toLowerCase();
					/* access reports double fk pointing to integer */
					if( col.sqlType == Types.DOUBLE && idxCol.getColumn().getPrecision() == 0 )
					{
						col.sqlType = Types.INTEGER;
						col.sqlTypeName = "integer";
					}
					List<IndexData.ColumnDescriptor> list = idx.getReferencedIndex().getColumns();
					
					if( list.size() == 1 )
					{
						if( !col.isPrimaryKey )
						{
							col.referencedTable = new Name( idx.getReferencedIndex().getTable().getName() );
							col.referencedColumn = new Name( list.get( 0 ).getName() );
						}
						else
						{
							System.out.println( "Ignoring foreign key on primary key: " + table.getName() + "." + col.name 
									+ " idx: " + idx.getName() + " points to: " 
									+ idx.getReferencedIndex().getTable().getName() + "." 
									+ list.get( 0 ).getName() );
						}
					}
					else
					{
						System.out.println( "Can't solve foreign key (multiple columns): " + table.getName() + "." + col.name 
								+ " idx: " + idx.getName() + " points to: " 
								+ idx.getReferencedIndex().getTable().getName() + "." 
								+ list.get( 0 ).getName() );
					}
/*					String canditateTable = col.name.substring( 0, col.name.length() - 3 );
					if( connection.getTableDefinition( canditateTable ) != null )
					{
						col.referencedTable = canditateTable;
						col.referencedColumn = col.name;
//						System.out.println( "FK: " + table.getName() + "." + col.name + "->" + col.referencedTable + "." + col.referencedColumn );
					}
					else
					{
						System.out.println( "Can't solve foreign key: " + table.getName() + "." + col.name 
								+ " idx: " + idx.getName() + " points to: " 
								+ idx.getReferencedIndex().getTable().getName() + "." 
								+ idx.getReferencedIndex().getColumns().get( 0 ).getName() );
						if( col.sqlType == Types.DOUBLE )
						{
							col.sqlType = Types.INTEGER;
							col.sqlTypeName = "integer";
						}
					}*/
				}
			}
		}
		Map<String,ForeignKeyDefinition> fks = new HashMap<String,ForeignKeyDefinition>();
		for( ColumnDefinition myCol: columns )
		{
			if( myCol.isPrimaryKey )
			{
				primaryKeys.add( myCol );
			}
			if( myCol.referencedTable != null )
			{
				ForeignKeyDefinition fk = fks.get( myCol.foreignKeyName );
				if( fk == null )
				{
					fk = new ForeignKeyDefinition( myCol.foreignKeyName, new Name( table.getName() ) );
					foreignKeys.add( fk );
				}
				fk.columns.add( myCol );
			}
		}
		System.out.println( "Table " + table.getName() + " has " + columns.size() + " columns "
				+ primaryKeys.size() + " primary keys " + foreignKeys.size() + " foreign keys" );
	}

	private void fillSQLType( ColumnDefinition myCol, Column col )
			throws SQLException
	{
		String name = col.getType().name();
		int type = col.getSQLType();
		switch( type )
		{
			case Types.DOUBLE:
				{
					if( col.getPrecision() == 0 )
					{
						String lowerName = col.getName().toLowerCase();
						if( lowerName.endsWith( "_id" ) || lowerName.endsWith( "_lnk" )  )
						{
							type = Types.INTEGER;
							name = "integer";
						}
					}
		//			System.out.println( "double col: " + table.getName() + "." + myCol.name
		//					+ " sqlType: " + col.getSQLType() + " precision: " + col.getPrecision()
		//					+ " scale: " + col.getScale() + " varLen: " + col.isVariableLength() + " idx:" + col.getColumnIndex() );
				}
				break;
			case Types.VARCHAR:
				name = "varchar";
				break;
			case Types.TIMESTAMP:
				name = "timestamp";
				break;
			case Types.INTEGER:
				if( col.isAutoNumber() )
				{
					name = "serial";
				}
				break;
			case Types.LONGVARCHAR:
				name = "longvarchar";
				break;
		}
/*		if( "memo".equals( name.toLowerCase() ) )
		{
			System.out.println( "memo TYPE: " + type );
			name = "text";
		}*/
		
		myCol.sqlType = type;
		myCol.sqlTypeName = name;
	}
}
