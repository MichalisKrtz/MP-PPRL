package db;

import java.sql.*;

public class DbConnection {

    public static Connection connect(String dbPath) {
        String url = "jdbc:sqlite:/" + dbPath;

        Connection con = null;
            try {
                con = DriverManager.getConnection(url);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        return con;
    }
}
