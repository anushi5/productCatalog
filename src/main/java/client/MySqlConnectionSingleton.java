package client;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySqlConnectionSingleton {

    private static MySqlConnectionSingleton instance;
    private static Connection connection;

    private MySqlConnectionSingleton() {
        // Create a new MySQL connection
        String dbUrl = "jdbc:mysql://localhost:3306/productData";
        String dbUser = "root";
        String dbPassword = "anushi1997";
        try {
            connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
        } catch (SQLException e) {
            // Handle connection error
            System.out.println("Exception in creating mysql connection");
        }
    }

    public static MySqlConnectionSingleton getInstance() {
        if (instance == null) {
            instance = new MySqlConnectionSingleton();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

}


