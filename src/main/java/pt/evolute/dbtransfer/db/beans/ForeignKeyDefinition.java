package pt.evolute.dbtransfer.db.beans;

import java.util.ArrayList;
import java.util.List;

import pt.evolute.dbtransfer.constrain.Constrainer;
import pt.evolute.dbtransfer.db.helper.HelperManager;

public class ForeignKeyDefinition 
{
	private static final boolean honorNames;
	static
	{
		honorNames = "true".equals( HelperManager.getProperties().get( Constrainer.CONSTRAIN_KEEP_NAMES ) );
	}
	
	public final Name table;
	public final String name;
	public final List<ColumnDefinition> columns = new ArrayList<ColumnDefinition>();
	
	public String outputName = null;
	
	public ForeignKeyDefinition( String n, Name t )
	{
		name = n;
		table = t;
	}
	
	public String getOutputName()
	{
		if( outputName == null )
		{
			if( honorNames && name != null && !name.isEmpty() )
			{
				outputName = name;
			}
			else
			{	
				outputName = table.saneName + "_";
				for( ColumnDefinition col: columns )
				{
					outputName += col.name + "_" + col.referencedTable + "_" + col.referencedColumn + "_";
				}
				outputName += "fk";
			}
		}
		return outputName;
	}
	
	public String getOriginalName()
	{
		return name;
	}
}
