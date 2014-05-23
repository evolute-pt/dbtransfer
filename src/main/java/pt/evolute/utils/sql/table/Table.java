package pt.evolute.utils.sql.table;

import pt.evolute.utils.sql.Field;
/**
 *
 * @author  fpalma
 */
public interface Table extends TableExpression
{
	public String []getAllFieldNames();
	public Field []getAllFields();
	public Field []getFields(String names[]);
	public Field getField(String fieldName);
	public String getName();
	public String getAlias();
	public boolean isTemp();
}
