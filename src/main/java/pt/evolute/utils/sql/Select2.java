package pt.evolute.utils.sql;

import pt.evolute.utils.Singleton;
import pt.evolute.utils.sql.backend.Backend;
import pt.evolute.utils.sql.table.DefaultTable;
import pt.evolute.utils.sql.table.Table;

/**
 *
 * @author fpalma
 */
public class Select2 extends Select
{
	public static final Integer JOIN_CROSS = new Integer( 0 );
	public static final Integer JOIN_LEFT_OUTER = new Integer( 1 );
//	public static final Integer JOIN_RIGHT_OUTER = new Integer( 2 );
	public static final Integer JOIN_INNER = new Integer( 3 );
	
	protected Table tables[];
	protected Integer joinTypes[];
	protected Expression joinExpressions[];
	protected Field fields[];
	protected Expression whereExpression;
	protected Field orderFields[];
	protected Field groupFields[];
	protected Expression havingExpression;
	protected Table intoClause;
	protected Integer limit;
	protected Integer offset;
	
//	private String statement = null;
	
	public Select2( String tableName, Expression whereExpression, String ... fields)
	{
		this( new String[]{tableName},null,null,fields,whereExpression,null,null,null,null);
	}
	
	public Select2( String tableName, Expression whereExpression, Field ... fields)
	{
		this( new Table[]{new DefaultTable(tableName)},null,null,fields,whereExpression,null,null,null,null);
	}
	
	public Select2( String tableName, Expression whereExpression, String [] fields, String [] order)
	{
		this( new String[]{tableName},null,null,fields,whereExpression,order,null,null,null);
	}
	
	/** Creates a new instance of Select2 */
	public Select2( Table tables[],
					Integer joinTypes[],
					Expression joinExpressions[],
					Field fields[],
					Expression whereExpression,
					Field orderFields[],
					Field groupFields[],
					Expression havingExpression,
					Table intoClause )
	{
		super( "" );
		this.tables = tables;
		this.joinTypes = joinTypes;
		if( this.joinTypes == null )
		{
			this.joinTypes = new Integer[ tables.length - 1 ];
			for( int jt = 0; jt < this.joinTypes.length; jt++ )
			{
				this.joinTypes[ jt ] = JOIN_CROSS;
			}
		}
		this.joinExpressions = joinExpressions != null ? joinExpressions : new Expression[ tables.length - 1 ];
		this.fields = fields;
		this.whereExpression = whereExpression;
		this.orderFields = orderFields;
		this.groupFields = groupFields;
		this.havingExpression = havingExpression;
		this.intoClause = intoClause;
		setDatabaseType( ( String ) Singleton.getInstance( Singleton.DEFAULT_DATABASE_TYPE ) );
	}
	
	public Select2( String tables[],
					Integer joinTypes[],
					Expression joinExpressions[],
					String fields[],
					Expression whereExpression,
					String orderFields[],
					String groupFields[],
					Expression havingExpression,
					String intoClause )
	{
		super( "" );
		this.tables = new Table[ tables.length ];
		for( int t = 0; t < tables.length; t++ )
		{
			this.tables[ t ] = new DefaultTable( tables[ t ] );
		}
		this.joinTypes = joinTypes;
		if( this.joinTypes == null )
		{
			this.joinTypes = new Integer[ tables.length - 1 ];
			for( int jt = 0; jt < this.joinTypes.length; jt++ )
			{
				this.joinTypes[ jt ] = JOIN_CROSS;
			}
		}
		this.joinExpressions = joinExpressions != null ? joinExpressions : new Expression[ tables.length - 1 ];
		this.fields = new Field[ fields.length ];
		for( int f = 0; f < fields.length; f++ )
		{
			this.fields[ f ] = new Field( fields[ f ] );
		}
		this.whereExpression = whereExpression;
		if( orderFields != null )
		{
			this.orderFields = new Field[ orderFields.length ];
			for( int of = 0; of < orderFields.length; of++ )
			{
				this.orderFields[ of ] = new Field( orderFields[ of ] );
			}
		}
		
		if( groupFields != null )
		{
			this.groupFields = new Field[ groupFields.length ];
			for( int gf = 0; gf < groupFields.length; gf++ )
			{
				this.groupFields[ gf ] = new Field( groupFields[ gf ] );
			}
		}
		this.havingExpression = havingExpression;
		if( intoClause != null )
		{
			this.intoClause = new DefaultTable( intoClause );
		}
		setDatabaseType( ( String ) Singleton.getInstance( Singleton.DEFAULT_DATABASE_TYPE ) );
	}
		
	@Override
	public String toString()
	{
/*		if( statement != null )
		{
			return toUnicode( statement );
		}*/
		if( tables == null || tables.length == 0 || 
			fields == null || fields.length == 0 )
		{
			return "";
		}
		StringBuilder statement = new StringBuilder( "SELECT " );
		if( limit != null )
		{
			statement.append( getBackend().getLimitFieldsPrefix( limit ) );
			statement.append( " " );
		}
		for( int n = 0; n < fields.length - 1; n++ )
		{
			fields[ n ].setBackend( getBackend() );
			statement.append( fields[ n ] );
			statement.append( ", " );
		}
		fields[ fields.length - 1 ].setBackend( getBackend() );
		statement.append( fields[ fields.length - 1 ] );
		statement.append( " " );
		
		// check fields in order by
		if( orderFields != null && orderFields.length > 0 && getVerifyOrderFields() )
		{
			for( int i = 0; i < orderFields.length; ++i )
			{
				if( orderFields[ i ] instanceof FieldIndex )
				{
					continue;
				}
				orderFields[ i ].setBackend( getBackend() );
				// TODO improve order field parser
				String orderField = orderFields[ i ].toString().trim();
				String orderFieldUpper = orderField.toUpperCase();
				if( orderFieldUpper.endsWith( " DESC" ) || orderFieldUpper.endsWith( " ASC" ) )
				{
					orderField = orderField.substring( 0, orderField.lastIndexOf( ' ' ) ).trim();
				}
				boolean found = false;
				for( int j = 0; j < fields.length; ++j )
				{
					if( orderField.equals( fields[ j ].toString() )
							|| fields[ j ].toString().endsWith( "." + orderField ) )
					{
						found = true;
						break;
					}
				}
				if( !found )
				{
					statement.append( ", " );
					statement.append( orderField );
				}
			}
		}
		if( intoClause != null && getDatabaseType() != null && getDatabaseType().equals( DB_POSTGRESQL ) )
		{
			statement.append( " INTO " );
			if( intoClause.isTemp() )
			{
				statement.append( "TEMP " );
			}
			statement.append( intoClause );
		}
		statement.append( " FROM " );
		for( int n = 0; n < tables.length; n++ )
		{
			if( n > 0 )
			{
				if( joinTypes[ n - 1 ] == null || JOIN_CROSS.equals( joinTypes[ n - 1 ] ) )
				{
					statement.append( ", " );
				}
				else if( JOIN_LEFT_OUTER.equals( joinTypes[ n - 1 ] ) )
				{
					statement.append( " LEFT OUTER JOIN " );
				}
				else if( JOIN_INNER.equals( joinTypes[ n - 1 ] ) )
				{
					statement.append( " INNER JOIN " );
				}
				else
				{
					throw new IllegalArgumentException( "Invalid join type: " + joinTypes[ n - 1 ] );
				}
			}
			statement.append( tables[ n ] );
			
			if( n > 0 && joinExpressions[ n - 1 ] != null )
			{
				joinExpressions[ n - 1 ].setBackend( getBackend() );
				statement.append( " ON " );
				statement.append( joinExpressions[ n - 1 ] );
			}
		}
		if( whereExpression != null )
		{
			whereExpression.setBackend( getBackend() );
			String wClause = whereExpression.toString();
			if( !wClause.isEmpty() )
			{
				statement.append( " WHERE " );
				statement.append( wClause );
				statement.append( " " );
			}
		}
		if( groupFields != null && groupFields.length > 0 )
		{
			statement.append( " GROUP BY " );
			for( int n = 0; n < groupFields.length - 1; n++ )
			{
				groupFields[ n ].setBackend( getBackend() );
				statement.append( groupFields[ n ] );
				statement.append( ", " );
			}
			groupFields[ groupFields.length - 1 ].setBackend( getBackend() );
			statement.append( groupFields[ groupFields.length - 1 ] );
			statement.append( " " );
		}
		if( havingExpression != null )
		{
			havingExpression.setBackend( getBackend() );
			statement.append( " HAVING " );
			statement.append( havingExpression );
		}
//		if( orderFields != null && orderFields.length > 0 )
//		{
//			statement += " ORDER BY ";
//			for( int n = 0; n < orderFields.length - 1; n++ )
//			{
//				orderFields[ n ].setBackend( getBackend() );
//				statement += orderFields[ n ] + ", ";
//			}
//			orderFields[ orderFields.length - 1 ].setBackend( getBackend() );
//			statement += orderFields[ orderFields.length - 1 ] + " ";
//		}
		statement.append( getOrderExpression() );
		if( intoClause != null && getDatabaseType() != null && getDatabaseType().equals( DB_INFORMIX ) )
		{
			statement.append( " INTO " );
			if( intoClause.isTemp() )
			{
				statement.append( "TEMP " );
			}
			statement.append( intoClause );
		}
		if( limit != null )
		{
			statement.append( getBackend().getLimitQuerySuffix( limit ) );
		}
		if( offset != null )
		{
			statement.append( getBackend().getOffsetQuerySuffix( offset ) );
		}
		return toUnicode( statement ).toString();
	}
	
	public CharSequence getOrderExpression()
	{
		StringBuilder result = new StringBuilder();
		if( orderFields != null && orderFields.length > 0 )
		{
			result.append( " ORDER BY " );
			for( int n = 0; n < orderFields.length - 1; n++ )
			{
				orderFields[ n ].setBackend( getBackend() );
				result.append( orderFields[ n ] );
				result.append( ", " );
			}
			orderFields[ orderFields.length - 1 ].setBackend( getBackend() );
			result.append( orderFields[ orderFields.length - 1 ] );
			result.append( " " );
		}
		return result;
	}
	
	public void setLimit( Integer nRows )
	{
		this.limit = nRows;
	}
	
	public void setOffset( Integer offset )
	{
		this.offset = offset;
	}
	
	@Override
	public void setBackend( Backend backend )
	{
//		statement = null;
		super.setBackend( backend );
	}

	public void insertFieldBefore( String string )
	{
		Field [] newFields = new Field[fields.length+1];
		newFields[0] = new Field( string );
		if( fields.length > 0 )
		{
			System.arraycopy( fields, 0, newFields, 1, fields.length );
		}
		this.fields = newFields;
	}
}
