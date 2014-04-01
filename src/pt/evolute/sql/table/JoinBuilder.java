/*
 * JoinBuilder.java
 *
 * Created on 30 de Maio de 2005, 16:17
 */

package pt.evolute.sql.table;

import pt.evolute.sql.Expression;

/**
 *
 * @author  fpalma
 */
public interface JoinBuilder
{
	public String getDatabaseType();
	public String getHeader();
	public Expression getFilter();
}
