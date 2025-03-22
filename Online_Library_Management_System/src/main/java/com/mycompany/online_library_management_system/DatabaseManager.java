
package com.mycompany.online_library_management_system;


import java.sql.*;
public class DatabaseManager {
    private static final String URL = "jdbc:mysql://localhost:3306/library_system"; // Update with your DB name
    private static final String USER = "root"; // Your database username
    private static final String PASSWORD = "4312"; // Your database password

    private static Connection connection = null;

    // Private constructor to prevent instantiation
    private DatabaseManager() {}

    // Method to get the connection
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        }
        return connection;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.out.println("Error closing connection: " + e.getMessage());
            }
        }
    }
}
