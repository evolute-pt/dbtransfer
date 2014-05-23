package pt.evolute.utils.dbmodel;

import java.util.List;

import pt.evolute.utils.object.DefaultLightPropertyObject;
import pt.evolute.utils.tables.ColumnizedObject;

/**
*
* @author lflores
*/
public class DBReference extends DefaultLightPropertyObject 
		implements ColumnizedObject, Cloneable
{
	public static final String NAME = "NAME";
	public static final String SRC_COLUMNS = "SRC_COLUMNS";
	public static final String SRC_TABLE = "SRC_TABLE";
	public static final String DEST_COLUMNS = "DEST_COLUMNS";
	public static final String DEST_TABLE = "DEST_TABLE";

	private final ModelProvider provider;

//	private DBTable destTable = null;

	/** Creates a new instance of DBReference */
	public DBReference( ModelProvider provider )
	{
		super( new String[] { NAME, SRC_COLUMNS, SRC_TABLE, DEST_COLUMNS, DEST_TABLE }, false );
		this.provider = provider;
	}

	public String getXMLRepresentation()
	{
		return null;
	}

	public void setXMLRepresentation()
	{
	}

	@SuppressWarnings("unchecked")
	public Object getValue( int col )
	{
		return get( getPropertyNames()[ col ] );
	}

	public DBTable getDestinationTable()
	{
		return ( DBTable ) get( DEST_TABLE );
	}
	
	@Override
	public Object clone()
	{
		DBReference reference = new DBReference( provider );
		reference.setMapData( getMapData() );
		return reference;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public String toString()
	{
		StringBuffer buff = new StringBuffer();
		String srcTable = "" + get( SRC_TABLE );
		String destTable = "" + get( DEST_TABLE );
		List<DBColumn> srcColumns = ( List<DBColumn> )get( SRC_COLUMNS );
		List<DBColumn> destColumns = ( List<DBColumn> )get( DEST_COLUMNS );
		for( int n = 0; n < srcColumns.size(); n++ )
		{
			buff.append( "[" + srcTable + "." + srcColumns.get( n ).get( DBColumn.NAME ) + " -> " );
			buff.append( destTable + "." + destColumns.get( n ).get( DBColumn.NAME ) + "]" );
		}
		return buff.toString();
	}
}
