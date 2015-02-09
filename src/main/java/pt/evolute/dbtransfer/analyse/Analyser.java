package pt.evolute.dbtransfer.analyse;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import pt.evolute.dbtransfer.Constants;
import pt.evolute.dbtransfer.db.DBConnection;
import pt.evolute.dbtransfer.db.DBConnector;
import pt.evolute.dbtransfer.db.beans.ColumnDefinition;
import pt.evolute.dbtransfer.db.beans.ConnectionDefinitionBean;
import pt.evolute.dbtransfer.db.beans.Name;
import pt.evolute.dbtransfer.db.helper.Helper;
import pt.evolute.dbtransfer.db.helper.HelperManager;

/**
 *
 * @author  lflores
 */
public class Analyser implements Constants
{	
	private final Name TABLES[];
	private final ConnectionDefinitionBean SRC;
        private final ConnectionDefinitionBean DST;
	private final DBConnection CON_SRC;
//	private final DatabaseMetaData DB_META;
	private final Properties PROPS;
	
	private final Helper SRC_TR;
	private final Helper DEST_TR;
	
	/** Creates a new instance of Analyser
     * @param props
     * @throws java.lang.Exception */
	public Analyser( Properties props, ConnectionDefinitionBean src, ConnectionDefinitionBean dst )
		throws Exception
	{
		PROPS = props;
		SRC = src;
                DST = dst;
                
                boolean ignoreEmpty = Boolean.parseBoolean( PROPS.getProperty( ONLY_NOT_EMPTY, "false" ) );
                
		CON_SRC = DBConnector.getConnection( SRC.getUrl(), SRC.getUser(), SRC.getPassword(), ignoreEmpty, SRC.getSchema() );
		List<Name> v = CON_SRC.getTableList();
		TABLES = v.toArray( new Name[ v.size() ] );
		
		SRC_TR = HelperManager.getTranslator( SRC.getUrl() );
		DEST_TR = HelperManager.getTranslator( props.getProperty( URL_DB_DESTINATION ) );
	}
	
	public void cloneDB()
		throws Exception
	{
		List<StringBuilder> v = new LinkedList<StringBuilder>();
		List<String> v2 = new LinkedList<String>();
		for( int i = 0; i < TABLES.length; ++i )
		{
			List<ColumnDefinition> list = CON_SRC.getColumnList( TABLES[ i ] );
			StringBuilder buff = new StringBuilder( "CREATE TABLE " );
			buff.append( TABLES[ i ].saneName );
			buff.append( " ( " );
			int j = 0;
			for( ColumnDefinition def: list )
			{
				if( j != 0 )
				{
					buff.append( ", " );
				}
				buff.append( DEST_TR.outputName( def.name.saneName ) );
				buff.append( " " );
				if( def.sqlSize != null && def.sqlSize > 10485760 )
				{
					def.sqlSize = 10485760;
				}
				buff.append( translate( def.sqlTypeName, def.sqlSize ) );
/*				if( def.sqlSize != null )
				{
						buff.append( "( " );
						if( translate( def.sqlTypeName ).equals( "varchar" )
								&& def.sqlSize > 10485760 )
						{
							def.sqlSize = 10485760;
						}
						buff.append( def.sqlSize );
						buff.append( " )" );
				}*/
				++j;
			}
			buff.append( " ) " );
			v2.add( "DROP TABLE " + TABLES[ i ] + " CASCADE" );
			v.add( buff );
		}
		
                DBConnection destCon = DBConnector.getConnection( DST.getUrl(), DST.getUser(), DST.getPassword(), false, DST.getSchema() );
		for( int i = 0; i < v.size(); ++i )
		{
			try
			{
				destCon.executeQuery( v2.get( i ) );
			}
			catch( Exception ex )
			{
				// table didn't exist
//				System.out.println( ex.getMessage() );
			}
System.out.println( "T: " + v.get( i ) );
			destCon.executeQuery( v.get( i ).toString() );
		}
	}
	
	private String translate( String type, Integer size )
	{
		return DEST_TR.outputType( SRC_TR.normalizedType( type ), size );
	}
}

