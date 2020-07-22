package pt.evolute.dbtransfer.diff;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.evolute.dbtransfer.Config;
import pt.evolute.dbtransfer.Constants;
import pt.evolute.dbtransfer.analyse.Analyser;
import pt.evolute.dbtransfer.constrain.Constrainer;
import pt.evolute.dbtransfer.db.DBConnection;
import pt.evolute.dbtransfer.db.DBConnector;
import pt.evolute.dbtransfer.db.beans.ColumnDefinition;
import pt.evolute.dbtransfer.db.beans.ConnectionDefinitionBean;
import pt.evolute.dbtransfer.db.beans.Name;
import pt.evolute.dbtransfer.db.helper.HelperManager;
import pt.evolute.dbtransfer.transfer.Mover;
import pt.evolute.utils.arrays.Virtual2DArray;
import pt.evolute.utils.arrays.exception.EndOfArrayException;
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

public class Diff extends Connector implements Constants
{
	private static final String TABLE_DBTRANSFER_UPDATE = "dbtransfer_update";
	
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
		
	public Diff( ConnectionDefinitionBean src, ConnectionDefinitionBean dst )
		throws Exception
	{
		SRC = src;
                DST = dst;
		boolean ignoreEmpty = Config.ignoreEmpty();
		
		CON_SRC = DBConnector.getConnection( SRC, ignoreEmpty );
		
		comment = Config.getDiffComment();
		
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
			if( !TABLE_DBTRANSFER_UPDATE.equals( table.toString() ) )
			{
				diffTable( table );
			}
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
		int i = 0;
		int u = 0;
		int d = 0;
		for( Object pk: src.keySet() )
		{
//			if( "cft".equals( table.toString() ) )
//			{
//				System.out.println( "Pk: " + pk );
//			}
			if( dest.containsKey( pk ) )
			{
//				if( "cft".equals( table.toString() ) )
//				{
//					System.out.println( "IN DEST pk: " + pk + " srcNull: " + (src == null) + " destNull: " + ( dest == null ) + " dest.get: " + dest.get( pk ) );
//				}
				if( !src.get( pk ).rowMd5.equals( dest.get( pk ).rowMd5 ) )
				{
					updateRow( table, pk );
					++u;
				}
				else if( "d".equals( dest.get( pk ).status ) )
				{
					updateAction( table, pk );
				}
			}
			else
			{
				if( "ctf".equals( table.toString() ) )
				{
					System.out.println( "INSERT");
				}
				insertRow( table, pk );
				++i;
			}
		}
		// run dst - find deleted
		System.out.println( "Checking deleted rows" );
		for( Object pk: dest.keySet() )
		{
			if( !src.containsKey( pk ) )
			{
				deleteRow( table, pk );
				++d;
			}
		}
		CON_DEST.executeQuery( "COMMIT;" );
		System.out.println( "Changes i " + i + " u " + u + " d " + d );
	}
	
	private Object[] getRowData( DBTable table, Object pk )
		throws Exception
	{
		List<DBColumn> cols = table.getColumns();
		List<String> fields = new ArrayList<String>();
		for( int i = 0; i < cols.size(); ++i )
		{
			if( !COLUMN_MODIFIED_ACTION.equals( cols.get( i ).get( DBColumn.NAME ) )
					&& !COLUMN_MODIFIED_STAMP.equals( cols.get( i ).get( DBColumn.NAME ) )
					&& !COLUMN_MODIFIED_COMMENT.equals( cols.get( i ).get( DBColumn.NAME ) ) )
			{
				fields.add( cols.get( i ).get( DBColumn.NAME ).toString() );
			}
		}
		Select2 select = new Select2( table.toString(), getPkExpression( table, pk ), fields.toArray( new String[ fields.size() ] ) ) ;
		System.out.println( "GET SRC ROW: " + select );
		Object row[] = new Object[ fields.size() ];
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
		List<Assignment> assigns = new ArrayList<Assignment>();
		List<DBColumn> cols = table.getColumns();
		for( int i = 0; i < rowData.length; ++i )
		{
			if( !COLUMN_MODIFIED_ACTION.equals( cols.get( i ).get( DBColumn.NAME ) )
					&& !COLUMN_MODIFIED_STAMP.equals( cols.get( i ).get( DBColumn.NAME ) )
					&& !COLUMN_MODIFIED_COMMENT.equals( cols.get( i ).get( DBColumn.NAME ) ) )
			{
				assigns.add( new Assignment( cols.get( i ).get( DBColumn.NAME ).toString(), rowData[ i ] ) );
			}
		}
		assigns.add( new Assignment( COLUMN_MODIFIED_ACTION, "u" ) );
		assigns.add( new Assignment( COLUMN_MODIFIED_COMMENT, comment ) );
		assigns.add( new Assignment( COLUMN_MODIFIED_STAMP, new Timestamp( System.currentTimeMillis() ) ) );
		
		Update update = new Update( table.toString(), assigns.toArray( new Assignment[ assigns.size() ] ), getPkExpression(table, pk) );
		update.setBackend( BackendProvider.getBackend( DST.getUrl() ) );
		
		System.out.println( "U " + update );
		try
		{
			CON_DEST.executeQuery( "BEGIN" );
			CON_DEST.executeQuery( update.toString() );
			CON_DEST.executeQuery( "COMMIT" );
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
		List<Assignment> assigns = new ArrayList<Assignment>();
		List<DBColumn> cols = table.getColumns();
		for( int i = 0; i < rowData.length; ++i )
		{
			if( !COLUMN_MODIFIED_ACTION.equals( cols.get( i ).get( DBColumn.NAME ) )
					&& !COLUMN_MODIFIED_STAMP.equals( cols.get( i ).get( DBColumn.NAME ) )
					&& !COLUMN_MODIFIED_COMMENT.equals( cols.get( i ).get( DBColumn.NAME ) ) )
			{
				assigns.add( new Assignment( cols.get( i ).get( DBColumn.NAME ).toString(), rowData[ i ] ) );
			}
		}
		assigns.add( new Assignment( COLUMN_MODIFIED_ACTION, "i" ) );
		assigns.add( new Assignment( COLUMN_MODIFIED_COMMENT, "comment" ) );
		assigns.add( new Assignment( COLUMN_MODIFIED_STAMP, new Timestamp( System.currentTimeMillis() ) ) );
		
		Insert insert = new Insert( table.toString(), assigns.toArray( new Assignment[ assigns.size() ] ) );
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
		System.out.println( "T: " + table.toString() );
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
			if( !COLUMN_MODIFIED_ACTION.equals( cols.get( i ).get( DBColumn.NAME ) )
					&& !COLUMN_MODIFIED_STAMP.equals( cols.get( i ).get( DBColumn.NAME ) )
					&& !COLUMN_MODIFIED_COMMENT.equals( cols.get( i ).get( DBColumn.NAME ) ) )
			{
				sb.append( "||'*'||coalesce(''||" );
				sb.append( cols.get( i ).get( DBColumn.NAME ) );
				sb.append( ",'NULL')" );
			}
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
		System.out.println( "Load Table: " + select.toString() );
		Map<Object,TableRow> map = new HashMap<Object,TableRow>();
		try
		{
			Virtual2DArray array = con.executeQuery( select.toString() );
			
			for( int i = 0; i < Integer.MAX_VALUE; ++i )
			{
				try
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
				catch( EndOfArrayException ex )
				{
					System.out.println( "End of array" );
					break;
				}
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
			Analyser analyser = new Analyser( SRC, DST );
			analyser.cloneDB();
			Mover mover = new Mover( SRC, DST );
			mover.moveDB();
			Constrainer constrainer = new Constrainer(HelperManager.getProperties(), SRC, DST );
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
			System.out.println( "Testing table: <" + table.originalName + ">" );
			List<ColumnDefinition> cols = CON_DEST.getColumnList( table );
			boolean modifiedStamp = false;
			boolean modifiedAction = false;
			boolean modifiedComment = false;
			for( ColumnDefinition col: cols )
			{
				System.out.println( "Testing col: <" + col.name + ">" );
				if( !modifiedAction && COLUMN_MODIFIED_ACTION.equals( col.name.toString() ) )
				{
					modifiedAction = true;
				}
				if( !modifiedStamp && COLUMN_MODIFIED_STAMP.equals( col.name.toString() ) )
				{
					modifiedStamp = true;
				}
				if( !modifiedComment && COLUMN_MODIFIED_COMMENT.equals( col.name.toString() ) )
				{
					modifiedComment = true;
				}
				if( modifiedAction && modifiedStamp && modifiedComment )
				{
					break;
				}
			}
			System.out.println( "MA " + modifiedAction + " MS " + modifiedStamp + " MC " + modifiedComment );
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
