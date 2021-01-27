package pt.evolute.dbtransfer.db.jackcess;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.Index;

import pt.evolute.dbtransfer.db.DBConnection;
import pt.evolute.dbtransfer.db.beans.ColumnDefinition;
import pt.evolute.dbtransfer.db.beans.ForeignKeyDefinition;
import pt.evolute.dbtransfer.db.beans.TableDefinition;
import pt.evolute.dbtransfer.db.beans.PrimaryKeyDefinition;
import pt.evolute.dbtransfer.db.beans.UniqueDefinition;
import pt.evolute.dbtransfer.db.helper.Helper;
import pt.evolute.dbtransfer.db.jackcess.beans.AccessTableDefinition;
import pt.evolute.utils.arrays.Virtual2DArray;
import pt.evolute.utils.dbmodel.DBTable;

/**
 *
 * @author lflores
 */
public class JackcessConnection implements DBConnection {

    private final Database db;

    private final List<TableDefinition> TABLES_LIST = new LinkedList<TableDefinition>();
    private final Map<TableDefinition, AccessTableDefinition> TABLES_MAP = new HashMap<TableDefinition, AccessTableDefinition>();
    private final boolean ignoreEmpty;

    public JackcessConnection(String url, String user, String pass, boolean onlyNotEmpty)
            throws Exception {
        db = DatabaseBuilder.open(new File(url.substring( 9 )));
        ignoreEmpty = onlyNotEmpty;
    }

    public List<TableDefinition> getTableList()
            throws Exception {
        if (TABLES_LIST.isEmpty()) {
            Set<String> set = db.getTableNames();
            System.out.println("Database has " + set.size() + " tables");
            for (String str : set) {
            	TableDefinition n = new TableDefinition(str);
                if (ignoreEmpty && getRowCount(n) == 0) {
                    continue;
                }
                TABLES_LIST.add(n);
            }
        }
        return TABLES_LIST;
    }

    public AccessTableDefinition getTableDefinition(TableDefinition table)
            throws IOException {
        AccessTableDefinition t = TABLES_MAP.get(table);
        if (t == null) {
        	com.healthmarketscience.jackcess.Table tb = db.getTable(table.originalName);
            if (tb != null) {
                t = new AccessTableDefinition( /*this,*/tb);
                TABLES_MAP.put(table, t);
            }
        }
        return t;
    }

    public List<ColumnDefinition> getColumnList(TableDefinition table) throws Exception {
        return getTableDefinition(table).getColumns();
    }

    public Virtual2DArray executeQuery(String sql) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public PrimaryKeyDefinition getPrimaryKey(TableDefinition table) throws Exception {
        List<ColumnDefinition> cols = getTableDefinition(table).getPrimaryKeys();
        PrimaryKeyDefinition pk = new PrimaryKeyDefinition();
        pk.name = table + "_" + "pk";
        pk.columns.addAll(cols);
        return pk;
    }

    public List<ForeignKeyDefinition> getForeignKeyList(TableDefinition table) throws Exception {
        return getTableDefinition(table).getForeignKeys();
    }

    public Virtual2DArray getFullTable(TableDefinition table) throws Exception {
        return new Jackcess2DArray(db.getTable(table.originalName));
    }

    public PreparedStatement prepareStatement(String sql) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<DBTable> getSortedTables() throws Exception {
        throw new RuntimeException("Not implemented yet!!!!");
    }

    @Override
    public List<UniqueDefinition> getUniqueList(TableDefinition table)
            throws Exception {
//		table = table.toLowerCase();
        List<UniqueDefinition> list = new LinkedList<UniqueDefinition>();
        for (Index idx : db.getTable(table.originalName).getIndexes()) {
            if (idx.isUnique()) {
                UniqueDefinition uniq = new UniqueDefinition(idx.getName(), table);
                for (Index.Column col : idx.getColumns()) {
                    uniq.columns.add(col.getName().toLowerCase());
                }
            }
        }
        return list;
    }

    @Override
    public int getRowCount(TableDefinition table) throws Exception {
        return db.getTable(table.originalName).getRowCount();
    }

    public Helper getHelper() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
