/*
 * LightPropertyObject.java
 *
 * Created on 30 de Novembro de 2006, 19:03
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pt.evolute.utils.object;

import java.util.Map;

/**
 *
 * @author fpalma
 */
public interface LightPropertyObject
{
	public Object get(String fieldName);

	public String[] getPropertyNames();

	public Map<String, Object> getMapData();

	public void set(String fieldName, Object value);

	public void setMapData(Map<String, Object> map);
}
