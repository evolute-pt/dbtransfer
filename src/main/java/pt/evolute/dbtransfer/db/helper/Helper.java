package pt.evolute.dbtransfer.db.helper;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import pt.evolute.dbtransfer.db.DBConnection;
import pt.evolute.dbtransfer.db.beans.Name;

public interface Helper 
{
    public String outputType(String type, Integer size);

    public String outputName(String type);

    public String normalizedType(String type);

    public String preLoadSetup(String table);

    public String postLoadSetup(String table);

    public void fixSequences(DBConnection con, String table, String typeName, String column)
            throws Exception;

    public void setDefaultValue(DBConnection con, String table, String typeName, String column, String value)
            throws Exception;

    public void setNotNull(DBConnection con, String table, String typeName, String column, Integer size)
            throws Exception;

    public String normalizeValue(String value);

    public String outputValue(String value);

    public Object outputValue(Object value);

    public int translateType(int type);

    public String getParametersHelp();

    public String normalizeDefault(String string);

    public void setPreparedValue(PreparedStatement pStm, int col, Object o, int type) throws SQLException;

    public void setupStatement(Statement stm) throws SQLException;

    public void initConnection( DBConnection con)
            throws Exception;
    
    public boolean isTableValid(Name n);        
}
