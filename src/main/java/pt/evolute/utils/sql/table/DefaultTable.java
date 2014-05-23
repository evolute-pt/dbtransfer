package pt.evolute.utils.sql.table;

import java.util.HashMap;
import java.util.Map;

import pt.evolute.utils.sql.Field;

/**
 *
 * @author  fpalma
 */
public class DefaultTable 
	implements Table
{
	protected final String NAME;
	protected final Map<String,Field> FIELDS_BY_NAME = new HashMap<String,Field>();
	protected final String []FIELD_NAMES;
	protected final Field []FIELDS;
	protected final String ALIAS;
	protected boolean temp;
	
	public DefaultTable( String name )
	{
		this( name, null );
	}
	
	public DefaultTable( String name, String fieldNames[] )
	{
		this( name, fieldNames, null );
	}
	
	/** Creates a new instance of DefaultTable */
	public DefaultTable( String name, String fieldNames[], String alias )
	{
		NAME = name;
		if( fieldNames == null )
		{
			FIELD_NAMES = new String[ 0 ];
			FIELDS = new Field[ 0 ];
		}
		else
		{
			FIELD_NAMES = fieldNames;
			FIELDS = new Field[ FIELD_NAMES.length ];
		}
		for( int n = 0; n < FIELD_NAMES.length; n++ )
		{
			Field field = new Field( FIELD_NAMES[ n ], this );
			FIELDS_BY_NAME.put( FIELD_NAMES[ n ], field );
			FIELDS[ n ] = field;
		}
		ALIAS = alias;
		temp = false;
	}
	
	public String[] getAllFieldNames()
	{
		return FIELD_NAMES;
	}
	
	
	
	public Field getField(String fieldName)
	{
		return FIELDS_BY_NAME.get( fieldName );
	}
	
	public String getHeader()
	{
		if( ALIAS == null )
		{
			return NAME;
		}
		else
		{
			return ALIAS;
		}
	}

	@Override
	public String toString()
	{
		if( ALIAS == null )
		{
			return NAME;
		}
		else
		{
			return ALIAS;
		}
	}
	
	public String getAlias()
	{
		return ALIAS;
	}
	
	public String getName()
	{
		return NAME;
	}
	
	public void setTemp()
	{
		temp = true;
	}
	
	public boolean isTemp()
	{
		return temp;
	}
	
	public Field[] getAllFields()
	{
		return FIELDS;
	}
	
	public Field[] getFields(String[] names)
	{
		Field fields[] = new Field[ names.length ];
		for( int n = 0; n < names.length; n++ )
		{
			fields[ n ] = FIELDS_BY_NAME.get( names[ n ] );
		}
		return fields;
	}
	
}
