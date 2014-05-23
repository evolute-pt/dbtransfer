package pt.evolute.utils.dbmodel;

import pt.evolute.utils.object.DefaultLightPropertyObject;
import pt.evolute.utils.tables.ColumnizedObject;


/**
*
* @author lflores
*/
public class DBColumn extends DefaultLightPropertyObject
		implements ColumnizedObject, Cloneable
{
	public static final String TABLE = "TABLE";
	public static final String NAME = "NAME";
	public static final String TYPE = "TYPE";
	public static final String TYPE_ID = "TYPE_ID";
	public static final String LENGTH = "LENGTH";
	public static final String IS_FOREIGN_KEY = "IS_FOREIGN_KEY";
	public static final String IS_PRIMARY_KEY = "IS_PRIMARY_KEY";

	public DBColumn()
	{
		super( new String[] { TABLE, NAME, TYPE, TYPE_ID, LENGTH, IS_FOREIGN_KEY, IS_PRIMARY_KEY }, true );
	}

	@SuppressWarnings("unchecked")
	public Object getValue( int col )
	{
		return get( getPropertyNames()[ col ] );
	}
	
	@Override
	public Object clone()
	{
		DBColumn column = new DBColumn();
		column.setMapData( getMapData() );
		return column;
	}
	@Override
	public String toString()
	{
		return ( String ) get( NAME ) + "PK: " + get( IS_PRIMARY_KEY ) + " FK: " + get( IS_FOREIGN_KEY );// + " - " + hashCode();
	}
}
