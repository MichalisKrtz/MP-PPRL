package db;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PartyDAO {
    private final String dbPath;
    private Connection conn = null;

    public PartyDAO(String dbPath) {
        this.dbPath = dbPath;
    }

    private void connect() {
        try {
            String url = "jdbc:sqlite:/";
            conn = DriverManager.getConnection(url + dbPath);
        } catch (SQLException e) {
            System.out.println("FAILED TO CONNECT TO THE DATABASE");
        }
    }

    public List<Map<String, DynamicTypeValue>> selectAll() {
        PreparedStatement ps = null;
        ResultSet rs = null;
        String query = "SELECT * FROM users";
        try {
            connect();
            ps = conn.prepareStatement(query);
            rs = ps.executeQuery();

            ResultSetMetaData rsmd = rs.getMetaData();
            List<Map<String , DynamicTypeValue>> records = new ArrayList<>();
            while (rs.next()) {
                Map<String, DynamicTypeValue> record = new HashMap<>();
                for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                    String key = rsmd.getColumnName(i);
                    String columnType = rsmd.getColumnTypeName(i);
                    DynamicTypeValue value;
                    switch (columnType) {
                        case "TEXT":
                            value = new StringTypeValue(rs.getString(i));
                            break;
                        case "INTEGER":
                            value = new IntegerTypeValue(rs.getInt(i));
                            break;
                        default:
                            value = null;
                            System.out.println("THE DATA TYPE OF THIS COLUMN IS NOT SUPPORTED");
                    }
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
                try { rs.close(); } catch (SQLException e) { System.out.println("FAILED TO CLOSE RESULT SET"); }
            }
            if (ps != null) {
                try { ps.close(); } catch (SQLException e) { System.out.println("FAILED TO CLOSE PREPARED STATEMENT"); }
            }
            if (conn != null) {
                try { conn.close(); } catch (SQLException e) { System.out.println("FAILED TO CLOSE DATABASE CONNECTION"); }
            }
        }

    }
}
