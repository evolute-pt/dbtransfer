package pt.evolute.dbtransfer.diff;

import java.util.ArrayList;
import java.util.List;

import pt.evolute.dbtransfer.db.PrimaryKeyValue;

public class TableRow 
{
	private static final String STATUS_INSERTED = "i";
	private static final String STATUS_UPDATED = "u";
	private static final String STATUS_DELETED = "d";
	
	private String status = null;
	private String rowMd5 = null;
	public final List<Object> row = new ArrayList<Object>();

	private void updateMd5()
	{
		StringBuilder sb = new StringBuilder( "*" );
		for( Object o: row )
		{
			if( o == null )
			{
				sb.append( "null" );
			}
			else if( o instanceof java.sql.Date )
			{
				sb.append( PrimaryKeyValue.D_F.format( o ) );
			}
			else if( o instanceof java.sql.Time )
			{
				sb.append( PrimaryKeyValue.T_F.format( o ) );
			}
			else if( o instanceof java.sql.Timestamp )
			{
				sb.append( PrimaryKeyValue.TS_F.format( o ) );
			}
			else
			{
				sb.append( o );
			}
			sb.append( "*" );
		}
		rowMd5 = sb.toString();
	}
	
	public String getMd5()
	{
		if( rowMd5 == null )
		{
			updateMd5();
		}
		return rowMd5;
	}
	
	public boolean isDeleted()
	{
		return STATUS_DELETED.equals( status );
	}
	
	public void setStatus( String s )
		throws Exception
	{
		if( !STATUS_INSERTED.equals( s )
				&& !STATUS_UPDATED.equals( s )
				&& !STATUS_DELETED.equals( s ))
		{
			throw new Exception( "Invalid status for row: " + s );
		}
		status = s;
	}
}