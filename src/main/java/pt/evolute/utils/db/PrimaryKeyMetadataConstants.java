/*
 * PrimaryKeyMetadataConstants.java
 *
 * Created on 5 de Dezembro de 2006, 18:28
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pt.evolute.utils.db;

/**
 *
 * @author fpalma
 */
public interface PrimaryKeyMetadataConstants
{
	public static final String TABLE_CAT = "TABLE_CAT";
	public static final String TABLE_SCHEM = "TABLE_SCHEM";
	public static final String TABLE_NAME = "TABLE_NAME";
	public static final String COLUMN_NAME = "COLUMN_NAME";
	public static final String KEY_SEQ = "KEY_SEQ";
	public static final String PK_NAME = "PK_NAME";
	
	public static final int INDEX_TABLE_CAT = 1;
	public static final int INDEX_TABLE_SCHEM = 2;
	public static final int INDEX_TABLE_NAME = 3;
	public static final int INDEX_COLUMN_NAME = 4;
	public static final int INDEX_KEY_SEQ = 5;
	public static final int INDEX_PK_NAME = 6;
	
	public static final String DESCRIPTIONS[] = 
			new String[]{ "",
							TABLE_CAT,
							TABLE_SCHEM,
							TABLE_NAME,
							COLUMN_NAME,
							KEY_SEQ,
							PK_NAME
	};
	
	public static final String TYPES[] = 
			new String[]{ "",
							"String",
							"String",
							"String",
							"String",
							"short",
							"String"
	};
	
	public static final boolean IS_USED[] = 
			new boolean[]{ false,
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
							true
	};
	
	public static final Class<?> CLASS_FOR_TYPE[] = 
			new Class[]{ null,
							String.class,
							String.class,
							String.class,
							String.class,
							Short.class,
							String.class
	};
	
	public static final String COMMENTS[] = 
			new String[]{ "", 
							"table catalog (may be null)", 
							"table schema (may be null)", 
							"table name", 
							"column name", 
							"sequence number within primary key", 
							"primary key name (may be null)"
	};
}
