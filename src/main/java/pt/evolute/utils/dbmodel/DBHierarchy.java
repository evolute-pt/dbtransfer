package pt.evolute.utils.dbmodel;

import java.util.List;
import java.util.Vector;

import pt.evolute.utils.object.DefaultLightPropertyObject;
import pt.evolute.utils.tables.ColumnizedObject;

/**
 *
 * @author lflores
 */
public class DBHierarchy extends DefaultLightPropertyObject
		implements ColumnizedObject
{
	public static final String SRC_TABLE = "SRC_TABLE";
	
//	private final ModelProvider provider;
	
	private List<DBTable> tableList = new Vector<DBTable>();
	private String tableListStr = null;
	
	/** Creates a new instance of DBHierarchy */
	public DBHierarchy( ModelProvider provider )
    {
		super( new String[] { SRC_TABLE }, false );
//		this.provider = provider;
    }
	
	public void addTable( DBTable table )
	{
		tableList.add( table );
		tableListStr = null;
	}
	
	public void addReverseTable( DBTable table )
	{
		tableList.add( 0, table );
		tableListStr = null;
	}
	
	@SuppressWarnings("unchecked")
	public Object getValue( int col )
    {
		if( tableListStr == null )
		{
			StringBuffer buffer = new StringBuffer( ( String )tableList.get( tableList.size() - 1 ).get( DBTable.NAME ) );
			for( int i = tableList.size() - 2; i >= 0 ; --i )
			{
				buffer.append( " -> " );
				buffer.append( tableList.get( i ).get( DBTable.NAME ) );
			}
			tableListStr = buffer.toString();
		}
		return tableListStr;
    }
	
	public DBTable[] getTables()
	{
		return tableList.toArray( new DBTable[ tableList.size() ] );
	}
}
