package pt.evolute.dbtransfer.analyse;

import java.util.LinkedList;
import java.util.List;

import pt.evolute.dbtransfer.Config;
import pt.evolute.dbtransfer.ConfigurationProperties;
import pt.evolute.dbtransfer.db.DBConnection;
import pt.evolute.dbtransfer.db.DBConnector;
import pt.evolute.dbtransfer.db.beans.ColumnDefinition;
import pt.evolute.dbtransfer.db.beans.ConnectionDefinitionBean;
import pt.evolute.dbtransfer.db.beans.TableDefinition;
import pt.evolute.dbtransfer.db.helper.Helper;
import pt.evolute.dbtransfer.db.helper.HelperManager;

/**
 *
 * @author  lflores
 */
public class Analyser implements ConfigurationProperties
{	
	private final TableDefinition TABLES[];
	private final ConnectionDefinitionBean SRC;
    private final ConnectionDefinitionBean DST;
	private final DBConnection CON_SRC;
//	private final DatabaseMetaData DB_META;
	
	private final Helper SRC_TR;
	private final Helper DEST_TR;
	
	/** Creates a new instance of Analyser
     * @param props
     * @throws java.lang.Exception */
	public Analyser( ConnectionDefinitionBean src, ConnectionDefinitionBean dst )
		throws Exception
	{
		SRC = src;
        DST = dst;
        if( Config.debug() )
        {
        	System.out.println( "Analyser source: " + SRC );
        	System.out.println( "Analyser destination: " + DST );
        }
        
        boolean ignoreEmpty = Config.ignoreEmpty();
        
		CON_SRC = DBConnector.getConnection( SRC.getUrl(), SRC.getUser(), SRC.getPassword(), ignoreEmpty );
		List<TableDefinition> v = CON_SRC.getTableList();
		TABLES = v.toArray( new TableDefinition[ v.size() ] );
		
		SRC_TR = HelperManager.getTranslator( SRC.getUrl() );
		DEST_TR = HelperManager.getTranslator( DST.getUrl() );
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
			buff.append( DEST_TR.getCreateTablePrefix() );
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
			buff.append( ") " );
			v2.add( DEST_TR.getDropTable( TABLES[ i ].toString() ) );
			v.add( buff );
		}
		
        DBConnection destCon = DBConnector.getConnection( DST.getUrl(), DST.getUser(), DST.getPassword(), false );
		
        for( int i = 0; i < v.size(); ++i )
		{
			try
			{
//				destCon.executeQuery( DEST_TR.getBegin() );
				destCon.executeQuery( v2.get( i ) );
//				destCon.executeQuery( DEST_TR.getCommit() );
			}
			catch( Exception ex )
			{
				// table didn't exist
				System.out.println( ex.getMessage() );
//				destCon.executeQuery( DEST_TR.getRollback() );
			}
System.out.println( "T: " + v.get( i ) );
//			destCon.executeQuery( DEST_TR.getBegin() );
			destCon.executeQuery( v.get( i ).toString() );
//			destCon.executeQuery( DEST_TR.getCommit() );
		}
	}
	
	private String translate( String type, Integer size )
	{
		return DEST_TR.outputType( SRC_TR.normalizedType( type ), size );
	}
}

