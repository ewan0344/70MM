
import java.sql.*;
import java.security.SecureRandom;
import java.security.MessageDigest;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.util.Base64;

public class UserDatabase {

    static final String SERVER_URL =
        "jdbc:mysql://localhost:3306/";

    static final String DATABASE_URL =
        "jdbc:mysql://localhost:3306/mm70_users";

    static final String USER = "root";

    static final String PASSWORD =
        System.getenv("MM70_DB_PASSWORD");


    // Create database and users table
    public static void setup() throws Exception {

        if (PASSWORD == null || PASSWORD.isBlank()) {
            throw new SQLException(
                "MM70_DB_PASSWORD environment variable is not set."
            );
        }

        // Create the database if it doesn't exist
        try (Connection con = DriverManager.getConnection(
                SERVER_URL, USER, PASSWORD);
             Statement st = con.createStatement()) {

            st.executeUpdate(
                "CREATE DATABASE IF NOT EXISTS mm70_users"
            );
        }

        // Create the users table if it doesn't exist
        String sql =
            "CREATE TABLE IF NOT EXISTS users (" +
            "user_id INT AUTO_INCREMENT PRIMARY KEY, " +
            "username VARCHAR(50) NOT NULL UNIQUE, " +
            "password_hash VARCHAR(255) NOT NULL, " +
            "salt VARCHAR(100) NOT NULL" +
            ")";

        try (Connection con = DriverManager.getConnection(
                DATABASE_URL, USER, PASSWORD);
             Statement st = con.createStatement()) {

            st.executeUpdate(sql);
        }
    }


    // Create a new user account
    public static boolean createUser(
            String username, char[] password) throws Exception {

        setup();

        byte[] saltBytes = new byte[16];
        new SecureRandom().nextBytes(saltBytes);

        String salt =
            Base64.getEncoder().encodeToString(saltBytes);

        String hash = hashPassword(password, saltBytes);

        String sql =
            "INSERT INTO users (username, password_hash, salt) " +
            "VALUES (?, ?, ?)";

        try (Connection con = DriverManager.getConnection(
                DATABASE_URL, USER, PASSWORD);
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, hash);
            ps.setString(3, salt);

            ps.executeUpdate();

            return true;
        }
    }


    // Verify login credentials
    public static boolean login(
            String username, char[] password) throws Exception {

        setup();

        String sql =
            "SELECT password_hash, salt FROM users WHERE username = ?";

        try (Connection con = DriverManager.getConnection(
                DATABASE_URL, USER, PASSWORD);
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    return false;
                }

                String savedHash =
                    rs.getString("password_hash");

                byte[] saltBytes = Base64.getDecoder().decode(
                    rs.getString("salt")
                );

                String enteredHash =
                    hashPassword(password, saltBytes);

                return MessageDigest.isEqual(
                    savedHash.getBytes(
                        java.nio.charset.StandardCharsets.UTF_8),
                    enteredHash.getBytes(
                        java.nio.charset.StandardCharsets.UTF_8)
                );
            }
        }
    }


    // Hash the password before saving or checking it
    private static String hashPassword(
            char[] password, byte[] salt) throws Exception {

        PBEKeySpec spec =
            new PBEKeySpec(password, salt, 210000, 256);

        try {
            SecretKeyFactory factory =
                SecretKeyFactory.getInstance(
                    "PBKDF2WithHmacSHA256"
                );

            byte[] hash =
                factory.generateSecret(spec).getEncoded();

            return Base64.getEncoder().encodeToString(hash);

        } finally {
            spec.clearPassword();
        }
    }
}
