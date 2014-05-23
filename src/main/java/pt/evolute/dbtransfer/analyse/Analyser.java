package pt.evolute.dbtransfer.analyse;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import pt.evolute.dbtransfer.Constants;
import pt.evolute.dbtransfer.db.DBConnection;
import pt.evolute.dbtransfer.db.DBConnector;
import pt.evolute.dbtransfer.db.beans.ColumnDefinition;
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
	private final String SRC_URL;
	private final DBConnection CON_SRC;
//	private final DatabaseMetaData DB_META;
	private final Properties PROPS;
	
	private final Helper SRC_TR;
	private final Helper DEST_TR;
	
	/** Creates a new instance of Analyser */
	public Analyser( Properties props )
		throws Exception
	{
		PROPS = props;
		SRC_URL = props.getProperty( URL_DB_SOURCE );
		String srcUser = props.getProperty( USER_DB_SOURCE );
		String srcPasswd = props.getProperty( PASSWORD_DB_SOURCE );
		
		boolean ignoreEmpty = Boolean.parseBoolean( PROPS.getProperty( ONLY_NOT_EMPTY, "false" ) );
		
		CON_SRC = DBConnector.getConnection( SRC_URL, srcUser, srcPasswd, ignoreEmpty );
		List<Name> v = CON_SRC.getTableList();
		TABLES = v.toArray( new Name[ v.size() ] );
		
		SRC_TR = HelperManager.getTranslator( SRC_URL );
		DEST_TR = HelperManager.getTranslator( props.getProperty( URL_DB_DESTINATION ) );
	}
	
	public void cloneDB()
		throws Exception
	{
		List<StringBuilder> v = new LinkedList<StringBuilder>();
		List<String> v2 = new LinkedList<String>();
		for( int i = 0; i < TABLES.length; ++i )
		{
//			if( !TABLES[ i ].toLowerCase().equals( "dados_ficha_clinic" ) )
//			{
//				continue;
//			}
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
		String destURL = PROPS.getProperty( URL_DB_DESTINATION );
		String destUser = PROPS.getProperty( USER_DB_DESTINATION );
		String destPasswd = PROPS.getProperty( PASSWORD_DB_DESTINATION );
		
		DBConnection destCon = DBConnector.getConnection( destURL, destUser, destPasswd, false );
		for( int i = 0; i < v.size(); ++i )
		{
			try
			{
				destCon.executeQuery( v2.get( i ).toString() );
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
//		String name = SQL_TYPES.get( type );
//		return name == null? type: name;
		return DEST_TR.outputType( SRC_TR.normalizedType( type ), size );
	}
}

