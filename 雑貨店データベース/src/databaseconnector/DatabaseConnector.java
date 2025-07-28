package databaseconnector;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnector {
    private static final String URL = "jdbc:postgresql://localhost:5432/thcsdl"; 
    private static final String USER = "postgres";
    private static final String PASSWORD = "12345678";

    // Hàm static để dùng ở mọi nơi
    public static Connection getConnection() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Kết nối thành công đến PostgreSQL");
        } catch (SQLException e) {
            System.err.println("Lỗi khi kết nối: " + e.getMessage());
        }
        return conn;
    }

    // (Không cần phương thức connect() hoặc main test nếu bạn đã test xong)
}
