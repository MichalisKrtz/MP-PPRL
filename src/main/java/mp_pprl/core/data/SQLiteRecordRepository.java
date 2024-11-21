package mp_pprl.core.data;

import mp_pprl.core.domain.*;
import mp_pprl.core.domain.Record;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SQLiteRecordRepository implements RecordRepository {
    private final String dbPath;
    private Connection conn = null;

    public SQLiteRecordRepository(String dbPath) {
        this.dbPath = dbPath;
    }

    private void connect() {
        try {
            String url = "jdbc:sqlite:/";
            conn = DriverManager.getConnection(url + dbPath);
        } catch (SQLException e) {
            System.out.println("FAILED TO CONNECT TO THE DATABASE");
            throw new RuntimeException(e);
        }
    }

    public List<Record> getAll() {
        PreparedStatement ps = null;
        ResultSet rs = null;
        String query = "SELECT * FROM users limit " + (10000);
        try {
            connect();
            ps = conn.prepareStatement(query);
            rs = ps.executeQuery();

            ResultSetMetaData rsmd = rs.getMetaData();
            List<Record> records = new ArrayList<>();
            while (rs.next()) {
                Record record = new DynamicRecord();
                for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                    String key = rsmd.getColumnName(i);
                    String columnType = rsmd.getColumnTypeName(i);
                    Object columnValue = rs.getObject(i);
                    DynamicValue value = DynamicValueFactory.createDynamicValue(columnType, columnValue);
                    if (value != null && key != null) {
                        record.put(key, value);
                    }
                }
                records.add(record);
            }
            return records;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    System.out.println("FAILED TO CLOSE RESULT SET");
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    System.out.println("FAILED TO CLOSE PREPARED STATEMENT");
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    System.out.println("FAILED TO CLOSE DATABASE CONNECTION");
                }
            }
        }

    }
}
