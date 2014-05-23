package pt.evolute.utils.sql;

import pt.evolute.utils.sql.backend.Backend;
import pt.evolute.utils.sql.table.DefaultTable;
import pt.evolute.utils.sql.table.Table;

public class Field extends Operand
{
	private final String iName;
	private final Table iTable;
	
	public static Field createNewField( String name )
	{
		return new Field(name);
	}
	
	public static Field createNewField( String name, Table table )
	{
		return new Field( name, table );
	}
	
	public static Field createNewField( String name, String tablename )
	{
		return new Field( name, tablename );
	}

	public Field( String name )
	{
		this( name, ( Table )null );
	}
	
	public Field( String name, Table table )
	{
		super( name );
		iName = name;
		iTable = table;
	}

	public Field( String column, String tablename )
	{
		this( column, new DefaultTable( tablename ) );
	}

	public String getName()
	{
		return iName;
	}
	
	public Assignment assign( Object value )
	{
		return new Assignment( this, value );
	}

	@Override
	public String toString()
	{
		return ( iTable != null ? iTable.toString() + "." : "" ) + getEscapedName( getBackend() );
	}
	
	private CharSequence getEscapedName( Backend backend )
	{
		return backend.getEscapedFieldName( getName() );
	}
	
	public Table getTable()
	{
		return iTable;
	}
}