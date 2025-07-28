package databaseconnector;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseHelper {

    // Thực hiện câu truy vấn SELECT và trả về ResultSet (người dùng nhớ đóng ResultSet và Statement sau khi dùng)
    public static ResultSet executeQuery(String sql) throws SQLException {
        Connection conn = DatabaseConnector.getConnection();
        Statement stmt = conn.createStatement();
        return stmt.executeQuery(sql);
    }

    // Thực hiện truy vấn trả về 1 giá trị số (SUM,...), tránh lặp code
    public static double executeScalarDouble(String sql) {
        double result = 0;
        try (Connection conn = DatabaseConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                result = rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }
}
