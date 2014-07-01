package pt.evolute.dbtransfer.db.helper;

import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;

import pt.evolute.dbtransfer.db.DBConnection;
import pt.evolute.dbtransfer.db.beans.Name;

public class NullHelper implements Helper
{
    private static NullHelper translator = null;

    protected NullHelper()
    {
    }

    public static NullHelper getTranslator()
    {
            if( translator == null )
            {
                    translator = new NullHelper();
            }
            return translator;
    }

    @Override
    public String outputType( String type, Integer size )
    {
            if( size != null )
            {
                    type = type + "( " + size + ")";
            }
            return type;
    }

    @Override
    public String outputName( String name )
    {
            return name;
    }

    @Override
    public String normalizedType( String type )
    {
            return type;
    }

    @Override
    public String preLoadSetup( String table )
    {
            return null;
    }

    @Override
    public String postLoadSetup( String table )
    {
            return null;
    }

    public void fixSequences( DBConnection con, String table, String typeName, String column )
            throws Exception
    {
    }

    public void setDefaultValue( DBConnection con, String table, String typeName, String column, String value )
            throws Exception
    {
            StringBuilder buff = new StringBuilder("ALTER TABLE ");
            buff.append( table );
            buff.append(" ALTER COLUMN ");
            buff.append( outputName( column ) );
            buff.append(" SET DEFAULT ");
            buff.append(value);
            try
            {
//			System.out.println("C: " + buff);
                    con.executeQuery(buff.toString());
            }
            catch(SQLException ex)
            {
                    if(ex.getMessage().contains("ultiple"))
                    {
                            throw ex;
                    }
            }
    }

    public void setNotNull( DBConnection con, String table, String typeName, String column, Integer size )
            throws Exception
    {
            StringBuilder buff = new StringBuilder("ALTER TABLE ");
            buff.append( table );
            buff.append(" ALTER COLUMN ");
            buff.append(column);
            buff.append(" SET NOT NULL");
            try
            {
//			System.out.println("C: " + buff);
                    con.executeQuery(buff.toString());
            }
            catch(SQLException ex)
            {
                    if(ex.getMessage().contains("ultiple"))
                    {
                            System.out.println("EX: " + table + "-" + column + ": " + ex.getMessage());
                            //							throw ex;
                    }
            }
    }

    @Override
    public String normalizeValue( String value )
    {
            return value;
    }

    @Override
    public String outputValue( String value )
    {
            return value;
    }

    @Override
    public Object outputValue( Object value )
    {
            return value;
    }

    @Override
    public int translateType(int type) 
    {
            return type;
    }

    @Override
    public String normalizeDefault(String string) 
    {
            return string;
    }

    @Override
    public String getParametersHelp() {
            return null;
    }

    @Override
    public void setPreparedValue(PreparedStatement pStm, int col, Object o, int type ) 
            throws SQLException
    {
        if(type == Types.TIME)
        {
            o = new Timestamp(((Time) o).getTime());
            type = Types.TIMESTAMP;
        }
        else if( ( type == Types.BOOLEAN || type == Types.BIT ) && o != null )
        {
            o = "1".equals( "" + o ) || "true".equals( "" + o ) || "t".equals( "" + o )? "1": "0";
        }
        if(o == null)
        {
            pStm.setNull(col + 1, translateType( type ) );
        }
        else if( type == Types.BLOB || type == Types.LONGVARBINARY || type == Types.VARBINARY )
        {
            o = outputValue( o );
            if( o instanceof byte[] )
            {
                pStm.setBytes( col + 1, ( byte[] )o );
            }
            else
            {
                pStm.setBlob( col + 1, ( Blob )o );
            }
        }
        else
        {
//                    System.out.println( "SPV: " + o + " t: " + type );
            pStm.setObject(col + 1, outputValue( o ), translateType( type ) );
        }
    }

    @Override
    public void setupStatement(Statement stm) 
            throws SQLException
    {
    }

    public void initConnection( DBConnection con) throws Exception 
    {
    }

    public boolean isTableValid(Name n) 
    {
        return true;
    }
}
