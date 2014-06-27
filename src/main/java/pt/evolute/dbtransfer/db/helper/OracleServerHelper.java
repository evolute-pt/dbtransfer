package pt.evolute.dbtransfer.db.helper;

import java.util.HashMap;
import java.util.Map;

import pt.evolute.dbtransfer.db.DBConnection;
import pt.evolute.dbtransfer.db.beans.Name;
import pt.evolute.dbtransfer.db.jdbc.JDBCConnection;

public class OracleServerHelper extends NullHelper
{
    private static final Map<String,String> OUTPUT = new HashMap<String,String>();
    private static final Map<String,String> RESERVED = new HashMap<String,String>();
    private static final Map<String,String> NORMALIZE = new HashMap<String,String>();
    private static final Map<String,String> DEFAULTS = new HashMap<String,String>();
    private static final Map<String,String> NORMALIZE_DEFAULTS = new HashMap<String,String>();

    static
    {
        OUTPUT.put( "varchar", "varchar2" );
        OUTPUT.put( "numeric", "number" );
        OUTPUT.put( "text", "nclob" );

        NORMALIZE.put( "varchar2", "varchar" );
        NORMALIZE.put( "number", "numeric" );
        NORMALIZE.put( "long", "text" );
    }

    private static OracleServerHelper translator = null;

    private OracleServerHelper()
    {
//		System.out.println( "Oracle helper - setting double slash on UnicodeChecker" );
//		UnicodeChecker.setUseDoubleSlash( true );
    }

    public static OracleServerHelper getTranslator()
    {
        if( translator == null )
        {
                translator = new OracleServerHelper();
        }
        return translator;
    }

    @Override
    public String outputType( String type, Integer size )
    {
        String output = OUTPUT.get( type.toLowerCase() );
        if( output == null )
        {
            output = type;
        }

        return output.toUpperCase();
    }

    @Override
    public String outputName( String name )
    {
        if( name.contains( " " ) )
        {
                name = name.replace( ' ', '_' );
        }
        if( name.contains( "." ) )
        {
                name = name.replace( '.', '_' );
        }
//		name = StringPlainer.convertString( name );
//		if( RESERVED.containsKey( name ) )
//		{
                name = "\"" + name + "\"";
//		}
        return name;
    }

    @Override
    public String normalizedType( String type )
    {
        String normalize = NORMALIZE.get( type.toLowerCase() );
        if( normalize == null )
        {
            normalize = type;
        }
        return normalize.toUpperCase();
    }

    @Override
    public void fixSequences( DBConnection con, String table, String typeName, String column )
            throws Exception
    {

    }

    @Override
    public String normalizeValue( String value )
    {
        if( value != null )
        {
        }
        return value;
    }

    @Override
    public void setDefaultValue( DBConnection con, String table, String typeName, String column, String value )
                    throws Exception
    {
        if( DEFAULTS.containsKey( value ) )
        {
                value = DEFAULTS.get( value );
        }
        if( value != null && !value.isEmpty() )
        {
                try
                {
                        super.setDefaultValue( con, table, typeName, column, value );
                }
                catch( Exception ex )
                {
                        System.out.println( "type: <" + typeName + "> value: <" + value + ">" ); 
                        throw ex;
                }
        }
    }

    @Override
    public int translateType( int type ) 
    {
        int pType = type;
        switch ( type ) 
        {
                default:
                        break;
        }
        return pType;
    }

    @Override
    public String normalizeDefault( String str )
    {
        String norm = NORMALIZE_DEFAULTS.get( str );
        if( norm == null )
        {
                norm = str;
        }
        return norm;
    }

    @Override
    public void initConnection( DBConnection con) throws Exception 
    {
        JDBCConnection con2 = ( JDBCConnection )con;
        con2.executeQuery( "ALTER SESSION SET CURRENT_SCHEMA=" + con2.getSchema() /*+ ";"*/ );
        con2.executeQuery( "ALTER SESSION SET nls_date_format='RR.MM.DD'" );
    }
    
    @Override
    public boolean isTableValid( Name name )
    {
        return !name.saneName.contains( "==" );
    }
}
