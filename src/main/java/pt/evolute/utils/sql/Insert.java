package pt.evolute.utils.sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import pt.evolute.utils.arrays.Virtual2DArray;
import pt.evolute.utils.db.ExecuterProvider;
import pt.evolute.utils.jdbc.StatementExecuterFactory;
import pt.evolute.utils.sql.backend.Backend;
import pt.evolute.utils.sql.backend.BackendProvider;

public class Insert implements UpdateQuery
{
	private static final int REGULAR = 0;
	private static final int SELECT = 1;

	private final String iTableName;
	private final Assignment []iAssignments;
	private final Field []iFields;
	private final Select iSelect;
	private final int iInsertType;
	private boolean unicodeFullStatement = false;
//	private AutoKeyRetriever autoKeyRetriever = null;
	
	private Virtual2DArray res;
	
	private final String iStatement;

	private int batchSize = -1;
	
	private Backend backend = null;
	
	public Insert( String insertQuery )
	{
		iStatement = insertQuery;
		iTableName = null;
		iAssignments = null;
		iFields = null;
		iSelect = null;
		iInsertType = REGULAR;
	}
	
	public Insert( String tableName, Assignment []assignments )
	{
		iStatement = null;
		iTableName = tableName;
		iAssignments = assignments;
		iInsertType = REGULAR;
		iSelect = null;
		iFields = null;
	}
	
	public Insert( String tableName, Field []fields,
					Select select )
	{
		iStatement = null;
		iTableName = tableName;
		iAssignments = null;
		iFields = fields;
		iSelect = select;
		iInsertType = SELECT;
	}
	
/*	public void setKeyRetriever( AutoKeyRetriever keyRetriever )
	{
		autoKeyRetriever = keyRetriever;
	}
*/	
	public void setUnicodeFullStatement( boolean translate )
	{
		unicodeFullStatement = translate;
	}
	
	@Override
	public String toString()
	{
		if( iStatement != null )
		{
			return toUnicode( iStatement ).toString();
		}
		
		if( iTableName == null || iTableName.length() == 0 )
		{
			return null;
		}
		StringBuilder insertStr = new StringBuilder( "INSERT INTO " ).append( iTableName );
		
		switch( iInsertType )
		{
			case REGULAR:
				toStringRegular( insertStr );
				break;
			case SELECT:
				toStringSelect( insertStr );
				break;
			default:	
				return null;
		}
		return toUnicode( insertStr ).toString();
	}
	
	private void toStringRegular( StringBuilder builder )
	{
		if( iAssignments == null || iAssignments.length == 0 )
		{
			builder.append( " DEFAULT VALUES;" );
		}
		else
		{
			builder.append( "(" );
			StringBuilder values = new StringBuilder( " ) VALUES ( " );
			for( int n = 0; n < iAssignments.length; n++ )
			{
				iAssignments[ n ].setBackend( backend );
//				iAssignments[ n ].getOperand().setUnicode( unicode );
				builder.append( iAssignments[ n ].getLeft() );
				values.append( iAssignments[ n ].getRight() );
				if( n < iAssignments.length - 1 )
				{
					builder.append( ", " );
					values.append( ", " );
				}
			}
			builder.append( values );
			builder.append( " );" );
		}
	}
	
	private void toStringSelect( StringBuilder builder )
	{
		String selectStr = iSelect.toString();
		if( iFields == null || iFields.length == 0 ||
			selectStr == null || selectStr.length() == 0 )
		{
			builder.append( " DEFAULT VALUES;" );
		}
		else
		{
			builder.append( "(" );
			for( int n = 0; n < iFields.length; n++ )
			{
				builder.append( iFields[ n ].toString() );
				if( n < iFields.length - 1 )
				{
					builder.append( ", " );
				}
			}
			builder.append( " ) " );
			builder.append( selectStr );
		}
	}

	private CharSequence toUnicode( CharSequence str )
	{
		if( unicodeFullStatement )
		{
			return backend.escapeUnicode( str );
		}
		else
		{
			return str;
		}
	}
	
	public void execute()
		throws Exception
	{
		res = StatementExecuterFactory.executeUpdateStatementWithCursor( this.toString(), this );
	}
	
	public void execute( ExecuterProvider provider )
		throws Exception
	{
		res = provider.getExecuter().executeQuery( this );
	}
	
	public Object[][] getObjects()
	{
		return res.getObjects();
	}
	
	public Virtual2DArray getCursorObjects()
	{
		return res;
	}
		
/*	public AutoKeyRetriever getKeyRetriever()
	{
		return autoKeyRetriever;
	}
*/	
	public String[] getBatch()
	{
		String inserts[] = new String[ batchSize ];
		for( int i = 0; i < batchSize; ++i )
		{
			for( int j = 0; j < iAssignments.length; ++j )
			{
				if( iAssignments[ j ].isBatch() )
				{
					iAssignments[ j ].currentBatch( i );
				}
			}
			inserts[ i ] = toString();
		}
		return inserts;
	}
	
	public boolean isBatch()
	{
		if( iAssignments != null )
		{
			for( int i = 0; i < iAssignments.length; ++i )
			{
				if( iAssignments[ i ].isBatch() )
				{
					batchSize = iAssignments[ i ].getBatchSize();
					return true;
				}
			}
		}
		return false;
	}

	public void fillParameters(Object stm)
			throws SQLException
	{
		int i = 1;
		PreparedStatement pstm = ( PreparedStatement )stm;
		if( iAssignments != null )
		{
			for( Assignment a: iAssignments )
			{
				a.getOperand().setBackend( getBackend() );
//				a.getOperand().setUnicode( unicode );
				if( a.getOperand().getInnerObject() instanceof byte[] )
				{
					pstm.setBytes( i, ( byte[] )a.getOperand().getInnerObject() );
					++i;
				}
			}
		}
	}
	
	public void setBackend( Backend backend )
	{
		this.backend = backend;
	}
	
	protected Backend getBackend()
	{
		if( backend == null )
		{
			new Exception( "NO BACKEND!!!!" ).printStackTrace( System.out );
			backend = BackendProvider.getDefaultBackend();
		}
		return backend;
	}
}