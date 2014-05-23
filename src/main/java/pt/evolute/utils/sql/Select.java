package pt.evolute.utils.sql;

import pt.evolute.utils.Singleton;
import pt.evolute.utils.arrays.Virtual2DArray;
import pt.evolute.utils.db.DBConstants;
import pt.evolute.utils.db.ExecuterProvider;
import pt.evolute.utils.jdbc.StatementExecuterFactory;
import pt.evolute.utils.sql.backend.Backend;
import pt.evolute.utils.sql.backend.BackendProvider;
import pt.evolute.utils.sql.table.JoinExpression;
import pt.evolute.utils.sql.table.Table;
import pt.evolute.utils.sql.table.TableExpression;

public class Select 
	implements SQLQuery, DBConstants
{
	private Virtual2DArray res = null;
	
	private Object iTablesAndJoins[];
	private Object iFields[];
	private Expression iWhere;
	private Object iOrderFields[];
	private Object iGroupFields[];
	private Expression iHavingExpression;
	private Table iIntoClause;
	private boolean unicode = false;
	private final boolean strange;
	private String databaseType;
	private boolean verifyOrderFields = true;

	private Backend backend = null;
	
	public Select( TableExpression tableExpressions[],
					Field fields[],
					Expression whereExpression,
					Field orderFields[],
					Field groupFields[],
					Expression havingExpression,
					Table intoClause )
	{
		iTablesAndJoins = tableExpressions;
		iFields = fields;
		iWhere = whereExpression;
		iOrderFields = orderFields;
		iGroupFields = groupFields;
		iHavingExpression = havingExpression;
		iIntoClause = intoClause;
		databaseType = ( String ) Singleton.getInstance( Singleton.DEFAULT_DATABASE_TYPE );
		iStatement = null;
		strange = false;
	}
	
	private final String iStatement;
	
	public Select( Object tablesAndJoins[],
					String fieldNames[],
					Expression whereExpression,
					String orderFields[],
					String groupFields[] )
	{
		iTablesAndJoins = tablesAndJoins;
		iFields = fieldNames;
		iWhere = whereExpression;
		iOrderFields = orderFields;
		iGroupFields = groupFields;
		databaseType = ( String ) Singleton.getInstance( Singleton.DEFAULT_DATABASE_TYPE );
		iStatement = null;
		strange = false;
	}
	
	public Select( Object tablesAndJoins[], String fieldNames[],
					Expression whereExpression )
	{
		this( tablesAndJoins, fieldNames, whereExpression, null, null );
	}
	
	public Select( String strangeSelect )
	{
		iStatement = strangeSelect;
		strange = true;
		databaseType = ( String ) Singleton.getInstance( Singleton.DEFAULT_DATABASE_TYPE );
	}
	
	@Override
	public String toString()
	{
		if( strange )
		{
			return toUnicode( getBackend().portSyntax( iStatement ) ).toString();
		}
		if( iTablesAndJoins == null || iTablesAndJoins.length == 0 || 
			iFields == null || iFields.length == 0 )
		{
			return "";
		}
		StringBuilder statement = new StringBuilder( "SELECT " );
		for( int n = 0; n < iFields.length - 1; n++ )
		{
			if( iFields[ n ] instanceof Operand )
			{
				Operand op = ( Operand )iFields[ n ];
				op.setBackend( backend );
				statement.append( op.toHeaderString() );
			}
			else
			{
				statement.append( iFields[ n ] );
			}
			statement.append( ", " );
		}
		if( iFields[ iFields.length - 1 ] instanceof Operand )
		{
			Operand op = (Operand) iFields[ iFields.length - 1 ];
			op.setBackend( backend );
			statement.append( op.toHeaderString() );
		}
		else
		{
			statement.append( iFields[ iFields.length - 1 ] );
		}
		statement.append( " " );
		// check fields in order by
		if( verifyOrderFields && iOrderFields != null && iOrderFields.length > 0 )
		{
			for( int i = 0; i < iOrderFields.length; ++i )
			{
				if( iOrderFields[ i ] instanceof FieldIndex )
				{
					continue;
				}
				String field = iOrderFields[ i ].toString();
				int k = field.indexOf( " " );
				if( k != -1 )
				{
					field = field.substring( 0, k );
				}
				boolean found = false;
				for( int j = 0; j < iFields.length; ++j )
				{
					if( field.equals( iFields[ j ].toString() ) )
					{
						found = true;
						break;
					}
				}
				if( !found )
				{
					statement.append( ", " );
					statement.append( field );
				}
			}
		}
		if( iIntoClause != null && databaseType != null && databaseType.equals( DB_POSTGRESQL ) )
		{
			statement.append( " INTO " );
			if( iIntoClause.isTemp() )
			{
				statement.append( "TEMP " );
			}
			statement.append( iIntoClause );
		}
		statement.append( " FROM " );
		if( iTablesAndJoins != null )
		{
			for( int n = 0; n < iTablesAndJoins.length; n++ )
			{
				if( iTablesAndJoins[ n ] instanceof JoinExpression )
				{
					((JoinExpression)iTablesAndJoins[ n ]).setDatabaseType( databaseType );
					Expression addWhere = ((JoinExpression)iTablesAndJoins[ n ]).getWhereExpression();
					if( addWhere != null )
					{
						if( iWhere == null )
						{
							iWhere = addWhere;
						}
						else
						{
							iWhere = iWhere.and( addWhere );
						}
					}
				}
				statement.append( iTablesAndJoins[ n ] );
				statement.append( n == iTablesAndJoins.length - 1 ? " " : ", " );
			}
		}
		
		if( iWhere != null )
		{
			iWhere.setBackend( backend );
			String wClause = iWhere.toString();
			if( !wClause.isEmpty() )
			{
				statement.append( " WHERE " );
				statement.append( wClause );
				statement.append( " " );
			}
		}
		if( iGroupFields != null && iGroupFields.length > 0 )
		{
			statement.append( " GROUP BY " );
			for( int n = 0; n < iGroupFields.length - 1; n++ )
			{
				statement.append( iGroupFields[ n ] );
				statement.append( ", " );
			}
			statement.append( iGroupFields[ iGroupFields.length - 1 ] );
			statement.append( " " );
		}
		if( iHavingExpression != null )
		{
			iHavingExpression.setBackend( backend );
			statement.append( " HAVING " );
			statement.append( iHavingExpression );
		}
		if( iOrderFields != null && iOrderFields.length > 0 )
		{
			statement.append( " ORDER BY " );
			for( int n = 0; n < iOrderFields.length - 1; n++ )
			{
				statement.append( iOrderFields[ n ] );
				statement.append( ", " );
			}
			statement.append( iOrderFields[ iOrderFields.length - 1 ] );
			statement.append( " " );
		}
		if( iIntoClause != null && databaseType != null && databaseType.equals( DB_INFORMIX ) )
		{
			statement.append( " INTO " );
			if( iIntoClause.isTemp() )
			{
				statement.append( "TEMP " );
			}
			statement.append( iIntoClause );
		}
		return toUnicode( statement ).toString();
	}
	
	public void setUnicode( boolean translate )
	{
		unicode = translate;
	}
	
	protected CharSequence toUnicode( CharSequence str )
	{
		if( unicode )
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
		res = StatementExecuterFactory.executeSelectStatementWithCursor( this.toString(), this );
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
	
	public void setDatabaseType( String databaseType )
	{
		this.databaseType = databaseType;
	}
	
	public String getDatabaseType()
	{
		return databaseType;
	}
	
	public void disableOrderFieldsVerification()
	{
		verifyOrderFields = false;
	}

	protected boolean getVerifyOrderFields()
	{
		return verifyOrderFields;
	}
	
	public void setBackend( Backend backend )
	{
/*		if( !strange )
		{
			iStatement = null;
		}*/
		this.backend = backend;
	}
	
	protected Backend getBackend()
	{
		if( backend == null )
		{
			setBackend( BackendProvider.getDefaultBackend() );
		}
		return backend;
	}
}