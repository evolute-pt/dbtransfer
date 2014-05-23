package pt.evolute.dbtransfer.db.beans;

import java.util.ArrayList;
import java.util.List;

import pt.evolute.dbtransfer.constrain.Constrainer;
import pt.evolute.dbtransfer.db.helper.HelperManager;

public class UniqueDefinition 
{
	private static final boolean honorNames;
	static
	{
		honorNames = "true".equals( HelperManager.getProperties().get( Constrainer.CONSTRAIN_KEEP_NAMES ) );
	}
	
	public final Name table;
	public final String name;
	public final List<String> columns = new ArrayList<String>();
	
	public String outputName = null;
	
	public UniqueDefinition( String n, Name t )
	{
		name = n;
		table = t;
	}
	
	public String getOutputName()
	{
		if( outputName == null )
		{
			if( honorNames )
			{
				outputName = name;
			}
			else
			{
				outputName = table.saneName + "_";
				for( String col: columns )
				{
					outputName += col + "_";
				}
				outputName += "uniq";
			}
		}
		return outputName;
	}
	
	public String getOriginalName()
	{
		return name;
	}
}
