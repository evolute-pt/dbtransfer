package pt.evolute.dbtransfer.transfer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import pt.evolute.utils.arrays.Virtual2DArray;
import pt.evolute.utils.db.Connector;
import pt.evolute.utils.string.UnicodeChecker;
import pt.evolute.dbtransfer.Constants;
import pt.evolute.dbtransfer.db.DBConnection;
import pt.evolute.dbtransfer.db.DBConnector;
import pt.evolute.dbtransfer.db.beans.ColumnDefinition;
import pt.evolute.dbtransfer.db.beans.ForeignKeyDefinition;
import pt.evolute.dbtransfer.db.beans.Name;
import pt.evolute.dbtransfer.db.helper.Helper;
import pt.evolute.dbtransfer.db.helper.HelperManager;
import pt.evolute.dbtransfer.db.jdbc.JDBCConnection;
import pt.evolute.utils.arrays.exception.EndOfArrayException;


/**
 *
 * @author  lflores
 */
public class Mover extends Connector implements Constants
{
    public static final int MAX_BATCH_ROWS = 1024;

    private final Name TABLES[];
    private final String SRC_URL;
    private final String DEST_URL;
    private final DBConnection CON_SRC;
    private final DBConnection CON_DEST;
    private final boolean CHECK_DEPS;
    private final boolean ESCAPE_UNICODE;

    private static final long MAX_MEM = Runtime.getRuntime().maxMemory();

    private boolean sleeping = false;
    private int readRows = 0;
    
    private static long lastGc = 0;
//	private static int oom = 0;

    /** Creates a new instance of Mover */
    public Mover( Properties props )
            throws Exception
    {
        SRC_URL = props.getProperty( URL_DB_SOURCE );
        ESCAPE_UNICODE = "true".equals( props.getProperty( TRANSFER_ESCAPE_UNICODE ) );
        String srcUser = props.getProperty( USER_DB_SOURCE );
        String srcPasswd = props.getProperty( PASSWORD_DB_SOURCE );
        boolean ignoreEmpty = Boolean.parseBoolean( props.getProperty( ONLY_NOT_EMPTY, "false" ) );

        CON_SRC = DBConnector.getConnection( SRC_URL, srcUser, srcPasswd, ignoreEmpty );

        DEST_URL = props.getProperty( URL_DB_DESTINATION );
        String destUser = props.getProperty( USER_DB_DESTINATION );
        String destPasswd = props.getProperty( PASSWORD_DB_DESTINATION );
        CON_DEST = DBConnector.getConnection( DEST_URL, destUser, destPasswd, false );
System.out.println( "Using max " + ( MAX_MEM / ( 1024 * 1024 ) ) + " MB of memory" );

        CHECK_DEPS = "true".equalsIgnoreCase( props.getProperty( TRANSFER_CHECK_DEPS, "false" ) );
        List<Name> v = CON_SRC.getTableList();
        if( CHECK_DEPS )
        {
            System.out.println( "Reordering tables for dependencies (" + v.size() + " tables)" );
            v = reorder( v );
        }
        TABLES = v.toArray( new Name[ v.size() ] );
    }

    private List<Name> reorder(List<Name> inputList) throws Exception 
    {
        Map<Name,Name> noDepsTablesMap = new HashMap<Name,Name>();
        List<Name> deps = new ArrayList<Name>();
        List<Name> list = new ArrayList<Name>();
        while( !inputList.isEmpty() || !deps.isEmpty() )
        {
            if( !deps.isEmpty() )
            {
                inputList.addAll( deps );
            }
            for( Name n: inputList )
            {
                if( JDBCConnection.debug )
                {
                    System.out.println( "Testing: " + n.originalName );
                }
                List<ForeignKeyDefinition> fks = CON_DEST.getForeignKeyList( n );
                boolean ok = true;
                for( ForeignKeyDefinition fk: fks )
                {
                    if( !noDepsTablesMap.containsKey( fk.columns.get( 0 ).referencedTable ) )
                    {
                        if( JDBCConnection.debug )
                        {
                            System.out.println( "Depends: " + fk.columns.get( 0 ).referencedTable.originalName );
                        }
                        deps.add( n );
                        ok = false;
                        break;
                    }
                }
                if( ok )
                {
                    list.add( n );
                    noDepsTablesMap.put( n, n );
                }
            }
            inputList.clear();
        }
        System.out.println( "Reordered (" + list.size() + " tables)" );
        return list;
    }

    private static boolean testRow( Virtual2DArray array, int row )
    {
        boolean rowOk = true;
        try
        {
            array.get( row, 0 );
        }
        catch( EndOfArrayException ex )
        {
            rowOk = false;
        }
        return rowOk;
    }
    
    public void moveDB()
            throws Exception
    {
        System.out.println( "Moving (" + TABLES.length + " tables)" );
        final List<Object> TEMP = new LinkedList<Object>();
        List<AsyncStatement> threads = new LinkedList<AsyncStatement>();
        Helper tr = HelperManager.getTranslator( DEST_URL );
        
        ReportThread rt = new ReportThread( this, AsyncStatement.getRunningThreads() );
        rt.start();
        
        for( int i = 0; i < TABLES.length; ++i )
        {
            Virtual2DArray rs = CON_SRC.getFullTable( TABLES[ i ] );
            System.out.println( "Moving table: " + TABLES[ i ] + " (" + /*rs.rowCount()*/ "NA" + " rows)" );
            boolean hasData = rs != null;
            if( hasData  )
            {
                hasData = testRow( rs, 0 );
            }
            if( hasData )
            {
                StringBuilder buff = new StringBuilder( "INSERT INTO " );
                StringBuilder args = new StringBuilder();
                buff.append( TABLES[ i ].saneName );
                buff.append( " ( " );

                List<ColumnDefinition> columns = CON_SRC.getColumnList( TABLES[ i ] );
                if( columns.isEmpty() )
                {
                    System.out.println( "NO COLUMNS FOR: " + TABLES[ i ] );
                }
                for( int j = 0; j < columns.size(); ++j )
                {
                    if( j != 0 )
                    {
                        buff.append( ", " );
                        args.append( ", " );
                    }
                    buff.append( tr.outputName( columns.get( j ).name.saneName ) );
                    args.append( "?" );
                }
                buff.append( " ) VALUES ( " );
                buff.append( args );
                buff.append( " )" );
                String insert = buff.toString();
//			PreparedStatement pstm = CON_DEST.prepareStatement( insert );
System.out.println( "I: " + i + " " + TABLES[ i ].saneName + " sql: " + insert + " colList.sz: " + columns.size() );
                int typesCache[] = new int[ columns.size() ];
                for( int j = 0; j < columns.size(); ++j )
                {
                    typesCache[ j ] = columns.get( j ).sqlType;
                    if( typesCache[ j ] == 0 )
                    {
                        System.out.println( "Can't resolve Type: " + columns.get( j ).name + " / " + columns.get( j ).sqlTypeName );
                    }
                }
                // TODO - do only if table has identity (when target is sqlserver)
                String pre = tr.preLoadSetup( TABLES[ i ].saneName );
                String post = tr.postLoadSetup( TABLES[ i ].saneName );
                AsyncStatement astm = new AsyncStatement( typesCache, CON_DEST, insert, TABLES[ i ].saneName, pre, post );
                threads.add( astm );
                int rows = 0;

                int row = 0;
                while( hasData )
                {
                    for( int j = 0; j < typesCache.length; ++j )
                    {
                        Object data = rs.get( row, j );
                        if( ESCAPE_UNICODE && data != null 
                                        && data instanceof String )
                        {
                            data = UnicodeChecker.parseToUnicode( ( String )data );
                        }
                        TEMP.add( data );
//					pstm.setObject( j, rs.getObject( j ), rsmd.getColumnType( j ) );
                    }
                    ++rows;
                    astm.DATA_SHARED.addAll( TEMP );
                    TEMP.clear();
                    if( ( rows % MAX_BATCH_ROWS ) == 0 )
                    {
                        readRows = rows;
                        //System.out.print( "+" + i + "." + ( rows / MAX_BATCH_ROWS ) );
                        while( !astm.DATA_SHARED.isEmpty() && cacheLimit() )
                        {
                            try
                            {
//                                System.out.print( "W" );
                                sleeping = true;
                                Thread.sleep( 1000 );
                                sleeping = false;
                            }
                            catch( InterruptedException ex )
                            {
                            }
                        }
                    }
                    ++row;
                    hasData = testRow( rs, row );
                }
                System.out.println( "Done reading table: " + TABLES[ i ].saneName );
                astm.stopThread();
//				astm.join();
                while( AsyncStatement.waitingThreads() > 0 /* !astm.DATA_SHARED.isEmpty() /*( 3 * typesCache.length * MAX_BATCH_ROWS )*/ )
                {
                    try
                    {
//                        System.out.print( "H" );
                        sleeping = true;
                        Thread.sleep( 500 );
                        sleeping = false;
                        System.gc();
                    }
                    catch( InterruptedException ex )
                    {
                        ex.printStackTrace();
                    }
                }
            }
        }
        rt.stopReporting();
        for( AsyncStatement async: threads )
        {
            if( async.isAlive() )
            {
                System.out.println( "Waiting for thread: " + async.getName() );
                async.join();
            }
        }
    }

    protected static synchronized boolean cacheLimit()
    {
        boolean limit = false;
        long totalMem = Runtime.getRuntime().totalMemory();
        if( ( double )totalMem > .75 * MAX_MEM )
        {
            long freeMem = Runtime.getRuntime().freeMemory();
            limit = (double)freeMem < .30 * MAX_MEM;
            if( limit )
            {
                if( (double)freeMem < .1 * MAX_MEM )
                {
//                System.out.println( "free: " + freeMem / (1024*1024) 
//                        + "/" + MAX_MEM / (1024*1024) + "MB" );
                    if( ( System.currentTimeMillis() - lastGc ) > 60 * 1000 )
                    {
                        System.out.println( "Invoking GC" );
                        System.gc();
                        lastGc = System.currentTimeMillis();
                    }
                }
            }
        }
        return limit;
    }

    public int getReadCount() 
    {
        return readRows;
    }
    
    public boolean isSleeping()
    {
        return sleeping;
    }
}
