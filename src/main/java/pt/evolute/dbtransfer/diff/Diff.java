package pt.evolute.dbtransfer.diff;

import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.evolute.dbtransfer.Config;
import pt.evolute.dbtransfer.ConfigurationProperties;
import pt.evolute.dbtransfer.analyse.Analyser;
import pt.evolute.dbtransfer.constrain.Constrainer;
import pt.evolute.dbtransfer.db.DBConnection;
import pt.evolute.dbtransfer.db.DBConnector;
import pt.evolute.dbtransfer.db.PrimaryKeyValue;
import pt.evolute.dbtransfer.db.beans.ColumnDefinition;
import pt.evolute.dbtransfer.db.beans.ConnectionDefinitionBean;
import pt.evolute.dbtransfer.db.beans.ForeignKeyDefinition;
import pt.evolute.dbtransfer.db.beans.TableDefinition;
import pt.evolute.dbtransfer.db.helper.HelperManager;
import pt.evolute.dbtransfer.db.jdbc.JDBCConnection;
import pt.evolute.dbtransfer.transfer.Mover;
import pt.evolute.utils.arrays.Virtual2DArray;
import pt.evolute.utils.arrays.exception.EndOfArrayException;
import pt.evolute.utils.db.Connector;
import pt.evolute.utils.dbmodel.DBColumn;
import pt.evolute.utils.dbmodel.DBTable;
import pt.evolute.utils.error.ErrorLogger;
import pt.evolute.utils.sql.Assignment;
import pt.evolute.utils.sql.Expression;
import pt.evolute.utils.sql.Field;
import pt.evolute.utils.sql.Insert;
import pt.evolute.utils.sql.Select2;
import pt.evolute.utils.sql.Update;
import pt.evolute.utils.sql.backend.BackendProvider;

public class Diff extends Connector implements ConfigurationProperties
{
	private static final String TABLE_DBTRANSFER_UPDATE = "dbtransfer_update";
	
	private static final String COLUMN_MODIFIED_STAMP = "modified_stamp";
	private static final String COLUMN_MODIFIED_ACTION = "modified_action";
	private static final String COLUMN_MODIFIED_COMMENT = "modified_comment";
	
	private static final long MAX_MEM = Runtime.getRuntime().maxMemory();

	public static final int MAX_LOAD_ROWS = 50000;
	
	private final TableDefinition TABLES[];
	private final ConnectionDefinitionBean SRC;
	private final ConnectionDefinitionBean DST;
	private final DBConnection CON_SRC;
	private final DBConnection CON_DEST;
	
	private static int BATCH_NUMBER = 1000;
	
	private final StringBuilder queryBuffer = new StringBuilder();
	private int queryCounter = 0;
	
	private final String comment;
	
	private int totalInserted = 0;
	private int totalUpdated = 0;
	private int totalDeleted = 0;
	
//	private static final NumberFormat NF = DecimalFormat.getNumberInstance();
		
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
	
		List<TableDefinition> v = CON_SRC.getTableList();
		v = reorder( v );
		TABLES = v.toArray( new TableDefinition[ v.size() ] );
	}
	
	public void diffDb()
		throws Exception
	{
		checkDestinationDb();
		diff();
		
		System.out.println( "Total changes i " + totalInserted + " u " + totalUpdated + " d " + totalDeleted );
	}
	
	private void diff()
		throws Exception
	{
		Map<String,TableDefinition> map = new HashMap<String,TableDefinition>();
		for( TableDefinition t: TABLES )
		{
			map.put( t.saneName, t );
		}
		for( DBTable table: CON_SRC.getSortedTables() )
		{
			if( Boolean.TRUE.toString().equals( Config.getDiffIgnoreTablesWithoutPrimaryKey() ) )
			{
				if( table.getPrimaryKey().isEmpty() )
				{
					continue;
				}
			}
			TableDefinition td = map.get( table.getSaneName() );
			if( td != null )
			{
				if( !TABLE_DBTRANSFER_UPDATE.equals( table.toString() ) )
				{
					try
					{
						checkDestinationHasAllColumns( td );
						diffTable( table, MAX_LOAD_ROWS );
					}
					catch( Exception ex )
					{
						ErrorLogger.logException( ex );
						throw ex;
					}
				}
			}
			else
			{
				System.out.println( "Table not found: " + table.getSaneName() );
			}
		}
	}
	
	private List<TableDefinition> reorder(List<TableDefinition> inputList) throws Exception 
    {
		List<TableDefinition> tablesByDependency = new ArrayList<TableDefinition>();
		
		while( !inputList.isEmpty() )
		{
			int noDeps = 0;
			 for( TableDefinition n: inputList )
	         {
				if( JDBCConnection.debug )
                {
                    System.out.println( "Testing: " + n.originalName );
                }
				List<ForeignKeyDefinition> fks = CON_DEST.getForeignKeyList( n );
                boolean ok = true;
                for( ForeignKeyDefinition fk: fks )
                {
                    if( !tablesByDependency.contains( fk.columns.get( 0 ).referencedTable ) )
                    {
                    	if( inputList.contains( fk.columns.get( 0 ).referencedTable ) )
                    	{
	                        if( JDBCConnection.debug )
	                        {
	                            System.out.println( "Depends failed for: " + fk.columns.get( 0 ).referencedTable.originalName );
	                        }
	                        ok = false;
	                        break;
                    	}
                    	else
                    	{
                    		if( JDBCConnection.debug )
	                        {
	                            System.out.println( "Depends failed for empty table (so it's OK): " + fk.columns.get( 0 ).referencedTable.originalName );
	                        }
                    	}
                    }
                }
                if( ok )
                {
                	tablesByDependency.add( n );
                    ++noDeps;
                    if( JDBCConnection.debug )
                    {
                        System.out.println( "No deps added: " + n );
                    }
                }
	         }
			 System.out.println( "No deps: " + noDeps );
			 inputList.removeAll( tablesByDependency );
			 System.out.println( "Still: " + inputList.size() + " to go!\n\n" );
		}
		System.out.println( "Reordered (" + tablesByDependency.size() + " tables)\nOrder:" );
        
		for( TableDefinition td: tablesByDependency )
		{
			System.out.println( td.originalName );
		}
		System.out.println( "\nDONE Reordering\n" );
		return tablesByDependency;
    }
	
	private void diffTable( DBTable table, int maxRowsPerPage )
		throws Exception
	{
		System.gc();
		System.out.println( "Testing table: " + table.toString() + " freemem: " 
					+ ( Runtime.getRuntime().freeMemory() / ( 1024 * 1024 ) ) );
		
		int i = 0;
		int u = 0;
		int d = 0;
		
		// one page at a time - maxRowsPerPage
		int srcPage = 0;
		int destPage = 0;
		TablePage dest = loadTable( table, CON_DEST, true, destPage++, maxRowsPerPage );
		int srcPageRowCount = 0;
		int destPageRowCount = dest.size();
		do
		{
			TablePage src = loadTable( table, CON_SRC, false, srcPage++, maxRowsPerPage );
			
			srcPageRowCount = src.size();
			
			// run src - find new and diff
			if( Config.debug() )
			{
				System.out.println( "Checking new and updated rows (" + src.size() + " vs " + ( dest != null? dest.size(): -1) + ")" );
			}

			for( PrimaryKeyValue pk: src.allKeysOrdered() )
			{
				System.out.println( "Source pkv: " + pk );
				if( ( dest == null || !dest.isPrimaryKeyValueInsidePage( pk ) ) )
				{
					if( dest != null )
					{
						// delete rest of rows on destination page
						for( PrimaryKeyValue pkNotExist: dest.keySet() )
						{
							if( !dest.get( pkNotExist ).isDeleted() )
							{
								deleteRow( table, pkNotExist );
								++d;
//								if( ( d % 1000 ) == 0 )
//								{
//									System.out.println( "Deleted (1) " + d + " rows!" );
//								}
							}
						}
						dest = null;
					}
					// can I load more pages on destination?
					if( destPageRowCount == maxRowsPerPage )
					{
//						System.out.println( "Loading next destination page: " + destPage );
						while( true ) 
						{
							dest = loadTable( table, CON_DEST, true, destPage++, maxRowsPerPage );
							destPageRowCount = dest.size();
							if( destPageRowCount > 0 && !dest.isPrimaryKeyValueInsidePage( pk ) )
							{
								for( PrimaryKeyValue pkNotExist: dest.keySet() )
								{
									if( !dest.get( pkNotExist ).isDeleted() )
									{
										deleteRow( table, pkNotExist );
										++d;
//										if( ( d % 1000 ) == 0 )
//										{
//											System.out.println( "Deleted (2) " + d + " rows!" );
//										}
									}
								}
							}
							else
							{
								// found the right page!
								break;
							}
						}
					}
//					else
//					{
//						System.out.println( "No more destination pages! size=" + destPageRowCount );
//					}
				}
				if( dest != null && dest.containsKey( pk ) )
				{
					if( !src.get( pk ).getMd5().equals( dest.get( pk ).getMd5() ) )
					{
						if( Config.debug() )
						{
							System.out.println( "Different rows: \n<" + src.get( pk ).getMd5() + ">\n<" + dest.get( pk ).getMd5() + ">" );
						}
						updateRow( table, pk, src.get( pk ) );
						++u;
					}
					else if( dest.get( pk ).isDeleted() )
					{
						// same row - but undeleted
						updateAction( table, pk );
						++u;
					}
					dest.remove( pk );
				}
				else
				{
					insertRow( table, pk, src.get( pk ) );
					++i;
				}
			}
			// run dst - find deleted
			if( Config.debug() )
			{
				System.out.println( "Checking deleted rows" );
			}
//			for( PrimaryKeyValue pk: dest.keySet() )
//			{
//				if( !"d".equals( dest.get( pk ).status ) )
//				{
//					deleteRow( table, pk );
//					++d;
//				}
//			}
		}
		while( srcPageRowCount == maxRowsPerPage );
		
		if( dest != null )
		{
			do
			{
				for( PrimaryKeyValue pk: dest.keySet() )
				{
					if( !dest.get( pk ).isDeleted() )
					{
						deleteRow( table, pk );
						++d;
//						if( ( d % 1000 ) == 0 )
//						{
//							System.out.println( "Deleted (3) " + d + " rows!" );
//						}
					}
				}
				dest = loadTable( table, CON_DEST, true, destPage++, maxRowsPerPage );
				destPageRowCount = dest.size();
			}
			while( destPageRowCount == maxRowsPerPage );
		}
		
//		CON_DEST.executeQuery( "COMMIT;" );
		commit();
		System.out.println( "Changes i " + i + " u " + u + " d " + d );
		totalInserted += i;
		totalUpdated += u;
		totalDeleted += d;
	}
	
	private void commit() throws Exception 
	{
		CON_DEST.executeQuery( queryBuffer.toString() );
		queryBuffer.delete( 0, queryBuffer.length() );
	}

	private void checkDestinationHasAllColumns(TableDefinition table) 
		throws Exception
	{
		List<ColumnDefinition> destCols = CON_DEST.getColumnList( table );
		for( ColumnDefinition col: CON_SRC.getColumnList( table ) )
		{
			if( !destCols.contains( col ) )
			{
				if( Config.debug() )
				{
					System.out.println( "Adding column " + col.name.saneName + " to destination table " + table.saneName );
				}
				CON_DEST.addColumnToTable( table, col );
			}
		}
	}

	private Object[] getRowData( DBTable table, PrimaryKeyValue pk )
		throws Exception
	{
		List<DBColumn> cols = table.getColumnsNoPK();
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
			new Exception( "Error in query: <" + select.toString() + ">" ).printStackTrace();
			throw ex;
		}
		return row;
	}
	
	private Expression getPkExpression( DBTable table, PrimaryKeyValue pk )
		throws Exception
	{
		List<Object> pko = pk.getList();
		Expression exp = null;
		List<DBColumn> pkf = new ArrayList<DBColumn>( table.getPrimaryKey() );
		if( pkf.isEmpty() )
		{
			if( Config.debug() )
			{
				System.out.println( "Table without PK (using all columns): " + table.getSaneName() );
			}
			if( "true".equals( Config.getDiffPrimaryKeyAllColumnsIfMissing() ) )
			{
				pkf = table.getColumnsNoPK();
				if( Config.debug() )
				{
					System.out.println( "PK : " + pkf );
				}
			}
			else
			{
				new Exception( "No PK: " + table );
			}
		}
		if( pko.size() != pkf.size() )
		{
			throw new Exception( "Invalid PK value for Table: " + pko.size() + " != " + pkf.size() + " " + table.getDestinationName() + " pk: " + pkf + " pkv: " + pk );
		}
		if( !pkf.isEmpty() )
		{
			exp = new Field( pkf.remove( 0 ).get( DBColumn.NAME ).toString() ).isEqual( pko.remove( 0 ) );
			for( DBColumn c: pkf )
			{
				exp = exp.and( new Field( c.get( DBColumn.NAME ).toString() ).isEqual( pko.remove( 0 ) ) );
			}
		}
//		if( pk.toString().lastIndexOf( '*' ) == 0 )
//		{
//			Object pkValue = pk.toString().substring( 1 );
//			if( !table.getPrimaryKey().get( 0 ).get( DBColumn.TYPE ).toString().toLowerCase().contains( "char" ) )
//			{
//				try
//				{
//					pkValue = Integer.parseInt( pkValue.toString() );
//				}
//				catch( NumberFormatException ex )
//				{
//					new Exception( "Invalid id: " + table + " / " + pk ).printStackTrace();
//					throw ex;
//				}
//			}
//			exp = new Field( ( String )table.getPrimaryKey().get( 0 
//					).get( DBColumn.NAME ) ).isEqual( pkValue );
//		}
//		else
//		{
//			exp = new Field( getKeyCollapsedField( table ) ).isEqual( pk );
//		}
		return exp;
	}
	
	private void updateRow( DBTable table, PrimaryKeyValue pk, TableRow row )
		throws Exception
	{
		Object rowData[] = null;
		if( row != null )
		{
			rowData = row.row.toArray();
		}
		else
		{
			// TODO - why ??
			getRowData( table, pk );
		}
		List<Assignment> assigns = new ArrayList<Assignment>();
		List<DBColumn> cols = table.getColumnsNoPK();
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
		
//		System.out.println( "U " + update );
		try
		{
//			CON_DEST.executeQuery( "BEGIN" );
//			CON_DEST.executeQuery( update.toString() );
//			CON_DEST.executeQuery( "COMMIT" );
			addQuery( update.toString() );
			
		}
		catch( Exception ex )
		{
			System.out.println( "Error on query: <" + update.toString() + ">" );
			throw ex;
		}
	}
	
	private void updateAction( DBTable table, PrimaryKeyValue pk )
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
			//CON_DEST.executeQuery( update.toString() );
			addQuery( update.toString() );
		}
		catch( Exception ex )
		{
			System.out.println( "Error on query: <" + update.toString() + ">" );
			throw ex;
		}
	}
	
	private void insertRow( DBTable table, PrimaryKeyValue pk, TableRow row )
		throws Exception
	{
		Object rowData[] = null;
		if( row != null )
		{
			rowData = row.row.toArray();
		}
		else
		{
			getRowData( table, pk );
		}
		List<Assignment> assigns = new ArrayList<Assignment>();
		List<DBColumn> cols = table.getColumnsNoPK();
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
		assigns.add( new Assignment( COLUMN_MODIFIED_COMMENT, comment ) );
		assigns.add( new Assignment( COLUMN_MODIFIED_STAMP, new Timestamp( System.currentTimeMillis() ) ) );
		List<DBColumn> pks = table.getPrimaryKey();
		if( !pks.isEmpty() )
		{
			for( int i = 0; i < pks.size(); ++i )
			{
				assigns.add( new Assignment( pks.get( i ).get( DBColumn.NAME ).toString(), pk.get( i ) ) );
			}
		}
		Insert insert = new Insert( table.toString(), assigns.toArray( new Assignment[ assigns.size() ] ) );
		insert.setBackend( BackendProvider.getBackend( DST.getUrl() ) );
		addQuery( insert.toString() );
		//CON_DEST.executeQuery( insert.toString() );
	}
	
	private void addQuery(String sql ) throws Exception 
	{
		queryBuffer.append( sql );
		queryBuffer.append( "\n" );
		if( ++queryCounter > BATCH_NUMBER )
		{
			queryCounter = 0;
			commit();
		}
	}

	private void deleteRow( DBTable table, PrimaryKeyValue pk )
		throws Exception
	{
		Update update = new Update( table.toString(), new Assignment[]{
					new Assignment( COLUMN_MODIFIED_ACTION, "d" ),
					new Assignment( COLUMN_MODIFIED_COMMENT, comment ),
					new Assignment( COLUMN_MODIFIED_STAMP, new Timestamp( System.currentTimeMillis() ) )
				} , getPkExpression( table, pk ).and( new Field( COLUMN_MODIFIED_ACTION ).isDifferent( "d" ) ) );
		update.setBackend( BackendProvider.getBackend( DST.getUrl() ) );
		//CON_DEST.executeQuery( update.toString() );
		addQuery( update.toString() );
	}
	
//	private static String getKeyCollapsedField( DBTable table )
//		throws Exception
//	{
//		if( Config.debug() )
//		{
//			System.out.println( "T: " + table.toString() );
//		}
//		List<DBColumn> pks = table.getPrimaryKey();
//		if( pks.isEmpty() && "true".equals( Config.getDiffPrimaryKeyAllColumnsIfMissing() ) )
//		{
//			pks = table.getColumnsNoPK();
//		}
//		StringBuilder sbPk = new StringBuilder( "'*'||" );
//		if( !pks.isEmpty() )
//		{
//			sbPk.append( pks.get( 0 ).get( DBColumn.NAME ) );
//			for( int i = 1; i < pks.size(); ++i )
//			{
//				sbPk.append( "||'*'||" );
//				sbPk.append( pks.get( i ).get( DBColumn.NAME ) );
//			}
//		}
//		else
//		{
//			throw new Exception( "Diff needs Primary Key on table - " + table.toString() );
//		}
//		return sbPk.toString();
//	}
	
	private static TablePage loadTable( DBTable table, DBConnection con, boolean getAction, int page, int rows )
		throws Exception
	{
		List<DBColumn> cols = table.getColumnsNoPK();
		List<String> str = new ArrayList<String>();
		for( int i = 0; i < cols.size(); ++i )
		{
			if( !COLUMN_MODIFIED_ACTION.equals( cols.get( i ).get( DBColumn.NAME ) )
					&& !COLUMN_MODIFIED_STAMP.equals( cols.get( i ).get( DBColumn.NAME ) )
					&& !COLUMN_MODIFIED_COMMENT.equals( cols.get( i ).get( DBColumn.NAME ) ) )
			{
//				if( i > 0 )
//				{
//					sb.append( "||" );
//				}
//				sb.append( "'*/'||COALESCE(" );
				if( cols.get( i ).getType().equals( Types.TIMESTAMP ) )
				{
//					sb.append( "TO_CHAR("+cols.get( i ).get( DBColumn.NAME )+",'YYYY/MM/DD HH24:MI:SS')" );
//					s += "TO_CHAR("+cols.get( i ).get( DBColumn.NAME )+",'YYYY/MM/DD HH24:MI:SS')";
					str.add( "" + cols.get( i ).get( DBColumn.NAME ) );
				}
				else if( cols.get( i ).getType().equals( Types.INTEGER )
						|| cols.get( i ).getType().equals( Types.TINYINT )
						|| cols.get( i ).getType().equals( Types.BIGINT ) )
				{
//					sb.append( "'.'||"+cols.get( i ).get( DBColumn.NAME ) );
					str.add( "" + cols.get( i ).get( DBColumn.NAME ) );
				}
				else if( cols.get( i ).getType().equals( Types.FLOAT )
						|| cols.get( i ).getType().equals( Types.DOUBLE )
						|| cols.get( i ).getType().equals( Types.NUMERIC ) )
				{
//					sb.append( "'.'||"+cols.get( i ).get( DBColumn.NAME ) );
					str.add( "" + cols.get( i ).get( DBColumn.NAME ) );
				}
				else
				{
//					sb.append( cols.get( i ).get( DBColumn.NAME ) );
					str.add( "" + cols.get( i ).get( DBColumn.NAME ) );
				}
//				sb.append( ",'.')" );
			}
		}
		List<String> fields = getPrimaryKeyFields( table );
		String pka[] = fields.toArray( new String[ fields.size() ] );
		List<String> cos = getNonPrimaryKeyFields( table );
		fields.addAll( cos );
		String colsa[] = cos.toArray( new String[ cos.size() ] );
//		String fields[] = new String[ getAction? 3: 2 ];
//		fields[ 0 ] = pkField;
//		if( Boolean.TRUE.toString().equals( Config.getDiffUseMD5() ) )
//		{
//			fields[ 1 ] = "md5(" + sb.toString() + ")";
//		}
//		else
//		{
//			fields[ 1 ] = sb.toString();
//		}
		if( getAction )
		{
//			fields[ 2 ] = COLUMN_MODIFIED_ACTION;
			fields.add( COLUMN_MODIFIED_ACTION );
		}
		List<String> order = new ArrayList<>();
		for( int i = 1; i <= pka.length; ++i )
		{
			order.add( "" + i );
		}
		String ordera[] = order.toArray( new String[ order.size() ] );
		Select2 select = new Select2( table.toString(), null, fields.toArray( new String[ fields.size() ] ), ordera );
		select.setLimit( rows );
		select.setOffset( page * rows );
		System.out.println( "Load Table Connection: " + con );
		System.out.println( "Load Table: " + select.toString() );
		select.setDatabaseType( con.getHelper().getType() );
//		Map<PrimaryKeyValue,TableRow> map = new HashMap<PrimaryKeyValue,TableRow>();
		TablePage tablePage = new TablePage();
		try
		{
			Virtual2DArray array = con.executeQuery( select.toString() );
			
			for( int i = 0; i < Integer.MAX_VALUE; ++i )
			{
				// rows
				try
				{
					PrimaryKeyValue pkv = new PrimaryKeyValue();
					// columns - pk
					TableRow row = new TableRow();
					for( int pki = 0; pki < pka.length; ++pki )
					{
						pkv.add( array.get( i, pki ) );
					}
//					StringBuilder sb = new StringBuilder( "*" );
					for( int f = 0; f < colsa.length; ++f )
					{
						Object o = array.get( i, f + pka.length );
						row.row.add( o );
					}
					
//					row.rowMd5 = sb.toString();
//					System.out.println( "PK: " + pkv.toString() + " md5: " + row.getMd5() );
					if( getAction )
					{
						row.setStatus( ( String )array.get( i, pka.length + colsa.length ) );
					}
					tablePage.put( pkv, row );
				}
				catch( EndOfArrayException ex )
				{
					System.out.println( "End of array - " + i );
					break;
				}
			}
		}
		catch( Exception ex )
		{
			new Exception( "Error in query: " + select );
			throw ex;
		}
		System.out.println( "Done loading table page: " + tablePage.size() + " pkvFirst: " + tablePage.getFirst() + " pkvLast: " + tablePage.getLast() );
		return tablePage;
	}
	
	private static List<String> getPrimaryKeyFields(DBTable table) throws Exception 
	{
		List<String> pkl = new ArrayList<String>();
		List<DBColumn> cols = table.getPrimaryKey();
		if( cols.isEmpty() && "true".equals( Config.getDiffPrimaryKeyAllColumnsIfMissing() ) )
		{
			cols = table.getColumnsNoPK();
		}
		for( DBColumn c: cols )
		{
			pkl.add( ( String )c.get( DBColumn.NAME ) );
		}
		return pkl;
	}
	
	private static List<String> getNonPrimaryKeyFields(DBTable table) throws Exception 
	{
		List<String> pkl = new ArrayList<String>();
		List<DBColumn> cols = table.getColumnsNoPK();
		for( DBColumn c: cols )
		{
			pkl.add( ( String )c.get( DBColumn.NAME ) );
		}
		return pkl;
	}

	private void checkDestinationDb()
		throws Exception
	{
		System.out.println( "Checking destination DB" );
		List<TableDefinition> v = CON_DEST.getTableList();
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
			throw new Exception( "Destination DB table count less than source and not empty!!!!" );
		}
		checkDiffColumns();
	}
	
	private void checkDiffColumns()
		throws Exception
	{
		System.out.println( "Checking diff columns" );
		List<TableDefinition> tableList = CON_SRC.getTableList();
		for( TableDefinition table: tableList )
		{
			if( Config.debug() )
			{
				System.out.println( "Testing table: <" + table.originalName + ">" );
			}
			List<ColumnDefinition> cols = CON_DEST.getColumnList( table );
			boolean modifiedStamp = false;
			boolean modifiedAction = false;
			boolean modifiedComment = false;
			for( ColumnDefinition col: cols )
			{
				if( Config.debug() )
				{
					System.out.println( "Testing col: <" + col.name + ">" );
				}
				if( !modifiedAction && COLUMN_MODIFIED_ACTION.equals( col.name.saneName ) )
				{
					modifiedAction = true;
				}
				if( !modifiedStamp && COLUMN_MODIFIED_STAMP.equals( col.name.saneName ) )
				{
					modifiedStamp = true;
				}
				if( !modifiedComment && COLUMN_MODIFIED_COMMENT.equals( col.name.saneName ) )
				{
					modifiedComment = true;
				}
				if( modifiedAction && modifiedStamp && modifiedComment )
				{
					break;
				}
			}
			if( Config.debug() )
			{
				System.out.println( "MA " + modifiedAction + " MS " + modifiedStamp + " MC " + modifiedComment );
			}
			if( !modifiedAction )
			{
				String sql1 = "ALTER TABLE " + table + " ADD COLUMN " 
					+ COLUMN_MODIFIED_ACTION + " CHAR( 1 ) NOT NULL DEFAULT 'i';";
				Update update = new Update( sql1 );
				update.setBackend( BackendProvider.getBackend( DST.getUrl() ) );
				CON_DEST.executeQuery( update.toString() );
				System.out.println( sql1 );
				String sql2 = "ALTER TABLE " + table + " ADD CONSTRAINT "
					+ table + "_" + COLUMN_MODIFIED_ACTION + "_ck CHECK( "
					+ COLUMN_MODIFIED_ACTION + " IN ( 'i', 'u', 'd' ) )";
				System.out.println( sql2 );
				update = new Update( sql2 );
				update.setBackend( BackendProvider.getBackend( DST.getUrl() ) );
				CON_DEST.executeQuery( update.toString() );
				
			}
			if( !modifiedStamp )
			{
				String sql3 = "ALTER TABLE " + table + " ADD COLUMN " 
				+ COLUMN_MODIFIED_STAMP + " TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now();";
				System.out.println( sql3 );
				Update update = new Update( sql3 );
				update.setBackend( BackendProvider.getBackend( DST.getUrl() ) );
				CON_DEST.executeQuery( update.toString() );
				
			}
			if( !modifiedComment )
			{
				String sql4 = "ALTER TABLE " + table + " ADD COLUMN " 
					+ COLUMN_MODIFIED_COMMENT + " VARCHAR( 255 );";
				System.out.println( sql4 );
				Update update = new Update( sql4 );
				update.setBackend( BackendProvider.getBackend( DST.getUrl() ) );
				CON_DEST.executeQuery( update.toString() );
			}
		}
	}
}
