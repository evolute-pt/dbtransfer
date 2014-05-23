/*
 * DefaultLightPropertyObject.java
 *
 * Created on 30 de Novembro de 2006, 19:04
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pt.evolute.utils.object;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 *
 * @author fpalma
 */
public class DefaultLightPropertyObject
		implements LightPropertyObject
{
	protected static final String NO_PROPERTIES[] = new String[ 0 ];
	protected final String PROPERTIES[];
	protected final List<String> PROPERTY_LIST = new ArrayList<String>();
	protected final boolean ENFORCE_PROPERTY_LIST;
	
	protected final HashMap<String,Object> DATA = new HashMap<String,Object>();
	
	/** Creates a new instance of DefaultLightPropertyObject */
	public DefaultLightPropertyObject()
	{
		this( NO_PROPERTIES, false );
	}
	
	public DefaultLightPropertyObject( String properties[], boolean enforcePropertyList )
	{
		if( properties == null )
		{
			PROPERTIES = NO_PROPERTIES;
		}
		else
		{
			PROPERTIES = properties;
		}
		ENFORCE_PROPERTY_LIST = enforcePropertyList;
		PROPERTY_LIST.addAll( Arrays.asList( PROPERTIES ) );
	}

	public Object get(String property)
	{
		if( ENFORCE_PROPERTY_LIST && !PROPERTY_LIST.contains( property ) )
		{
			throw new InvalidPropertyException( property, "Valid properties are: " + PROPERTY_LIST );
		}
		else
		{
			return DATA.get( property );
		}
	}

	public void set(String property, Object value)
	{
		if( ENFORCE_PROPERTY_LIST && !PROPERTY_LIST.contains( property ) )
		{
			throw new InvalidPropertyException( property, "Valid properties are: " + PROPERTY_LIST );
		}
		else
		{
			DATA.put( property, value );
		}
	}

	@Override
	public void setMapData(Map<String, Object> map)
	{
		DATA.putAll( map );
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> getMapData()
	{
		return (Map<String, Object>) DATA.clone();
	}

	public String[] getPropertyNames()
	{
		return PROPERTIES;
	}
	
	@Override
	public boolean equals( Object other )
	{
		if( !( other instanceof DefaultLightPropertyObject ) )
		{
			return false;
		}
		DefaultLightPropertyObject propertyObject = 
				( DefaultLightPropertyObject ) other;
		return ( ENFORCE_PROPERTY_LIST == propertyObject.ENFORCE_PROPERTY_LIST ) &&
				( getMapData().equals( propertyObject.getMapData() ) ) &&
				( PROPERTY_LIST.equals( propertyObject.PROPERTY_LIST ) ) ;
	}
}
