package pt.evolute.dbtransfer.db.beans;

import java.util.ArrayList;
import java.util.List;

public class PrimaryKeyDefinition 
{
	public String name;
	public List<ColumnDefinition> columns = new ArrayList<ColumnDefinition>();
}
