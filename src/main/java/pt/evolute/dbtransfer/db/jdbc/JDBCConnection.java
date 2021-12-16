package pt.evolute.dbtransfer.db.jdbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import pt.evolute.dbtransfer.db.DBConnection;
import pt.evolute.dbtransfer.db.beans.ColumnDefinition;
import pt.evolute.dbtransfer.db.beans.ForeignKeyDefinition;
import pt.evolute.dbtransfer.db.beans.Name;
import pt.evolute.dbtransfer.db.beans.PrimaryKeyDefinition;
import pt.evolute.dbtransfer.db.beans.TableDefinition;
import pt.evolute.dbtransfer.db.beans.UniqueDefinition;
import pt.evolute.dbtransfer.db.helper.Helper;
import pt.evolute.dbtransfer.db.helper.HelperManager;
import pt.evolute.utils.arrays.CursorResultSet2DArray;
import pt.evolute.utils.arrays.Virtual2DArray;
import pt.evolute.utils.db.Connector;
import pt.evolute.utils.dbmodel.DBTable;
import pt.evolute.utils.dbmodel.ModelProvider;

/**
 *
 * @author lflores
 */
public class JDBCConnection implements DBConnection
{
    public static boolean debug = true;
    public static boolean debugSQL = true;

    private final String dbUrl;
    private final String dbUser;
    private final String dbPasswd;

    private Connection connection;

    private final String catalog;
    private final boolean ignoreEmpty;

    private final Map<String,List<ColumnDefinition>> MAP_TABLE_COLUMNS = new HashMap<String,List<ColumnDefinition>>();

    private final List<TableDefinition> LIST_TABLES = new ArrayList<TableDefinition>();
    
    private final Helper helper;

    public JDBCConnection( String url, String user, String pass, boolean onlyNotEmpty )
                    throws Exception
    {
            dbUrl = url;
            dbUser= user;
            dbPasswd= pass;
            testInitConnection();
            catalog = connection.getCatalog();
//            if( schema == null )
//            {
//                dbSchema = Connector.getSchema( url );
//            }
//            else
//            {
//                dbSchema = schema;
//            }

            ignoreEmpty = onlyNotEmpty;
            helper = HelperManager.getTranslator( url );
            helper.initConnection( this );
            System.out.println( "JDBC: " + url + " catalog: " + catalog );
    }

    private void testInitConnection()
            throws Exception
    {
        boolean init;
        if( connection == null )
        {
            init = true;
        }
        else
        {
            try
            {
                init = connection.isClosed() || !connection.isValid( 4 );
            }
            catch( SQLException ex )
            {
            	if( debug )
            	{
            		ex.printStackTrace();
            	}
                init = true;
                try
                {
                	connection.close();
                }
                catch( SQLException ex1 )
                {
                	if( debug )
                	{
                		ex1.printStackTrace();
                	}
                }
            }
        }
        if( init )
        {
        	if( debug )
        	{
        		System.out.println( "getConnection: " + dbUrl + " " + dbUser );
        	}
            connection = Connector.getConnection( dbUrl, dbUser, dbPasswd );
            connection.setAutoCommit( true );
        }
    }

    public List<TableDefinition> getTableList()
                    throws Exception
    {
    	if( LIST_TABLES.isEmpty() )
    	{
	        DatabaseMetaData rsmd = connection.getMetaData();
	        ResultSet rs = rsmd.getTables( catalog, connection.getSchema(), null, new String[] { "TABLE" } );
	        while( rs.next() )
	        {
	            String table = rs.getString( 3 );
	            TableDefinition n = new TableDefinition( table );
	            if( helper.isTableValid( n ) )
	            {
	            	if( "elo_log".equalsIgnoreCase( table ) 
	            			|| "iri_sessao".equalsIgnoreCase( table )
	            			|| "iri_ses_output".equalsIgnoreCase( table )
	            			|| "iri_transaccao".equalsIgnoreCase( table )
	            			|| "sql_features".equalsIgnoreCase( table ) 
	            			|| "sql_implementation_info".equalsIgnoreCase( table )
	            			|| "sql_languages".equalsIgnoreCase( table )
	            			|| "sql_packages".equalsIgnoreCase( table )
	            			|| "sql_parts".equalsIgnoreCase( table )
	            			|| "sql_sizing".equalsIgnoreCase( table )
	            			|| "sql_sizing_profiles".equalsIgnoreCase( table ))
	            	{
	            		continue;
	            	}
	                if( ignoreEmpty && getRowCount( n ) == 0 )
	                {
	                        continue;
	                }
	                System.out.println( "Loading table: <" + table + ">" );
	                LIST_TABLES.add( n );
	            }
	        }
	        rs.close();
    	}
	    return LIST_TABLES;
    }

    public List<ColumnDefinition> getColumnList( TableDefinition table ) throws Exception
    {
        List<ColumnDefinition> list = MAP_TABLE_COLUMNS.get( table.saneName );
        if( list == null )
        {
            testInitConnection();
            DatabaseMetaData rsmd = connection.getMetaData();
            ResultSet rs = rsmd.getColumns( catalog, connection.getSchema(), table.saneName, null );
            list = new LinkedList<ColumnDefinition>();
            Map<String,ColumnDefinition> cols = new HashMap<String, ColumnDefinition>();
            if( debug )
            {
            	System.out.println( "COL FOR TABLE: <" + table.saneName + ">" );
            }
            while( rs.next() )
            {
            	String n;
            	if( table.originalName.equals( table.originalName.toUpperCase() ) )
            	{
            		n = rs.getString( 4 );
            	}
            	else
            	{
            		n = "\"" + rs.getString( 4 ) + "\"";
            	}
            	Name name = new Name( n );
            	if( debug )
                {
                	System.out.println( "COL: <" + name.originalName + ">" );
                }
//                    System.out.println( "COL: " + name.originalName );
                if( !cols.containsKey( name.saneName ) )
                {
                    ColumnDefinition col = new ColumnDefinition();
                    col.name = name;
                    col.sqlTypeName = rs.getString( 6 );
                    col.sqlType = rs.getInt( 5 );
                    if( rs.getInt( 5 ) == Types.CHAR
                                    || rs.getInt( 5 ) == Types.LONGVARCHAR
                                    || rs.getInt( 5 ) == Types.VARCHAR
                                    || rs.getInt( 5 ) == Types.NCHAR
                                    || rs.getInt( 5 ) == Types.LONGNVARCHAR
                                    || rs.getInt( 5 ) == Types.NVARCHAR )
                    {
                            col.sqlSize = rs.getInt( 7 );
                    }
                    col.defaultValue = helper.normalizeDefault( rs.getString( 13 ) );
                    col.isNotNull = "NO".equals( rs.getString( 18 ).trim() );
                    cols.put( col.name.saneName, col );
                    list.add( col );
//                        System.out.println( "Adding col: " + table.originalName + " - " + name );
                }
                else
                {
                    System.out.println( "Ignoring duplicate: " + table.originalName + " - " + name );
                    new Exception( "Ignoring duplicate: " + table.originalName + " - " + name ).printStackTrace();;
                }
            }
            rs.close();
            list = Collections.unmodifiableList( list );
            MAP_TABLE_COLUMNS.put( table.originalName, list );
        }
//            System.out.println( "COLSS size: " + list.size() );
        return list;
    }

    public Virtual2DArray executeQuery(String sql) throws Exception
    {
        if( debug && debugSQL )
        {
            System.out.println( "SQL: " + sql );
        }
        testInitConnection();
        Statement stm = connection.createStatement( ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY );
        helper.setupStatement( stm );
        Virtual2DArray ret = null;
        try
        {
        	boolean hasResult = stm.execute( sql );
	        if( hasResult )
	        {
	            ResultSet rs = stm.getResultSet();
	            ret = new CursorResultSet2DArray( rs );
	        }
        }
        catch( Exception ex )
        {
        	System.out.println( ex.getMessage() + "<" + sql + ">"  );
        	throw ex;
        }
        return ret;
    }

    public PrimaryKeyDefinition getPrimaryKey( TableDefinition table) throws Exception
    {
        PrimaryKeyDefinition pk = new PrimaryKeyDefinition();
        pk.name = table + "_pk";
        DatabaseMetaData rsmd = connection.getMetaData();
        ResultSet rs = rsmd.getPrimaryKeys( catalog, connection.getSchema(), helper.outputName( table.originalName ) );
        while( rs.next() )
        {
            ColumnDefinition col = new ColumnDefinition();
            col.name = new Name( rs.getString( 4 ) );
            ResultSet rsC = rsmd.getColumns( catalog, connection.getSchema(), helper.outputName( table.originalName ), col.name.originalName );
            rsC.next();
            col.sqlTypeName = rsC.getString( 6 );
            if( rsC.getInt( 5 ) == Types.CHAR
                            || rsC.getInt( 5 ) == Types.LONGVARCHAR
                            || rsC.getInt( 5 ) == Types.VARCHAR
                            || rsC.getInt( 5 ) == Types.NCHAR
                            || rsC.getInt( 5 ) == Types.LONGNVARCHAR
                            || rsC.getInt( 5 ) == Types.NVARCHAR )
            {
                col.sqlSize = rsC.getInt( 7 );
            }
            col.defaultValue = rsC.getString( 13 );
            col.isNotNull = "NO".equals( rsC.getString( 18 ).trim() );
            rsC.close();
            pk.columns.add( col );
        }
        rs.close();
        return pk;
    }

    public List<ForeignKeyDefinition> getForeignKeyList(TableDefinition table) throws Exception
    {
        DatabaseMetaData rsmd = connection.getMetaData();
        System.out.println( "getting FKs: " + connection.getCatalog() + "/" + connection.getSchema() + "/" + table.originalName + "/" );
        ResultSet rs = rsmd.getImportedKeys( catalog, connection.getSchema(), helper.outputName( table.originalName ) );
        List<ForeignKeyDefinition> list = new LinkedList<ForeignKeyDefinition>();
        Map<String,ForeignKeyDefinition> fks = new HashMap<String, ForeignKeyDefinition>();
        while( rs.next() )
        {
            String fkName = rs.getString( 12 );
//System.out.println( "FK : " + fkName );
            ForeignKeyDefinition fk = fks.get( fkName );
            if( fk == null )
            {
                    fk = new ForeignKeyDefinition( fkName, table );
                    fks.put( fkName, fk );
            }
            ColumnDefinition col = new ColumnDefinition();
            col.referencedTable = new TableDefinition( rs.getString( 3 ) );
            col.referencedColumn = new ColumnDefinition();
            col.referencedColumn.name = new Name( rs.getString( 4 ) );
            col.name = new Name( rs.getString( 8 ) );
            ResultSet rsC = rsmd.getColumns( catalog, connection.getSchema(), helper.outputName( table.originalName ), col.name.originalName );
            if( rsC.next() )
            {
                col.sqlTypeName = rsC.getString( 6 );
                if( rsC.getInt( 5 ) == Types.CHAR
                                || rsC.getInt( 5 ) == Types.LONGVARCHAR
                                || rsC.getInt( 5 ) == Types.VARCHAR
                                || rsC.getInt( 5 ) == Types.NCHAR
                                || rsC.getInt( 5 ) == Types.LONGNVARCHAR
                                || rsC.getInt( 5 ) == Types.NVARCHAR )
                {
                        col.sqlSize = rsC.getInt( 7 );
                }
                col.defaultValue = rsC.getString( 13 );
                col.isNotNull = "NO".equals( rsC.getString( 18 ).trim() );
                rsC.close();
            }
            else
            {
                new Exception( "Can't find column for fk - " + fk.name + "/ col " + col.name ).printStackTrace();
            }
            fk.columns.add( col );
            list.add( fk );
        }
        rs.close();
        return list;
    }

    public Virtual2DArray getFullTable( TableDefinition table ) throws Exception
    {
            List<ColumnDefinition> cols = getColumnList( table );
            if( cols.isEmpty() )
            {
            	System.out.println( "Table without columns: " + table );
            	return null;
            }
            StringBuilder buffer = new StringBuilder( cols.get( 0 ).name.originalName );
            for( int i = 1; i < cols.size(); ++i )
            {
                ColumnDefinition col = cols.get( i );
                    buffer.append( ", " );
                    buffer.append( col.name.originalName );
            }
            return executeQuery( "SELECT " + buffer + " FROM " + helper.outputName( table.originalName ) );
    }

    public PreparedStatement prepareStatement(String sql) throws Exception
    {
            return connection.prepareStatement( sql );
    }

    @Override
    public List<DBTable> getSortedTables() throws Exception 
    {
            ModelProvider model = new ModelProvider( connection );
            return model.getSortedTables();
    }

    @Override
    public List<UniqueDefinition> getUniqueList( TableDefinition table )
            throws Exception
    {
        DatabaseMetaData rsmd = connection.getMetaData();
        ResultSet rs = rsmd.getIndexInfo( catalog, connection.getSchema(), helper.outputName( table.originalName ), true, false );
        List<UniqueDefinition> list = new LinkedList<UniqueDefinition>();
        UniqueDefinition lastUniq = null;
        while( rs.next() )
        {
            if( rs.getString( 6 ) != null && rs.getString( 9 ) != null && columnExists( table, rs.getString( 9 ) ) )
            {
                if( lastUniq == null || !lastUniq.name.equals( rs.getString( 6 ) ) )
                {
                    lastUniq = new UniqueDefinition( rs.getString( 6 ), table );
                    list.add( lastUniq );
                }
                lastUniq.columns.add( rs.getString( 9 ) );
            }
            else
            {
                System.out.println( "Discarding unique index table: " + table + " column: " + rs.getString( 9 ) );
            }
        }
        rs.close();
        return list;
    }

    private boolean columnExists( TableDefinition table, String column )
    	throws Exception
    {
    	List<ColumnDefinition> list = getColumnList( table );
    	boolean exists = false;
    	for( ColumnDefinition col: list )
    	{
    		if( col.name.equals( column ) )
    		{
    			exists = true;
    			break;
    		}
    	}
    	return exists;
    }
    
    @Override
    public int getRowCount( TableDefinition table) throws Exception 
    {
        int count = ( ( Number )executeQuery( "SELECT COUNT(*) FROM " + helper.outputName( table.originalName ) ).get( 0, 0 ) ).intValue();
        if( debug )
        {
                System.out.println( "COUNT: " + table.originalName + " " + count + " rows." );
        }
        return count;
    }

    public Helper getHelper()
    {
        return helper;
    }
    
    public String getSchema() throws SQLException
    {
        return connection.getSchema();
    }
}
