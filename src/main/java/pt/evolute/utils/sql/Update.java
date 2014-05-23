package pt.evolute.utils.sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import pt.evolute.utils.arrays.Virtual2DArray;
import pt.evolute.utils.db.ExecuterProvider;
import pt.evolute.utils.jdbc.StatementExecuterFactory;
import pt.evolute.utils.sql.backend.Backend;
import pt.evolute.utils.sql.backend.BackendProvider;

public class Update implements UpdateQuery
{
	private String iStatement = null;
	private boolean unicodeFullStatement = false;
//	private boolean strange = false;
	
	private String iTableName;
	private Assignment iAssignments[];
	private String iFromTables[];
	private Expression iWhere;
	
	private Virtual2DArray res = null;
	
	private int batchSize = -1;
	
	private Backend backend = null;
	
	public Update( String tableName, 
			Assignment []assignments, 
			String fromTables[],
			Expression whereExpression )
	{
		iTableName = tableName;
		iAssignments = assignments;
		iFromTables = fromTables;
		iWhere = whereExpression;
	}
	
	public Update( String tableName, 
			Assignment []assignments, 
			Expression whereExpression )
	{
		this( tableName, assignments, null, whereExpression );
	}
	
	public Update( String updateQuery )
	{
		iStatement = updateQuery;
//		strange = true;
	}

	public void setUnicodeFullStatement( boolean translate )
	{
		unicodeFullStatement = translate;
	}

	@Override
	public String toString()
	{
		if( iStatement != null )
		{
			// TODO - called too many times!
//			System.out.println( "Fix UPDATE to: " + getBackend().portSyntax( iStatement ) );
			return toUnicode( getBackend().portSyntax( iStatement ) ).toString();
		}
		if( iTableName == null || iAssignments == null || iAssignments.length == 0 )
		{
			return "";
		}
		StringBuilder statement = new StringBuilder( "UPDATE " );
		statement.append( iTableName );
		statement.append( " SET " ); 
		for( int n = 0; n < iAssignments.length - 1; n++ )
		{
			iAssignments[ n ].setBackend( getBackend() );
//			iAssignments[ n ].getOperand().setUnicode( unicodeFullStatement );
			statement.append( iAssignments[ n ] );
			statement.append( ", " );
		}
//		iAssignments[ iAssignments.length - 1 ].getOperand().setUnicode( unicodeFullStatement );
		iAssignments[ iAssignments.length - 1 ].setBackend( getBackend() );
		statement.append( iAssignments[ iAssignments.length - 1 ] );
		statement.append( " " );
		if( iFromTables != null && iFromTables.length > 0 )
		{
			statement.append( "FROM " );
			statement.append( iFromTables[ 0 ] );
			for( int i = 1; i < iFromTables.length; ++i )
			{
				statement.append( ", " );
				statement.append( iFromTables[ i ] );
			}
			statement.append( " " );
		}
		if( iWhere != null )
		{
			iWhere.setBackend( getBackend() );
			String wClause = iWhere.toString();
			if( !wClause.isEmpty() )
			{
				statement.append( "WHERE " );
				statement.append( wClause );
				statement.append( " " );
			}
		}
		return toUnicode( getBackend().portSyntax( statement ) ).toString();
	}
	
	private CharSequence toUnicode( CharSequence str )
	{
		if( unicodeFullStatement )
		{
			return backend.escapeUnicode( str );
		}
		else
		{
			return str.toString();
		}
	}
	
	@Override
	public void execute()
		throws Exception
	{
		res = StatementExecuterFactory.executeUpdateStatementWithCursor( this.toString(), this );
	}
	
	@Override
	public void execute( ExecuterProvider provider )
		throws Exception
	{
		res = provider.getExecuter().executeQuery( this );
	}
	
	@Override
	public Object[][] getObjects()
	{
		Virtual2DArray resArray = getCursorObjects();
		if( resArray == null )
		{
			return null;
		}
		return resArray.getObjects();
	}
	
	@Override
	public Virtual2DArray getCursorObjects()
	{
		return res;
	}
	
	@Override
	public String[] getBatch()
	{
		String updates[] = new String[ batchSize ];
		for( int i = 0; i < batchSize; ++i )
		{
			for( int j = 0; j < iAssignments.length; ++j )
			{
				if( iAssignments[ j ].isBatch() )
				{
					iAssignments[ j ].currentBatch( i );
				}
			}
			updates[ i ] = toString();
		}
		return updates;
	}
	
	@Override
	public boolean isBatch()
	{
		if( iAssignments == null )
		{
			return false;
		}
		for( int i = 0; i < iAssignments.length; ++i )
		{
			if( iAssignments[ i ].isBatch() )
			{
				batchSize = iAssignments[ i ].getBatchSize();
				return true;
			}
		}
		return false;
	}

	@Override
	public void fillParameters(Object stm)
			throws SQLException
	{
		int i = 1;
		PreparedStatement pstm = ( PreparedStatement )stm;
		if( iAssignments != null )
		{
			for( Assignment a: iAssignments )
			{
				if( a.getOperand().getInnerObject() instanceof byte[] )
				{
					pstm.setBytes( i, ( byte[] )a.getOperand().getInnerObject() );
					++i;
				}
			}
		}
	}
	
	@Override
	public void setBackend( Backend backend )
	{
		this.backend = backend;
	}
	
	protected Backend getBackend()
	{
		if( backend == null )
		{
			new Exception( "NO BACKEND!!!!" ).printStackTrace( System.out );
			setBackend( BackendProvider.getDefaultBackend() );
		}
		return backend;
	}
	
}