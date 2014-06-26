/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pt.evolute.dbtransfer.db.beans;

import java.util.Properties;

/**
 *
 * @author lflores
 */
public class ConnectionDefinitionBean 
{
    private final String url;
    private final String user;
    private final String passwd;
    private final String schema;
    
    public ConnectionDefinitionBean( String jdbcUrl, String usr, String pass, String dbSchema )
    {
        url = jdbcUrl;
        user = usr;
        passwd = pass;
        schema = dbSchema;
    }
    
    public String getUrl()
    {
        return url;
    }
    
    public String getUser()
    {
        return user;
    }
    
    public String getPassword()
    {
        return passwd;
    }
    
    public String getSchema()
    {
        return schema;
    }
    
    public static ConnectionDefinitionBean loadBean( Properties props, String pro[] )
    {
        if( pro == null || pro.length != 4 )
        {
            throw new RuntimeException( "pro[] must be not null and have 4 elements" );
        }
        return new ConnectionDefinitionBean( props.getProperty( pro[ 0 ] ), 
                props.getProperty( pro[ 1 ] ), props.getProperty( pro[ 2 ] ), 
                props.getProperty( pro[ 3 ] ) );
    }
}
