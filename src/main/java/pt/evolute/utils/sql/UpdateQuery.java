/*
 * UpdateQuery.java
 *
 * Created on 12 de Junho de 2003, 19:44
 */

package pt.evolute.utils.sql;

import java.sql.SQLException;

/**
 *
 * @author  informatica
 */
public interface UpdateQuery extends SQLQuery
{
	public boolean isBatch();
	
	public String[] getBatch();

	public void fillParameters(Object stm)
			throws SQLException;
}
