package pt.evolute.dbtransfer.diff;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import pt.evolute.utils.arrays.Virtual2DArray;
import pt.evolute.utils.db.Connector;
import pt.evolute.utils.dbmodel.DBColumn;
import pt.evolute.utils.dbmodel.DBTable;
import pt.evolute.utils.sql.Assignment;
import pt.evolute.utils.sql.Expression;
import pt.evolute.utils.sql.Field;
import pt.evolute.utils.sql.Insert;
import pt.evolute.utils.sql.Select2;
import pt.evolute.utils.sql.Update;
import pt.evolute.utils.sql.backend.BackendProvider;
import pt.evolute.dbtransfer.Constants;
import pt.evolute.dbtransfer.analyse.Analyser;
import pt.evolute.dbtransfer.constrain.Constrainer;
import pt.evolute.dbtransfer.db.DBConnection;
import pt.evolute.dbtransfer.db.DBConnector;
import pt.evolute.dbtransfer.db.beans.ColumnDefinition;
import pt.evolute.dbtransfer.db.beans.ConnectionDefinitionBean;
import pt.evolute.dbtransfer.db.beans.Name;
import pt.evolute.dbtransfer.transfer.Mover;

public class Diff extends Connector implements Constants
{
	private static final String COLUMN_MODIFIED_STAMP = "modified_stamp";
	private static final String COLUMN_MODIFIED_ACTION = "modified_action";
	private static final String COLUMN_MODIFIED_COMMENT = "modified_comment";
	
	private static final long MAX_MEM = Runtime.getRuntime().maxMemory();

	public static final int MAX_BATCH_ROWS = 4096;
	
	private final Name TABLES[];
	private final ConnectionDefinitionBean SRC;
	private final ConnectionDefinitionBean DST;
	private final DBConnection CON_SRC;
	private final DBConnection CON_DEST;
	
	private final String comment;
	
	private final Properties props;
	
	public Diff( Properties props, ConnectionDefinitionBean src, ConnectionDefinitionBean dst )
		throws Exception
	{
		this.props = props;
		SRC = src;
                DST = dst;
		boolean ignoreEmpty = Boolean.parseBoolean( props.getProperty( ONLY_NOT_EMPTY, "false" ) );
		
		CON_SRC = DBConnector.getConnection( SRC, ignoreEmpty );
		
		comment = props.getProperty( DIFF_COMMENT );
		
		CON_DEST = DBConnector.getConnection( DST, false );
		
	System.out.println( "Using max " + ( MAX_MEM / ( 1024 * 1024 ) ) + " MB of memory" );
	
		List<Name> v = CON_SRC.getTableList();
		TABLES = v.toArray( new Name[ v.size() ] );
	}
	
	public void diffDb()
		throws Exception
	{
		checkDestinationDb();
		diff();
	}
	
	private void diff()
		throws Exception
	{	
		for( DBTable table: CON_SRC.getSortedTables() )
		{
			diffTable( table );
		}
	}
	
	private void diffTable( DBTable table )
		throws Exception
	{
		System.gc();
		System.out.println( "Testing table: " + table.toString() + " freemem: " 
					+ ( Runtime.getRuntime().freeMemory() / ( 1024 * 1024 ) ) );
		// full RAM algorithm
		Map<Object,TableRow> src = loadTable( table, CON_SRC, false );
		Map<Object,TableRow> dest = loadTable( table, CON_DEST, true );
		// run src - find new and diff
		System.out.println( "Checking new and updated rows (" + src.size() + " vs " + dest.size() + ")" );
		for( Object pk: src.keySet() )
		{
			if( dest.containsKey( pk ) )
			{
//				System.out.println( "pk: " + pk + " srcNull: " + (src == null) + " destNull: " + ( dest == null ) + " dest.get: " + dest.get( pk ) );
				if( !src.get( pk ).rowMd5.equals( dest.get( pk ).rowMd5 ) )
				{
					updateRow( table, pk );
				}
				else if( "d".equals( dest.get( pk ).status ) )
				{
					updateAction( table, pk );
				}
			}
			else
			{
				insertRow( table, pk );
			}
		}
		// run dst - find deleted
		System.out.println( "Checking deleted rows" );
		for( Object pk: dest.keySet() )
		{
			if( !src.containsKey( pk ) )
			{
				deleteRow( table, pk );
			}
		}
	}
	
	private Object[] getRowData( DBTable table, Object pk )
		throws Exception
	{
		List<DBColumn> cols = table.getColumns();
		String fields[] = new String[ cols.size() ];
		for( int i = 0; i < fields.length; ++i )
		{
			fields[ i ] = cols.get( i ).get( DBColumn.NAME ).toString();
		}
		Select2 select = new Select2( table.toString(), getPkExpression( table, pk ), fields );
		Object row[] = new Object[ fields.length ];
		try
		{
			Virtual2DArray array = CON_SRC.executeQuery( select.toString() );
			
			for( int i = 0; i < array.columnCount(); ++i )
			{
				row[ i ] = array.get( 0, i );
			}
		}
		catch( Exception ex )
		{
			new Exception( "Error in query: " + select.toString() ).printStackTrace();
			throw ex;
		}
		return row;
	}
	
	private Expression getPkExpression( DBTable table, Object pk )
		throws Exception
	{
		Expression exp = null;
		if( pk.toString().lastIndexOf( '*' ) == 0 )
		{
			Object pkValue = pk.toString().substring( 1 );
			if( !table.getPrimaryKey().get( 0 ).get( DBColumn.TYPE ).toString().contains( "char" ) )
			{
				try
				{
					pkValue = new Integer( pkValue.toString() );
				}
				catch( NumberFormatException ex )
				{
					new Exception( "Invalid id: " + table + " / " + pk ).printStackTrace();
					throw ex;
				}
			}
			exp = new Field( ( String )table.getPrimaryKey().get( 0 
					).get( DBColumn.NAME ) ).isEqual( pkValue );
		}
		else
		{
			exp = new Field( getKeyCollapsedField( table ) ).isEqual( pk );
		}
		return exp;
	}
	
	private void updateRow( DBTable table, Object pk )
		throws Exception
	{
		Object rowData[] = getRowData( table, pk );
		Assignment assigns[] = new Assignment[ rowData.length + 3 ];
		List<DBColumn> cols = table.getColumns();
		for( int i = 0; i < rowData.length; ++i )
		{
			assigns[ i ] = new Assignment( cols.get( i ).get( DBColumn.NAME ).toString(), rowData[ i ] );
		}
		assigns[ rowData.length  ] = new Assignment( COLUMN_MODIFIED_ACTION, "u" );
		assigns[ rowData.length + 1 ] = new Assignment( COLUMN_MODIFIED_COMMENT, comment );
		assigns[ rowData.length + 2 ] = new Assignment( COLUMN_MODIFIED_STAMP, new Timestamp( System.currentTimeMillis() ) );
		
		Update update = new Update( table.toString(), assigns, getPkExpression(table, pk) );
		update.setBackend( BackendProvider.getBackend( DST.getUrl() ) );
		try
		{
			CON_DEST.executeQuery( update.toString() );
		}
		catch( Exception ex )
		{
			System.out.println( "Error on query: <" + update.toString() + ">" );
			throw ex;
		}
	}
	
	private void updateAction( DBTable table, Object pk )
		throws Exception
	{
		Assignment assigns[] = new Assignment[ 3 ];
		assigns[ 0  ] = new Assignment( COLUMN_MODIFIED_ACTION, "u" );
		assigns[ 1 ] = new Assignment( COLUMN_MODIFIED_COMMENT, comment );
		assigns[ 2 ] = new Assignment( COLUMN_MODIFIED_STAMP, new Timestamp( System.currentTimeMillis() ) );
		
		Update update = new Update( table.toString(), assigns, getPkExpression(table, pk) );
		update.setBackend( BackendProvider.getBackend( DST.getUrl() ) );
		try
		{
			CON_DEST.executeQuery( update.toString() );
		}
		catch( Exception ex )
		{
			System.out.println( "Error on query: <" + update.toString() + ">" );
			throw ex;
		}
	}
	
	private void insertRow( DBTable table, Object pk )
		throws Exception
	{
		Object rowData[] = getRowData( table, pk );
		Assignment assigns[] = new Assignment[ rowData.length + 3 ];
		List<DBColumn> cols = table.getColumns();
		for( int i = 0; i < rowData.length; ++i )
		{
			assigns[ i ] = new Assignment( cols.get( i ).get( DBColumn.NAME ).toString(), rowData[ i ] );
		}
		assigns[ rowData.length  ] = new Assignment( COLUMN_MODIFIED_ACTION, "i" );
		assigns[ rowData.length + 1 ] = new Assignment( COLUMN_MODIFIED_COMMENT, "comment" );
		assigns[ rowData.length + 2 ] = new Assignment( COLUMN_MODIFIED_STAMP, new Timestamp( System.currentTimeMillis() ) );
		
		Insert insert = new Insert( table.toString(), assigns );
		insert.setBackend( BackendProvider.getBackend( DST.getUrl() ) );
		CON_DEST.executeQuery( insert.toString() );
	}
	
	private void deleteRow( DBTable table, Object pk )
		throws Exception
	{
		Update update = new Update( table.toString(), new Assignment[]{
					new Assignment( COLUMN_MODIFIED_ACTION, "d" ),
					new Assignment( COLUMN_MODIFIED_COMMENT, "comment" ),
					new Assignment( COLUMN_MODIFIED_STAMP, new Timestamp( System.currentTimeMillis() ) )
				} , getPkExpression( table, pk ).and( new Field( COLUMN_MODIFIED_ACTION ).isDifferent( "d" ) ) );
		update.setBackend( BackendProvider.getBackend( DST.getUrl() ) );
		CON_DEST.executeQuery( update.toString() );
	}
	
	private static String getKeyCollapsedField( DBTable table )
		throws Exception
	{
		List<DBColumn> pks = table.getPrimaryKey();
		StringBuilder sbPk = new StringBuilder( "'*'||" );
		sbPk.append( pks.get( 0 ).get( DBColumn.NAME ) );
		for( int i = 1; i < pks.size(); ++i )
		{
			sbPk.append( "||'*'||" );
			sbPk.append( pks.get( i ).get( DBColumn.NAME ) );
		}
		return sbPk.toString();
	}
	
	private static Map<Object,TableRow> loadTable( DBTable table, DBConnection con, boolean getAction )
		throws Exception
	{
		List<DBColumn> cols = table.getColumnsNoPK();
		StringBuilder sb = new StringBuilder( "'*'||coalesce(''||" );
		sb.append( cols.get( 0 ).get( DBColumn.NAME ) );
		sb.append( ",'NULL')" );
		for( int i = 1; i < cols.size(); ++i )
		{
			sb.append( "||'*'||coalesce(''||" );
			sb.append( cols.get( i ).get( DBColumn.NAME ) );
			sb.append( ",'NULL')" );
		}
		String pkField = getKeyCollapsedField( table );
		String fields[] = new String[ getAction? 3: 2 ];
		fields[ 0 ] = pkField;
		fields[ 1 ] = "md5(" + sb.toString() + ")";
		if( getAction )
		{
			fields[ 2 ] = COLUMN_MODIFIED_ACTION;
		}
		Select2 select = new Select2( table.toString(), null, fields, new String[]{ pkField } );
//		System.out.println( "Load Table: " + select.toString() );
		Map<Object,TableRow> map = new HashMap<Object,TableRow>();
		try
		{
			Virtual2DArray array = con.executeQuery( select.toString() );
			
			for( int i = 0; i < array.rowCount(); ++i )
			{
				Object id = array.get( i, 0 );
				TableRow row = new TableRow();
				row.rowMd5 = ( String )array.get( i, 1 );
				if( getAction )
				{
					row.status = ( String )array.get( i, 2 );
				}
				map.put( id, row );
			}
		}
		catch( Exception ex )
		{
			new Exception( "Error in query: " + select );
			throw ex;
		}
		return map;
	}
	
	private void checkDestinationDb()
		throws Exception
	{
		List<Name> v = CON_DEST.getTableList();
		if( v.isEmpty() && TABLES.length > 0 )
		{
			Analyser analyser = new Analyser( props, SRC, DST );
			analyser.cloneDB();
			Mover mover = new Mover( props, SRC, DST );
			mover.moveDB();
			Constrainer constrainer = new Constrainer( props, SRC, DST );
			constrainer.constrainDB();
			// all OK :) 
		}
		else if( v.size() >= TABLES.length )
		{
			// all OK :)
			// TODO - cycle all table/all column check for sanitity
		}
		else 
		{
			throw new Exception( "Destination DB table count lesser than source and not empty!!!!" );
		}
		checkDiffColumns();
	}
	
	private void checkDiffColumns()
		throws Exception
	{
		List<Name> tableList = CON_DEST.getTableList();
		for( Name table: tableList )
		{
			List<ColumnDefinition> cols = CON_DEST.getColumnList( table );
			boolean modifiedStamp = false;
			boolean modifiedAction = false;
			boolean modifiedComment = false;
			for( ColumnDefinition col: cols )
			{
				if( !modifiedAction && COLUMN_MODIFIED_ACTION.equals( col.name ) )
				{
					modifiedAction = true;
				}
				if( !modifiedStamp && COLUMN_MODIFIED_STAMP.equals( col.name ) )
				{
					modifiedStamp = true;
				}
				if( !modifiedComment && COLUMN_MODIFIED_COMMENT.equals( col.name ) )
				{
					modifiedComment = true;
				}
				if( modifiedAction && modifiedStamp && modifiedComment )
				{
					break;
				}
			}
			if( !modifiedAction )
			{
				String sql1 = "ALTER TABLE " + table + " ADD COLUMN " 
					+ COLUMN_MODIFIED_ACTION + " CHAR( 1 ) NOT NULL DEFAULT 'i';";
					CON_DEST.executeQuery( sql1 );
				System.out.println( sql1 );
				String sql2 = "ALTER TABLE " + table + " ADD CONSTRAINT "
					+ table + "_" + COLUMN_MODIFIED_ACTION + "_ck CHECK( "
					+ COLUMN_MODIFIED_ACTION + " IN ( 'i', 'u', 'd' ) )";
				System.out.println( sql2 );
				CON_DEST.executeQuery( sql2 );
			}
			if( !modifiedStamp )
			{
				String sql3 = "ALTER TABLE " + table + " ADD COLUMN " 
				+ COLUMN_MODIFIED_STAMP + " TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now();";
				System.out.println( sql3 );
				CON_DEST.executeQuery( sql3 );
			}
			if( !modifiedComment )
			{
				String sql1 = "ALTER TABLE " + table + " ADD COLUMN " 
					+ COLUMN_MODIFIED_COMMENT + " VARCHAR( 255 );";
				CON_DEST.executeQuery( sql1 );
				System.out.println( sql1 );
			}
		}
	}
}
