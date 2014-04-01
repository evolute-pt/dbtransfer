/*
 * Constrainer.java
 *
 * Created on February 8, 2005, 8:04 PM
 */

package dbtransfer.constrain;

import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Properties;

import pt.evolute.db.Connector;
import dbtransfer.Constants;
import dbtransfer.db.DBConnection;
import dbtransfer.db.DBConnector;
import dbtransfer.db.beans.ColumnDefinition;
import dbtransfer.db.beans.ForeignKeyDefinition;
import dbtransfer.db.beans.Name;
import dbtransfer.db.beans.PrimaryKeyDefinition;
import dbtransfer.db.beans.UniqueDefinition;
import dbtransfer.db.helper.Helper;
import dbtransfer.db.helper.HelperManager;
/**
 *
 * @author  lflores
 */
public class Constrainer  extends Connector implements Constants
{
	public static final String CONSTRAIN_KEEP_NAMES = "CONSTRAIN.KEEP_NAMES";
	
	private final Name TABLES[];
	private final String SRC_URL;
	private final DBConnection CON_SRC;
	private final DBConnection CON_DEST;
	
	private final Helper SRC_TR;
	private final Helper DEST_TR;
	
	private final boolean ignoreEmpty;
	
	/** Creates a new instance of Constrainer */
	public Constrainer( Properties props )
		throws Exception
	{
		SRC_URL = props.getProperty( URL_DB_SOURCE );
		String srcUser = props.getProperty( USER_DB_SOURCE );
		String srcPasswd = props.getProperty( PASSWORD_DB_SOURCE );
		ignoreEmpty = Boolean.parseBoolean( props.getProperty( ONLY_NOT_EMPTY, "false" ) );
		
		CON_SRC = DBConnector.getConnection( SRC_URL, srcUser, srcPasswd, ignoreEmpty );
		
		String destURL = props.getProperty( URL_DB_DESTINATION );
		String destUser = props.getProperty( USER_DB_DESTINATION );
		String destPasswd = props.getProperty( PASSWORD_DB_DESTINATION );
		CON_DEST = DBConnector.getConnection( destURL, destUser, destPasswd, false );

		SRC_TR = HelperManager.getTranslator( SRC_URL );
		DEST_TR = HelperManager.getTranslator( props.getProperty( URL_DB_DESTINATION ) );
		
		List<Name> v = CON_SRC.getTableList();
		TABLES = v.toArray( new Name[ v.size() ] );
	}
	
	public void constrainDB()
		throws Exception
	{
         //   boolean after = false;
		for( Name table: TABLES )
		{
			List<ColumnDefinition> list = CON_SRC.getColumnList( table );
			for( ColumnDefinition col: list )
			{
				col.sqlTypeName = DEST_TR.outputType( SRC_TR.normalizedType( col.sqlTypeName ), null );
				constrainColumn( col, table );
			}
			addPrimaryKey( table );	
		}
		for( Name table: TABLES )
		{
			addUniques( table );
		}
		// separate for to run AFTER all primary keys
		for( Name table: TABLES )
		{
			addForeignKeys( table );
		}
	}

	private void addForeignKeys( Name table ) throws Exception
	{
		// FOREIGN KEYS
		List<ForeignKeyDefinition> imported = CON_SRC.getForeignKeyList( table );
System.out.println( "table: " + table.saneName + " has " + imported.size() + " possible fks" );
		for(ForeignKeyDefinition fk : imported)
		{
			if( ignoreEmpty && CON_SRC.getRowCount( fk.columns.get( 0 ).referencedTable ) == 0 )
			{
				continue;
			}
			StringBuilder buff = new StringBuilder("ALTER TABLE ");
			buff.append( table.saneName );
			buff.append(" ADD CONSTRAINT ");
			ColumnDefinition col0 = fk.columns.remove( 0 );
			buff.append( fk.getOutputName() );
			buff.append(" FOREIGN KEY ( ");
			buff.append( col0.name );
			for( ColumnDefinition col: fk.columns )
			{
				buff.append( ", " );
				buff.append(col.name);
			}
			buff.append(" ) REFERENCES ");
			buff.append(col0.referencedTable);
			buff.append("( ");
			buff.append(col0.referencedColumn);
			for( ColumnDefinition col: fk.columns )
			{
				buff.append( ", " );
				buff.append( col.referencedColumn );
			}
			buff.append(" )");
			try
			{
				CON_DEST.executeQuery(buff.toString());
			}
			catch(SQLException ex)
			{
				System.out.println("Error: " + buff);
				if(ex.getMessage().indexOf("ultiple") == -1 
						&& ex.getMessage().indexOf("violates") == -1)
				{
					System.out.println("MSG: " + ex.getMessage());
					throw ex;
				}
			}
		}
	}

	private void addUniques( Name table ) throws Exception
	{
		List<UniqueDefinition> uniqs = CON_SRC.getUniqueList( table );
		for(UniqueDefinition uniq : uniqs)
		{
			if( !"PRIMARY".equals( uniq.getOriginalName() ) )
			{
				StringBuilder buff = new StringBuilder("ALTER TABLE ");
				buff.append( table.saneName );
				buff.append(" ADD CONSTRAINT ");
				buff.append( uniq.getOutputName() );
				buff.append( " UNIQUE ( ");
				buff.append( uniq.columns.get( 0 ) );
				
				for( int i = 1; i < uniq.columns.size(); ++i )
				{
					buff.append( ", " );
					buff.append( uniq.columns.get( i ) );
				}
				buff.append(" )");
				try
				{
					CON_DEST.executeQuery(buff.toString());
				}
				catch(SQLException ex)
				{
					System.out.println("Error: " + buff);
					if(ex.getMessage().indexOf("ultiple") == -1 
							&& ex.getMessage().indexOf("violates") == -1 
							&& ex.getMessage().indexOf( "already exists" ) == -1 )
					{
						System.out.println("MSG: " + ex.getMessage());
						throw ex;
					}
				}
			}
		}
	}
	
	private void addPrimaryKey( Name table ) throws Exception
	{
		// PRIMARY KEY
		PrimaryKeyDefinition key = CON_SRC.getPrimaryKey( table );
		if( !key.columns.isEmpty() )
		{
			StringBuilder buff = new StringBuilder("ALTER TABLE ");
			buff.append( table.saneName );
			buff.append(" ADD PRIMARY KEY ( ");
			buff.append(key.columns.remove(0).name);
			//		buff.append( " )" );
			for(ColumnDefinition col : key.columns)
			{
				buff.append(", ");
				buff.append(col.name);
			}
			buff.append(" )");
			try
			{
				CON_DEST.executeQuery(buff.toString());
			}
			catch(SQLException ex)
			{
				System.out.println("Error: " + buff);
				if(ex.getMessage().indexOf("ultiple") == -1)
				{
					throw ex;
				}
			}
		}
	}

	private void constrainColumn(ColumnDefinition col, Name table ) throws Exception
	{
		// CORRECT SERIAL
		String typeName = col.sqlTypeName;
		
		DEST_TR.fixSequences( CON_DEST, table.saneName, typeName, col.name.saneName );
		
		String defaultValue = col.defaultValue;
		if(defaultValue != null)
		{
			defaultValue = SRC_TR.normalizeValue( defaultValue );
			String value = null;
			switch(col.sqlType)
			{
				case Types.BIT:
					value = defaultValue;
					break;
				case Types.CHAR:
				case Types.VARCHAR:
					value = defaultValue;
//					value = "'" + defaultValue + "'";
					//value = defaultValue; // -> when the source is postgresql the ' is not needed
					break;
				case Types.TINYINT:
				case Types.SMALLINT:
				case Types.INTEGER:
				case Types.NUMERIC:
				case Types.BIGINT:
				case Types.REAL:
				case Types.FLOAT:
				case Types.DOUBLE:
				case Types.DECIMAL:
					value = defaultValue;
					break;
				case Types.DATE:
				case Types.TIME:
				case Types.TIMESTAMP:
					value = DEST_TR.outputValue( defaultValue );
					if( value == null )
					{
						System.out.println("STRANGE TIME TYPE: " + table + "." + col.sqlTypeName + ": " + defaultValue);
					}
					break;
				default:
					System.out.println("STRANGE TYPE: " + table + "." + col.sqlTypeName + ": " + defaultValue);
			}
			if( value != null )
			{
				DEST_TR.setDefaultValue( CON_DEST, table.saneName, typeName, col.name.saneName, value );
			}
		}
		if(col.isNotNull)
		{
			DEST_TR.setNotNull( CON_DEST, table.saneName, typeName, col.name.saneName, col.sqlSize );
		}
	}
}
