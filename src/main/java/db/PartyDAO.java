package db;

import java.sql.*;

public class PartyDAO {
    private final Connection con;

    public PartyDAO(String dbPath) {
        con = DbConnection.connect(dbPath);
    }

    public void selectFromValues() {
        String sql = "SELECT * FROM _values_ LIMIT 10";
        try {
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                String name = rs.getString("_object_id_");
                System.out.println(name);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
