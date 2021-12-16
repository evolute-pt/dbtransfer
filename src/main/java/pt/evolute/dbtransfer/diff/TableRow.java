package pt.evolute.dbtransfer.diff;

import java.util.ArrayList;
import java.util.List;

public class TableRow 
{
	public String status = null;
	public String rowMd5 = null;
	public final List<Object> row = new ArrayList<Object>();
}