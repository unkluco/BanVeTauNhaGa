package com.connectDB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * Singleton kết nối SQL Server.
 * Đọc cấu hình từ file .env đặt tại thư mục gốc dự án.
 *
 * File .env cần có các biến:
 *   DB_HOST     - hostname hoặc IP của SQL Server (mặc định: localhost)
 *   DB_PORT     - cổng kết nối (mặc định: 1433)
 *   DB_NAME     - tên database (mặc định: BanVeTauNhaGa)
 *   DB_USER     - tên đăng nhập SQL Server
 *   DB_PASSWORD - mật khẩu
 */
public class ConnectDB {

    private static ConnectDB instance = new ConnectDB();
    private static Connection con = null;

    private ConnectDB() {}

    public static ConnectDB getInstance() {
        return instance;
    }

    public static Connection getCon() {
        return con;
    }

    public void connect() throws SQLException {

        String url = "jdbc:sqlserver://localhost:1433;"
                +"databaseName=BanVeTauNhaGa;"
                +"encrypt=true;"
                +"trustServerCertificate=true;";
        String user = "sa";
        String password = "sapassword";
        con = DriverManager.getConnection(url, user, password);
        System.out.println("Connected to the database.");
    }

    public void disconnect() {
        if (con != null) {
            try {
                con.close();
                con = null;
                System.out.println("Đã đóng kết nối database.");
            } catch (SQLException e) {
                System.err.println("Lỗi khi đóng kết nối: " + e.getMessage());
            }
        }
    }
}
