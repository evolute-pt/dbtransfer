package pt.evolute.dbtransfer.db.dummy;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

import pt.evolute.dbtransfer.db.DBConnection;
import pt.evolute.dbtransfer.db.beans.ColumnDefinition;
import pt.evolute.dbtransfer.db.beans.ForeignKeyDefinition;
import pt.evolute.dbtransfer.db.beans.Name;
import pt.evolute.dbtransfer.db.beans.PrimaryKeyDefinition;
import pt.evolute.dbtransfer.db.beans.UniqueDefinition;
import pt.evolute.dbtransfer.db.helper.Helper;
import pt.evolute.utils.arrays.Virtual2DArray;
import pt.evolute.utils.dbmodel.DBTable;

/**
 *
 * @author lflores
 */
public class DummyConnection implements DBConnection {

	private final String table;
	
	private final int rows;
	
    private final List<Name> TABLES_LIST = new ArrayList<Name>();
    private final List<DBTable> DBTABLES_LIST = new ArrayList<DBTable>();
    private ArrayList<ColumnDefinition> COLUMNS_LIST = null; 
    private final boolean ignoreEmpty;

    public DummyConnection(String url, String user, String pass, boolean onlyNotEmpty)
            throws Exception {
    	url = sanitizeUrl( url );
        String t[] = url.split(":")[ 1 ].split("/");
        
        table = t[ 2 ];
        rows = Integer.parseInt( t[ 3 ] ) * 1000000;
        
        ignoreEmpty = onlyNotEmpty;
    }
    
    private String sanitizeUrl(String url) {
		String t = url.split( ":" )[ 1 ];
		if( t.isEmpty() )
		{
			url = url + "//table/1";
		}
		else if( !t.startsWith( "//" ) )
		{
			url = "//" + url;
		}
		if( !t.substring( 2 ).contains( "/" ) )
		{
			url = url + "/1";
		}
		return url;
	}

	public List<Name> getTableList()
            throws Exception {
        if (TABLES_LIST.isEmpty()) {
        	if( !ignoreEmpty || rows != 0 )
        	{
        		Name n = new Name( table );
        		TABLES_LIST.add( n );
        		COLUMNS_LIST = new ArrayList<ColumnDefinition>();
        		ColumnDefinition col = new ColumnDefinition();
        		col.name = new Name( "id_pk" );
        		col.isNotNull = true;
        		col.isPrimaryKey = true;
        		col.sqlType = Types.BIGINT;
        		col.sqlTypeName = "bigint";
        		COLUMNS_LIST.add( col );
        	}
        }
        return TABLES_LIST;
    }

    public List<ColumnDefinition> getColumnList(Name table) throws Exception {
        return COLUMNS_LIST;
    }

    public Virtual2DArray executeQuery(String sql) throws Exception {
    	if( !sql.equals( "BEGIN;" ) && !sql.equals( "COMMIT;" ) )
    	{
    		System.out.println( "DB: " + table + " sql: " + sql );
    	}
    	return null;
    }

    public PrimaryKeyDefinition getPrimaryKey(Name table) throws Exception {
        PrimaryKeyDefinition pk = new PrimaryKeyDefinition();
        pk.name = table + "_" + "pk";
        pk.columns.addAll( COLUMNS_LIST);
        return pk;
    }

    public List<ForeignKeyDefinition> getForeignKeyList(Name table) throws Exception {
        return new ArrayList<ForeignKeyDefinition>();
    }

    public Virtual2DArray getFullTable(Name table) throws Exception {
    	Virtual2DArray array = null;
    	if( !COLUMNS_LIST.isEmpty() && table.originalName.contentEquals( this.table ) )
    	{
    		array = new Dummy2DArray( rows, new String[] { COLUMNS_LIST.get( 0 ).name.originalName } );
    	}
        return array; 
    }

    public PreparedStatement prepareStatement(String sql) throws Exception {
        return new PreparedStatement() {
			
			@Override
			public <T> T unwrap(Class<T> iface) throws SQLException {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public boolean isWrapperFor(Class<?> iface) throws SQLException {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public void setQueryTimeout(int seconds) throws SQLException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setPoolable(boolean poolable) throws SQLException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setMaxRows(int max) throws SQLException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setMaxFieldSize(int max) throws SQLException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setFetchSize(int rows) throws SQLException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setFetchDirection(int direction) throws SQLException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setEscapeProcessing(boolean enable) throws SQLException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setCursorName(String name) throws SQLException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public boolean isPoolable() throws SQLException {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean isClosed() throws SQLException {
				// TODO Auto-generated method stub
				return true;
			}
			
			@Override
			public boolean isCloseOnCompletion() throws SQLException {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public SQLWarning getWarnings() throws SQLException {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public int getUpdateCount() throws SQLException {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public int getResultSetType() throws SQLException {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public int getResultSetHoldability() throws SQLException {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public int getResultSetConcurrency() throws SQLException {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public ResultSet getResultSet() throws SQLException {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public int getQueryTimeout() throws SQLException {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public boolean getMoreResults(int current) throws SQLException {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean getMoreResults() throws SQLException {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public int getMaxRows() throws SQLException {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public int getMaxFieldSize() throws SQLException {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public ResultSet getGeneratedKeys() throws SQLException {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public int getFetchSize() throws SQLException {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public int getFetchDirection() throws SQLException {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public Connection getConnection() throws SQLException {
				return new Connection() {
					
					@Override
					public <T> T unwrap(Class<T> iface) throws SQLException {
						// TODO Auto-generated method stub
						return null;
					}
					
					@Override
					public boolean isWrapperFor(Class<?> iface) throws SQLException {
						// TODO Auto-generated method stub
						return false;
					}
					
					@Override
					public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void setTransactionIsolation(int level) throws SQLException {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void setSchema(String schema) throws SQLException {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public Savepoint setSavepoint(String name) throws SQLException {
						// TODO Auto-generated method stub
						return null;
					}
					
					@Override
					public Savepoint setSavepoint() throws SQLException {
						// TODO Auto-generated method stub
						return null;
					}
					
					@Override
					public void setReadOnly(boolean readOnly) throws SQLException {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void setHoldability(int holdability) throws SQLException {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void setClientInfo(String name, String value) throws SQLClientInfoException {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void setClientInfo(Properties properties) throws SQLClientInfoException {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void setCatalog(String catalog) throws SQLException {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void setAutoCommit(boolean autoCommit) throws SQLException {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void rollback(Savepoint savepoint) throws SQLException {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void rollback() throws SQLException {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void releaseSavepoint(Savepoint savepoint) throws SQLException {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency,
							int resultSetHoldability) throws SQLException {
						// TODO Auto-generated method stub
						return null;
					}
					
					@Override
					public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency)
							throws SQLException {
						// TODO Auto-generated method stub
						return null;
					}
					
					@Override
					public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
						// TODO Auto-generated method stub
						return null;
					}
					
					@Override
					public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
						// TODO Auto-generated method stub
						return null;
					}
					
					@Override
					public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
						// TODO Auto-generated method stub
						return null;
					}
					
					@Override
					public PreparedStatement prepareStatement(String sql) throws SQLException {
						// TODO Auto-generated method stub
						return null;
					}
					
					@Override
					public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency,
							int resultSetHoldability) throws SQLException {
						// TODO Auto-generated method stub
						return null;
					}
					
					@Override
					public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
						// TODO Auto-generated method stub
						return null;
					}
					
					@Override
					public CallableStatement prepareCall(String sql) throws SQLException {
						// TODO Auto-generated method stub
						return null;
					}
					
					@Override
					public String nativeSQL(String sql) throws SQLException {
						// TODO Auto-generated method stub
						return null;
					}
					
					@Override
					public boolean isValid(int timeout) throws SQLException {
						return true;
					}
					
					@Override
					public boolean isReadOnly() throws SQLException {
						// TODO Auto-generated method stub
						return false;
					}
					
					@Override
					public boolean isClosed() throws SQLException {
						// TODO Auto-generated method stub
						return false;
					}
					
					@Override
					public SQLWarning getWarnings() throws SQLException {
						// TODO Auto-generated method stub
						return null;
					}
					
					@Override
					public Map<String, Class<?>> getTypeMap() throws SQLException {
						// TODO Auto-generated method stub
						return null;
					}
					
					@Override
					public int getTransactionIsolation() throws SQLException {
						// TODO Auto-generated method stub
						return 0;
					}
					
					@Override
					public String getSchema() throws SQLException {
						// TODO Auto-generated method stub
						return null;
					}
					
					@Override
					public int getNetworkTimeout() throws SQLException {
						// TODO Auto-generated method stub
						return 0;
					}
					
					@Override
					public DatabaseMetaData getMetaData() throws SQLException {
						// TODO Auto-generated method stub
						return null;
					}
					
					@Override
					public int getHoldability() throws SQLException {
						// TODO Auto-generated method stub
						return 0;
					}
					
					@Override
					public String getClientInfo(String name) throws SQLException {
						// TODO Auto-generated method stub
						return null;
					}
					
					@Override
					public Properties getClientInfo() throws SQLException {
						// TODO Auto-generated method stub
						return null;
					}
					
					@Override
					public String getCatalog() throws SQLException {
						// TODO Auto-generated method stub
						return null;
					}
					
					@Override
					public boolean getAutoCommit() throws SQLException {
						// TODO Auto-generated method stub
						return false;
					}
					
					@Override
					public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
						// TODO Auto-generated method stub
						return null;
					}
					
					@Override
					public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability)
							throws SQLException {
						// TODO Auto-generated method stub
						return null;
					}
					
					@Override
					public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
						// TODO Auto-generated method stub
						return null;
					}
					
					@Override
					public Statement createStatement() throws SQLException {
						// TODO Auto-generated method stub
						return null;
					}
					
					@Override
					public SQLXML createSQLXML() throws SQLException {
						// TODO Auto-generated method stub
						return null;
					}
					
					@Override
					public NClob createNClob() throws SQLException {
						// TODO Auto-generated method stub
						return null;
					}
					
					@Override
					public Clob createClob() throws SQLException {
						// TODO Auto-generated method stub
						return null;
					}
					
					@Override
					public Blob createBlob() throws SQLException {
						// TODO Auto-generated method stub
						return null;
					}
					
					@Override
					public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
						// TODO Auto-generated method stub
						return null;
					}
					
					@Override
					public void commit() throws SQLException {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void close() throws SQLException {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void clearWarnings() throws SQLException {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void abort(Executor executor) throws SQLException {
						// TODO Auto-generated method stub
						
					}
				};
			}
			
			@Override
			public int executeUpdate(String sql, String[] columnNames) throws SQLException {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public int executeUpdate(String sql) throws SQLException {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public ResultSet executeQuery(String sql) throws SQLException {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public int[] executeBatch() throws SQLException {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public boolean execute(String sql, String[] columnNames) throws SQLException {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean execute(String sql, int[] columnIndexes) throws SQLException {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean execute(String sql) throws SQLException {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public void closeOnCompletion() throws SQLException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void close() throws SQLException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void clearWarnings() throws SQLException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void clearBatch() throws SQLException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void cancel() throws SQLException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void addBatch(String sql) throws SQLException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setURL(int parameterIndex, URL x) throws SQLException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setTime(int parameterIndex, Time x) throws SQLException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setString(int parameterIndex, String x) throws SQLException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setShort(int parameterIndex, short x) throws SQLException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setRowId(int parameterIndex, RowId x) throws SQLException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setRef(int parameterIndex, Ref x) throws SQLException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setObject(int parameterIndex, Object x) throws SQLException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setNull(int parameterIndex, int sqlType) throws SQLException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setNString(int parameterIndex, String value) throws SQLException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setNClob(int parameterIndex, Reader reader) throws SQLException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setNClob(int parameterIndex, NClob value) throws SQLException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setLong(int parameterIndex, long x) throws SQLException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setInt(int parameterIndex, int x) throws SQLException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setFloat(int parameterIndex, float x) throws SQLException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setDouble(int parameterIndex, double x) throws SQLException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setDate(int parameterIndex, Date x) throws SQLException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setClob(int parameterIndex, Reader reader) throws SQLException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setClob(int parameterIndex, Clob x) throws SQLException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setBytes(int parameterIndex, byte[] x) throws SQLException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setByte(int parameterIndex, byte x) throws SQLException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setBoolean(int parameterIndex, boolean x) throws SQLException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setBlob(int parameterIndex, Blob x) throws SQLException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setArray(int parameterIndex, Array x) throws SQLException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public ParameterMetaData getParameterMetaData() throws SQLException {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public ResultSetMetaData getMetaData() throws SQLException {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public int executeUpdate() throws SQLException {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public ResultSet executeQuery() throws SQLException {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public boolean execute() throws SQLException {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public void clearParameters() throws SQLException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void addBatch() throws SQLException {
				// TODO Auto-generated method stub
				
			}
		};
    }

    @Override
    public List<DBTable> getSortedTables() throws Exception {
    	return DBTABLES_LIST;
    }

    @Override
    public List<UniqueDefinition> getUniqueList(Name table)
            throws Exception {
        List<UniqueDefinition> list = new LinkedList<UniqueDefinition>();
        return list;
    }

    @Override
    public int getRowCount(Name table) throws Exception {
        return rows;
    }

    public Helper getHelper() {
        return new Helper() {
			
			@Override
			public int translateType(int type) {
				return type;
			}
			
			@Override
			public void setupStatement(Statement stm) throws SQLException {
			}
			
			@Override
			public void setPreparedValue(PreparedStatement pStm, int col, Object o, int type) throws SQLException {
			}
			
			@Override
			public void setNotNull(DBConnection con, String table, String typeName, String column, Integer size)
					throws Exception {
			}
			
			@Override
			public void setDefaultValue(DBConnection con, String table, String typeName, String column, String value)
					throws Exception {
			}
			
			@Override
			public String preLoadSetup(String table) {
				return null;
			}
			
			@Override
			public String postLoadSetup(String table) {
				return null;
			}
			
			@Override
			public Object outputValue(Object value) {
				return value;
			}
			
			@Override
			public String outputValue(String value) {
				return value;
			}
			
			@Override
			public String outputType(String type, Integer size) {
				return type;
			}
			
			@Override
			public String outputName(String type) {
				return type;
			}
			
			@Override
			public String normalizedType(String type) {
				return type;
			}
			
			@Override
			public String normalizeValue(String value) {
				return value;
			}
			
			@Override
			public String normalizeDefault(String string) {
				return string;
			}
			
			@Override
			public boolean isTableValid(Name n) {
				return true;
			}
			
			@Override
			public void initConnection(DBConnection con) throws Exception {
			}
			
			@Override
			public String getParametersHelp() {
				return null;
			}
			
			@Override
			public void fixSequences(DBConnection con, String table, String typeName, String column) throws Exception {
			}
		};
    }
}
