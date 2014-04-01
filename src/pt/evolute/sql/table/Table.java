/*
 * Table.java
 *
 * Created on 2 de Junho de 2005, 16:24
 */

package pt.evolute.sql.table;

import pt.evolute.sql.Field;
/**
 *
 * @author  fpalma
 */
public interface Table extends TableExpression
{
	public String []getAllFieldNames();
	public Field []getAllFields();
	public Field []getFields( String names[] );
	public Field getField( String fieldName );
	public String getName();
	public String getAlias();
	public boolean isTemp();
}
