
import java.sql.*;
import java.security.SecureRandom;
import java.security.MessageDigest;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.util.Base64;
import java.util.ArrayList;
import java.util.List;

public class UserDatabase {

    static final String SERVER_URL = "jdbc:mysql://localhost:3306/";
    static final String DATABASE_URL = "jdbc:mysql://localhost:3306/mm70_users";
    static final String USER = "root";
    static final String PASSWORD = System.getenv("MM70_DB_PASSWORD");

    public static Connection getConnection() throws SQLException {
        if (PASSWORD == null || PASSWORD.isBlank()) {
            throw new SQLException("MM70_DB_PASSWORD is not set.");
        }

        return DriverManager.getConnection(DATABASE_URL, USER, PASSWORD);
    }

    public static void setup() throws Exception {

        if (PASSWORD == null || PASSWORD.isBlank()) {
            throw new SQLException("MM70_DB_PASSWORD is not set.");
        }

        try (Connection con = DriverManager.getConnection(
                SERVER_URL, USER, PASSWORD);
             Statement st = con.createStatement()) {

            st.executeUpdate("CREATE DATABASE IF NOT EXISTS mm70_users");
        }

        try (Connection con = getConnection();
             Statement st = con.createStatement()) {

            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS users (" +
                "user_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "username VARCHAR(50) NOT NULL UNIQUE, " +
                "password_hash VARCHAR(255) NOT NULL, " +
                "salt VARCHAR(100) NOT NULL)"
            );

            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS friendships (" +
                "user_id INT NOT NULL, " +
                "friend_id INT NOT NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "PRIMARY KEY (user_id, friend_id), " +
                "FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE, " +
                "FOREIGN KEY (friend_id) REFERENCES users(user_id) ON DELETE CASCADE)"
            );

            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS friend_requests (" +
                "request_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "sender_id INT NOT NULL, " +
                "recipient_id INT NOT NULL, " +
                "status VARCHAR(20) NOT NULL DEFAULT 'pending', " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "UNIQUE KEY unique_request (sender_id, recipient_id), " +
                "FOREIGN KEY (sender_id) REFERENCES users(user_id) ON DELETE CASCADE, " +
                "FOREIGN KEY (recipient_id) REFERENCES users(user_id) ON DELETE CASCADE)"
            );
        }
    }

    public static boolean createUser(String username, char[] password)
            throws Exception {

        setup();

        byte[] saltBytes = new byte[16];
        new SecureRandom().nextBytes(saltBytes);

        String salt = Base64.getEncoder().encodeToString(saltBytes);
        String hash = hashPassword(password, saltBytes);

        String sql = "INSERT INTO users (username, password_hash, salt) VALUES (?, ?, ?)";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, hash);
            ps.setString(3, salt);
            ps.executeUpdate();
            return true;
        }
    }

    public static boolean login(String username, char[] password)
            throws Exception {

        setup();

        String sql = "SELECT password_hash, salt FROM users WHERE username = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return false;

                String savedHash = rs.getString("password_hash");
                byte[] saltBytes = Base64.getDecoder().decode(rs.getString("salt"));
                String enteredHash = hashPassword(password, saltBytes);

                return MessageDigest.isEqual(
                    savedHash.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                    enteredHash.getBytes(java.nio.charset.StandardCharsets.UTF_8)
                );
            }
        }
    }

    private static String hashPassword(char[] password, byte[] salt)
            throws Exception {

        PBEKeySpec spec = new PBEKeySpec(password, salt, 210000, 256);

        try {
            SecretKeyFactory factory =
                SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

            return Base64.getEncoder().encodeToString(
                factory.generateSecret(spec).getEncoded()
            );
        } finally {
            spec.clearPassword();
        }
    }

    private static int getUserId(Connection con, String username)
            throws SQLException {

        String sql = "SELECT user_id FROM users WHERE username = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("user_id");
            }
        }

        return -1;
    }

    // Search usernames that are not already friends or in a pending request.
    public static List<String> searchUsers(String username, String text)
            throws Exception {

        setup();
        List<String> users = new ArrayList<>();

        String sql =
            "SELECT u.username FROM users u " +
            "WHERE u.username LIKE ? AND u.username <> ? " +
            "AND NOT EXISTS (" +
            " SELECT 1 FROM friendships f " +
            " WHERE f.user_id = (SELECT user_id FROM users WHERE username = ?) " +
            " AND f.friend_id = u.user_id) " +
            "AND NOT EXISTS (" +
            " SELECT 1 FROM friend_requests r " +
            " WHERE ((r.sender_id = (SELECT user_id FROM users WHERE username = ?) " +
            " AND r.recipient_id = u.user_id) " +
            " OR (r.recipient_id = (SELECT user_id FROM users WHERE username = ?) " +
            " AND r.sender_id = u.user_id)) AND r.status = 'pending') " +
            "ORDER BY u.username LIMIT 20";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, text + "%");
            ps.setString(2, username);
            ps.setString(3, username);
            ps.setString(4, username);
            ps.setString(5, username);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) users.add(rs.getString("username"));
            }
        }

        return users;
    }

    // Send a request instead of immediately becoming friends.
    public static boolean sendFriendRequest(String sender, String recipient)
            throws Exception {

        setup();

        if (sender == null || recipient == null ||
            sender.equalsIgnoreCase(recipient)) return false;

        try (Connection con = getConnection()) {
            int senderId = getUserId(con, sender);
            int recipientId = getUserId(con, recipient);

            if (senderId == -1 || recipientId == -1 ||
                senderId == recipientId) return false;

            String checkFriend =
                "SELECT 1 FROM friendships WHERE user_id = ? AND friend_id = ?";

            try (PreparedStatement ps = con.prepareStatement(checkFriend)) {
                ps.setInt(1, senderId);
                ps.setInt(2, recipientId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return false;
                }
            }

            // If the other person has already requested you, accept that request.
            String incoming =
                "SELECT request_id FROM friend_requests " +
                "WHERE sender_id = ? AND recipient_id = ? AND status = 'pending'";

            try (PreparedStatement ps = con.prepareStatement(incoming)) {
                ps.setInt(1, recipientId);
                ps.setInt(2, senderId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return acceptFriendRequest(sender, rs.getInt("request_id"));
                    }
                }
            }

            String existing =
                "SELECT request_id, status FROM friend_requests " +
                "WHERE sender_id = ? AND recipient_id = ?";

            try (PreparedStatement ps = con.prepareStatement(existing)) {
                ps.setInt(1, senderId);
                ps.setInt(2, recipientId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        if ("pending".equals(rs.getString("status")) ||
                            "accepted".equals(rs.getString("status"))) {
                            return false;
                        }

                        try (PreparedStatement update = con.prepareStatement(
                                "UPDATE friend_requests SET status = 'pending', " +
                                "created_at = CURRENT_TIMESTAMP WHERE request_id = ?")) {
                            update.setInt(1, rs.getInt("request_id"));
                            update.executeUpdate();
                            return true;
                        }
                    }
                }
            }

            String insert =
                "INSERT INTO friend_requests (sender_id, recipient_id, status) " +
                "VALUES (?, ?, 'pending')";

            try (PreparedStatement ps = con.prepareStatement(insert)) {
                ps.setInt(1, senderId);
                ps.setInt(2, recipientId);
                ps.executeUpdate();
            }

            return true;
        }
    }

    // Return usernames who have sent requests to this user.
    public static List<String> getPendingRequests(String username)
            throws Exception {

        setup();
        List<String> requests = new ArrayList<>();

        String sql =
            "SELECT u.username FROM friend_requests r " +
            "JOIN users u ON u.user_id = r.sender_id " +
            "JOIN users me ON me.user_id = r.recipient_id " +
            "WHERE me.username = ? AND r.status = 'pending' " +
            "ORDER BY r.created_at DESC";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) requests.add(rs.getString("username"));
            }
        }

        return requests;
    }

    // Accept a request and create the friendship in both directions.
    public static boolean acceptFriendRequest(String username, int requestId)
            throws Exception {

        setup();

        try (Connection con = getConnection()) {
            con.setAutoCommit(false);

            try {
                int recipientId = getUserId(con, username);
                int senderId = -1;

                String find =
                    "SELECT sender_id FROM friend_requests " +
                    "WHERE request_id = ? AND recipient_id = ? AND status = 'pending'";

                try (PreparedStatement ps = con.prepareStatement(find)) {
                    ps.setInt(1, requestId);
                    ps.setInt(2, recipientId);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            con.rollback();
                            return false;
                        }
                        senderId = rs.getInt("sender_id");
                    }
                }

                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE friend_requests SET status = 'accepted' " +
                        "WHERE request_id = ?")) {
                    ps.setInt(1, requestId);
                    ps.executeUpdate();
                }

                String insert =
                    "INSERT IGNORE INTO friendships (user_id, friend_id) VALUES (?, ?)";

                try (PreparedStatement ps = con.prepareStatement(insert)) {
                    ps.setInt(1, recipientId);
                    ps.setInt(2, senderId);
                    ps.executeUpdate();

                    ps.setInt(1, senderId);
                    ps.setInt(2, recipientId);
                    ps.executeUpdate();
                }

                con.commit();
                return true;

            } catch (Exception ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    // Reject a request.
    public static boolean rejectFriendRequest(String username, int requestId)
            throws Exception {

        setup();

        String sql =
            "UPDATE friend_requests r " +
            "JOIN users u ON u.user_id = r.recipient_id " +
            "SET r.status = 'rejected' " +
            "WHERE r.request_id = ? AND u.username = ? AND r.status = 'pending'";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, requestId);
            ps.setString(2, username);
            return ps.executeUpdate() > 0;
        }
    }

    public static List<String> getFriends(String username)
            throws Exception {

        setup();
        List<String> friends = new ArrayList<>();

        String sql =
            "SELECT u.username FROM friendships f " +
            "JOIN users me ON me.user_id = f.user_id " +
            "JOIN users u ON u.user_id = f.friend_id " +
            "WHERE me.username = ? ORDER BY u.username";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) friends.add(rs.getString("username"));
            }
        }

        return friends;
    }

    // Get pending request IDs and sender names for the notification list.
    public static List<String[]> getPendingRequestDetails(String username)
            throws Exception {

        setup();
        List<String[]> requests = new ArrayList<>();

        String sql =
            "SELECT r.request_id, u.username FROM friend_requests r " +
            "JOIN users u ON u.user_id = r.sender_id " +
            "JOIN users me ON me.user_id = r.recipient_id " +
            "WHERE me.username = ? AND r.status = 'pending' " +
            "ORDER BY r.created_at DESC";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    requests.add(new String[] {
                        rs.getString("request_id"),
                        rs.getString("username")
                    });
                }
            }
        }

        return requests;
    }
}
