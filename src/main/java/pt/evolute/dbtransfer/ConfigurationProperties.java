/*
 * Constants.java
 *
 * Created on February 7, 2005, 12:01 AM
 */

package pt.evolute.dbtransfer;

/**
 *
 * @author  lflores
 */
public interface ConfigurationProperties
{
    public final static String URL_DB_SOURCE = "URL_DB_SOURCE";
    public final static String USER_DB_SOURCE = "USER_DB_SOURCE";
    public final static String PASSWORD_DB_SOURCE = "PASSWORD_DB_SOURCE";
    public final static String SCHEMA_DB_SOURCE = "SCHEMA_DB_SOURCE";

    public final static String[] SOURCE_PROPS = { URL_DB_SOURCE, USER_DB_SOURCE, PASSWORD_DB_SOURCE, SCHEMA_DB_SOURCE };

    public final static String URL_DB_DESTINATION = "URL_DB_DESTINATION";
    public final static String USER_DB_DESTINATION = "USER_DB_DESTINATION";
    public final static String PASSWORD_DB_DESTINATION = "PASSWORD_DB_DESTINATION";
    public final static String SCHEMA_DB_DESTINATION = "SCHEMA_DB_DESTINATION";
    public final static String DESTINATION_TABLE_PREFIX = "DESTINATION_TABLE_PREFIX";
    
    public final static String DESTINATION_PROPS[] = { URL_DB_DESTINATION, USER_DB_DESTINATION, PASSWORD_DB_DESTINATION, SCHEMA_DB_DESTINATION, DESTINATION_TABLE_PREFIX };

    public final static String ANALYSE = "ANALYSE";
    public final static String ANALYSE_DELETE_IF_EXISTS = "ANALYSE_DELETE_IF_EXISTS";

    public final static String ONLY_NOT_EMPTY = "ONLY_NOT_EMPTY";

    public final static String TRANSFER = "TRANSFER";
    public final static String TRANSFER_THREADS = "TRANSFER.THREADS";
    public final static String TRANSFER_ESCAPE_UNICODE = "TRANSFER.ESCAPE_UNICODE";
    public final static String TRANSFER_CHECK_DEPS = "TRANSFER.CHECK_DEPS";
    public final static String TRANSFER_USE_DEST_FOR_DEPS = "TRANSFER.USE_DEST_FOR_DEPS";
    public final static String TRANSFER_MAX_READ_ROWS = "TRANSFER.MAX_READ_ROWS";
    public final static String TRANSFER_IGNORE_BLOBS = "TRANSFER.IGNORE_BLOBS";
    
    public final static String CONSTRAIN = "CONSTRAIN";

    public final static String DIFF = "DIFF";
    public final static String DIFF_COMMENT = "DIFF.COMMENT";
    public final static String DIFF_USE_MD5 = "DIFF.USE_MD5";
    public final static String DIFF_IGNORE_DESTINATION_TABLE_COUNT = "DIFF.IGNORE_DESTINATION_TABLE_COUNT";

    public final static String DEBUG = "DEBUG";
}
