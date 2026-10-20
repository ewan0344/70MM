
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private static final String SERVER_URL =
            "jdbc:mysql://localhost:3306/";

    private static final String DATABASE_URL =
            "jdbc:mysql://localhost:3306/mm70";

    private static final String DATABASE_NAME = "mm70";
    
    private static final String USER = "root";

    private static final String PASSWORD =
            System.getenv("MM70_DB_PASSWORD");

    public static Connection getConnection() throws SQLException {

        if (PASSWORD == null || PASSWORD.isBlank()) {
            throw new SQLException(
                "MySQL password not configured. Set the " +
                "MM70_DB_PASSWORD environment variable and restart Eclipse."
            );
        }

        // Connect to the MySQL server without selecting a database.
        try (Connection serverConnection =
                    DriverManager.getConnection(
                        SERVER_URL, USER, PASSWORD);
             Statement statement =
                    serverConnection.createStatement()) {

            // Create the project database if it doesn't exist.
            statement.executeUpdate(
                "CREATE DATABASE IF NOT EXISTS `" +
                DATABASE_NAME + "`"
            );
        }

        // Connect to the project database.
        return DriverManager.getConnection(
            DATABASE_URL, USER, PASSWORD
        );
    }
}