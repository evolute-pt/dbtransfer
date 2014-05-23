/*
 * ColumnMetadataConstants.java
 *
 * Created on 4 de Dezembro de 2006, 19:36
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pt.evolute.utils.db;

/**
 *
 * @author fpalma
 */
public interface ColumnMetadataConstants
{
	public static final String TABLE_CAT = "TABLE_CAT";
	public static final String TABLE_SCHEM = "TABLE_SCHEM";
	public static final String TABLE_NAME = "TABLE_NAME";
	public static final String COLUMN_NAME = "COLUMN_NAME";
	public static final String DATA_TYPE = "DATA_TYPE";
	public static final String TYPE_NAME = "TYPE_NAME";
	public static final String COLUMN_SIZE = "COLUMN_SIZE";
	public static final String BUFFER_LENGTH = "BUFFER_LENGTH";
	public static final String DECIMAL_DIGITS = "DECIMAL_DIGITS";
	public static final String NUM_PREC_RADIX = "NUM_PREC_RADIX";
	public static final String NULLABLE = "NULLABLE";
	public static final String REMARKS = "REMARKS";
	public static final String COLUMN_DEF = "COLUMN_DEF";
	public static final String SQL_DATA_TYPE = "SQL_DATA_TYPE";
	public static final String SQL_DATETIME_SUB = "SQL_DATETIME_SUB";
	public static final String CHAR_OCTET_LENGTH = "CHAR_OCTET_LENGTH";
	public static final String ORDINAL_POSITION = "ORDINAL_POSITION";
	public static final String IS_NULLABLE = "IS_NULLABLE";
	public static final String SCOPE_CATLOG = "SCOPE_CATLOG";
	public static final String SCOPE_SCHEMA = "SCOPE_SCHEMA";
	public static final String SCOPE_TABLE = "SCOPE_TABLE";
	public static final String SOURCE_DATA_TYPE = "SOURCE_DATA_TYPE";
	
	public static final int INDEX_TABLE_CAT = 1;
	public static final int INDEX_TABLE_SCHEM = 2;
	public static final int INDEX_TABLE_NAME = 3;
	public static final int INDEX_COLUMN_NAME = 4;
	public static final int INDEX_DATA_TYPE = 5;
	public static final int INDEX_TYPE_NAME = 6;
	public static final int INDEX_COLUMN_SIZE = 7;
	public static final int INDEX_BUFFER_LENGTH = 8;
	public static final int INDEX_DECIMAL_DIGITS = 9;
	public static final int INDEX_NUM_PREC_RADIX = 10;
	public static final int INDEX_NULLABLE = 11;
	public static final int INDEX_REMARKS = 12;
	public static final int INDEX_COLUMN_DEF = 13;
	public static final int INDEX_SQL_DATA_TYPE = 14;
	public static final int INDEX_SQL_DATETIME_SUB = 15;
	public static final int INDEX_CHAR_OCTET_LENGTH = 16;
	public static final int INDEX_ORDINAL_POSITION = 17;
	public static final int INDEX_IS_NULLABLE = 18;
	public static final int INDEX_SCOPE_CATLOG = 19;
	public static final int INDEX_SCOPE_SCHEMA = 20;
	public static final int INDEX_SCOPE_TABLE = 21;
	public static final int INDEX_SOURCE_DATA_TYPE = 22;
	
	public static final String DESCRIPTIONS[] = 
			new String[]{ "",
							TABLE_CAT,
							TABLE_SCHEM,
							TABLE_NAME,
							COLUMN_NAME,
							DATA_TYPE,
							TYPE_NAME,
							COLUMN_SIZE,
							BUFFER_LENGTH,
							DECIMAL_DIGITS,
							NUM_PREC_RADIX,
							NULLABLE,
							REMARKS,
							COLUMN_DEF,
							SQL_DATA_TYPE,
							SQL_DATETIME_SUB,
							CHAR_OCTET_LENGTH,
							ORDINAL_POSITION,
							IS_NULLABLE,
							SCOPE_CATLOG,
							SCOPE_SCHEMA,
							SCOPE_TABLE,
							SOURCE_DATA_TYPE
	};
	
	public static final String TYPES[] = 
			new String[]{ "",
							"String",
							"String",
							"String",
							"String",
							"int",
							"String",
							"int",
							null, 
							"int",
							"int",
							"int",
							"String",
							"String",
							"int",
							"int",
							"int",
							"int",
							"String",
							"String",
							"String",
							"String",
							"short"
	};
	
	public static final boolean IS_USED[] = 
			new boolean[]{ false,
							true,
							true,
							true,
							true,
							true,
							true,
							true,
							false,
							true,
							true,
							true,
							true,
							true,
							false,
							false,
							true,
							true,
							true,
							true,
							true,
							true,
							true
	};
	
	public static final boolean IS_CLASS[] = 
			new boolean[]{ false,
							true,
							true,
							true,
							true,
							false,
							true,
							false,
							false,
							false,
							false,
							false,
							true,
							true,
							false,
							false,
							false,
							false,
							true,
							true,
							true,
							true,
							false
	};
	
	@SuppressWarnings("rawtypes")
	public static final Class CLASS_FOR_TYPE[] = 
			new Class[]{ null,
							String.class,
							String.class,
							String.class,
							String.class,
							Integer.class,
							String.class,
							Integer.class,
							null, 
							Integer.class,
							Integer.class,
							Integer.class,
							String.class,
							String.class,
							Integer.class,
							Integer.class,
							Integer.class,
							Integer.class,
							String.class,
							String.class,
							String.class,
							String.class,
							Short.class
	};
	
	public static final String COMMENTS[] = 
			new String[]{ "", 
							"table catalog (may be null)",
							"table schema (may be null)",
							"table name",
							"column name",
							"SQL type from java.sql.Types",
							"Data source dependent type name, for a UDT the type name is fully qualified",
							"column size. For char or date types this is the maximum number of characters, for numeric or decimal types this is precision.",
							"is not used.",
							"the number of fractional digits",
							"Radix (typically either 10 or 2)",
							"is NULL allowed\n\t* columnNoNulls - might not allow NULL values\n\t* columnNullable - definitely allows NULL values"
							+"\n\t* columnNullableUnknown - nullability unknown ",
							"comment describing column (may be null)",
							"default value (may be null)",
							"unused",
							"unused",
							"for char types the maximum number of bytes in the column",
							"index of column in table (starting at 1)",
							"\"NO\" means column definitely does not allow NULL values; \"YES\" means the column might allow NULL values. An empty string means nobody knows.",
							"catalog of table that is the scope of a reference attribute (null if DATA_TYPE isn't REF)",
							"schema of table that is the scope of a reference attribute (null if the DATA_TYPE isn't REF)",
							"table name that this the scope of a reference attribure (null if the DATA_TYPE isn't REF)",
							"source type of a distinct type or user-generated Ref type, SQL type from java.sql.Types (null if DATA_TYPE isn't DISTINCT or user-generated REF)"
	};
}
