/*
 * ForeignKeyMetadataConstants.java
 *
 * Created on 5 de Dezembro de 2006, 18:11
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pt.evolute.utils.db;

/**
 *
 * @author fpalma
 */
public interface ForeignKeyMetadataConstants
{
	public static final String PKTABLE_CAT = "PKTABLE_CAT";
	public static final String PKTABLE_SCHEM = "PKTABLE_SCHEM";
	public static final String PKTABLE_NAME = "PKTABLE_NAME";
	public static final String PKCOLUMN_NAME = "PKCOLUMN_NAME";
	public static final String FKTABLE_CAT = "FKTABLE_CAT";
	public static final String FKTABLE_SCHEM = "FKTABLE_SCHEM";
	public static final String FKTABLE_NAME = "FKTABLE_NAME";
	public static final String FKCOLUMN_NAME = "FKCOLUMN_NAME";
	public static final String KEY_SEQ = "KEY_SEQ";
	public static final String UPDATE_RULE = "UPDATE_RULE";
	public static final String DELETE_RULE = "DELETE_RULE";
	public static final String FK_NAME = "FK_NAME";
	public static final String PK_NAME = "PK_NAME";
	public static final String DEFERRABILITY = "DEFERRABILITY";
	
	public static final int INDEX_PKTABLE_CAT = 1;
	public static final int INDEX_PKTABLE_SCHEM = 2;
	public static final int INDEX_PKTABLE_NAME = 3;
	public static final int INDEX_PKCOLUMN_NAME = 4;
	public static final int INDEX_FKTABLE_CAT = 5;
	public static final int INDEX_FKTABLE_SCHEM = 6;
	public static final int INDEX_FKTABLE_NAME = 7;
	public static final int INDEX_FKCOLUMN_NAME = 8;
	public static final int INDEX_KEY_SEQ = 9;
	public static final int INDEX_UPDATE_RULE = 10;
	public static final int INDEX_DELETE_RULE = 11;
	public static final int INDEX_FK_NAME = 12;
	public static final int INDEX_PK_NAME = 13;
	public static final int INDEX_DEFERRABILITY = 14;
	
	public static final String DESCRIPTIONS[] = 
			new String[]{ "",
							PKTABLE_CAT,
							PKTABLE_SCHEM,
							PKTABLE_NAME,
							PKCOLUMN_NAME,
							FKTABLE_CAT,
							FKTABLE_SCHEM,
							FKTABLE_NAME,
							FKCOLUMN_NAME,
							KEY_SEQ,
							UPDATE_RULE,
							DELETE_RULE,
							FK_NAME,
							PK_NAME,
							DEFERRABILITY
	};
	
	public static final String TYPES[] = 
			new String[]{ "",
							"String",
							"String",
							"String",
							"String",
							"String",
							"String",
							"String",
							"String",
							"short",
							"short",
							"short",
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
							true,
							true,
							true,
							true,
							false,
							false,
							false,
							true,
							true,
							false
	};
	
	public static final Class<?> CLASS_FOR_TYPE[] = 
			new Class[]{ null,
							String.class,
							String.class,
							String.class,
							String.class,
							String.class,
							String.class,
							String.class,
							String.class,
							Short.class,
							Short.class,
							Short.class,
							String.class,
							String.class,
							Short.class
	};
	
	public static final String COMMENTS[] = 
			new String[]{ "", 
							"primary key table catalog (may be null)", 
							"primary key table schema (may be null)", 
							"primary key table name", 
							"primary key column name", 
							"foreign key table catalog (may be null)", 
							"foreign key table schema (may be null)", 
							"foreign key table name", 
							"foreign key column name", 
							"sequence number within a foreign key", 
							"What happens to a foreign key when the primary key is updated:"
								+ "\n\t* importedNoAction - do not allow update of primary key if it has been imported"
								+ "\n\t* * importedKeyCascade - change imported key to agree with primary key update"
								+ "\n\t* * importedKeySetNull - change imported key to NULL if its primary key has been updated"
								+ "\n\t* * importedKeySetDefault - change imported key to default values if its primary key has been updated"
								+ "\n\t* * importedKeyRestrict - same as importedKeyNoAction (for ODBC 2.x compatibility) ", 
							"What happens to the foreign key when primary is deleted."
								+ "\n\t* importedKeyNoAction - do not allow delete of primary key if it has been imported"
								+ "\n\t* importedKeyCascade - delete rows that import a deleted key"
								+ "\n\t* importedKeySetNull - change imported key to NULL if its primary key has been deleted"
								+ "\n\t* importedKeyRestrict - same as importedKeyNoAction (for ODBC 2.x compatibility)"
								+ "\n\t* importedKeySetDefault - change imported key to default if its primary key has been deleted ", 
							"foreign key name (may be null)", 
							"primary key name (may be null)", 
							"can the evaluation of foreign key constraints be deferred until commit"
								+ "\n\t* importedKeyInitiallyDeferred - see SQL92 for definition"
								+ "\n\t* importedKeyInitiallyImmediate - see SQL92 for definition"
								+ "\n\t* importedKeyNotDeferrable - see SQL92 for definition "
	};
}
