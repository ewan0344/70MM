
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.sql.*;

public class FriendProfilePage extends JFrame {

    private final Main main;
    private final String friendUsername;
    private JPanel moviesPanel;

    private final Color BG = Color.decode("#171415");
    private final Color CARD = Color.decode("#241E20");
    private final Color GOLD = Color.decode("#D6A36A");
    private final Color RED = Color.decode("#A64B52");
    private final Color TEXT = Color.decode("#F1E9DD");

    public FriendProfilePage(Main main, String username) {
        this.main = main;
        this.friendUsername = username;

        setTitle("70mm - " + friendUsername + "'s Profile");
        setSize(760, 560);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBackground(BG);
        content.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Profile heading and Favourites heading
        JPanel header = new JPanel(new GridLayout(2, 1, 4, 4));
        header.setBackground(BG);

        JLabel profileTitle = new JLabel(
            friendUsername + "'s Profile",
            SwingConstants.CENTER
        );
        profileTitle.setFont(new Font("Arial", Font.BOLD, 26));
        profileTitle.setForeground(GOLD);

        JLabel favouritesTitle = new JLabel(
            "Favourites",
            SwingConstants.CENTER
        );
        favouritesTitle.setFont(new Font("Arial", Font.BOLD, 20));
        favouritesTitle.setForeground(TEXT);

        header.add(profileTitle);
        header.add(favouritesTitle);

        content.add(header, BorderLayout.NORTH);

        moviesPanel = new JPanel(new GridLayout(0, 3, 10, 10));
        moviesPanel.setBackground(BG);

        JScrollPane scroll = new JScrollPane(moviesPanel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(BG);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        content.add(scroll, BorderLayout.CENTER);

        JButton closeButton = new JButton("Close");
        styleButton(closeButton);
        closeButton.addActionListener(e -> dispose());

        JPanel bottom = new JPanel();
        bottom.setBackground(BG);
        bottom.add(closeButton);

        content.add(bottom, BorderLayout.SOUTH);

        setContentPane(content);

        loadMovies();
        setVisible(true);
    }

    private void styleButton(JButton button) {
        button.setBackground(RED);
        button.setForeground(TEXT);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void loadMovies() {
        moviesPanel.removeAll();

        String sql =
            "SELECT m.tmdb_id, m.title " +
            "FROM mm70_users.user_favourites f " +
            "JOIN mm70_users.users u ON u.user_id = f.user_id " +
            "JOIN movies_new m ON m.tmdb_id = f.tmdb_id " +
            "WHERE u.username = ? " +
            "ORDER BY f.created_at DESC";

        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, friendUsername);

            try (ResultSet rs = ps.executeQuery()) {
                int count = 0;

                while (rs.next()) {
                    int movieId = rs.getInt("tmdb_id");
                    String title = rs.getString("title");

                    JPanel card = new JPanel(new BorderLayout(5, 7));
                    card.setBackground(CARD);
                    card.setBorder(
                        BorderFactory.createEmptyBorder(8, 8, 8, 8)
                    );

                    JLabel poster = new JLabel("", SwingConstants.CENTER);
                    poster.setPreferredSize(new Dimension(150, 180));

                    File file = new File("posters/" + movieId + ".jpg");

                    if (file.exists()) {
                        ImageIcon icon =
                            new ImageIcon(file.getAbsolutePath());

                        Image image = icon.getImage().getScaledInstance(
                            130, 170, Image.SCALE_SMOOTH
                        );

                        poster.setIcon(new ImageIcon(image));
                    } else {
                        poster.setText("No Poster");
                        poster.setForeground(TEXT);
                    }

                    JLabel name = new JLabel(
                        "<html><center>" + escapeHtml(title) +
                        "</center></html>",
                        SwingConstants.CENTER
                    );
                    name.setForeground(GOLD);
                    name.setCursor(new Cursor(Cursor.HAND_CURSOR));

                    // Open movie details when the title is clicked.
                    name.addMouseListener(
                        new java.awt.event.MouseAdapter() {
                            @Override
                            public void mouseClicked(
                                    java.awt.event.MouseEvent e) {
                                main.showMovieDetails(movieId);
                            }
                        }
                    );

                    JButton watchlistButton =
                        new JButton("Add to Watchlist");
                    styleButton(watchlistButton);
                    watchlistButton.addActionListener(
                        e -> addToWatchlist(movieId)
                    );

                    JButton suggestButton =
                        new JButton("Suggest to Friend");
                    styleButton(suggestButton);
                    suggestButton.addActionListener(
                        e -> sendSuggestion(movieId)
                    );

                    JPanel details =
                        new JPanel(new GridLayout(0, 1, 5, 5));
                    details.setBackground(CARD);
                    details.add(name);
                    details.add(watchlistButton);
                    details.add(suggestButton);

                    card.add(poster, BorderLayout.CENTER);
                    card.add(details, BorderLayout.SOUTH);

                    moviesPanel.add(card);
                    count++;
                }

                if (count == 0) {
                    showMessage(
                        friendUsername +
                        " hasn't added any favourites yet."
                    );
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            showMessage(
                "Could not load favourites:\n" + ex.getMessage()
            );
        }

        moviesPanel.revalidate();
        moviesPanel.repaint();
    }

    // Add the friend's favourite to the CURRENT user's watchlist.
    private void addToWatchlist(int movieId) {
        String sql =
            "INSERT INTO watchlist_items_new " +
            "(user_id, tmdb_id, status) " +
            "SELECT u.user_id, ?, 'WANTED' " +
            "FROM mm70_users.users u " +
            "WHERE u.username = ? " +
            "ON DUPLICATE KEY UPDATE status = " +
            "IF(status = 'WATCHED', status, 'WANTED')";

        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, movieId);
            ps.setString(2, main.getUsername());

            int rows = ps.executeUpdate();

            if (rows == 0) {
                JOptionPane.showMessageDialog(
                    this,
                    "Your account was not found. Please log in again."
                );
                return;
            }

            JOptionPane.showMessageDialog(
                this,
                "Movie added to your watchlist!"
            );

        } catch (Exception ex) {
            ex.printStackTrace();

            JOptionPane.showMessageDialog(
                this,
                "Could not add movie to your watchlist:\n" +
                ex.getMessage(),
                "Watchlist Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // Send a movie suggestion to the friend.
    private void sendSuggestion(int movieId) {
        String sql =
            "INSERT INTO mm70_users.movie_suggestions " +
            "(sender_id, recipient_id, tmdb_id) " +
            "SELECT s.user_id, r.user_id, ? " +
            "FROM mm70_users.users s " +
            "JOIN mm70_users.users r " +
            "WHERE s.username = ? " +
            "AND r.username = ? " +
            "AND s.user_id <> r.user_id " +
            "ON DUPLICATE KEY UPDATE " +
            "status = 'pending', created_at = CURRENT_TIMESTAMP";

        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, movieId);
            ps.setString(2, main.getUsername());
            ps.setString(3, friendUsername);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                JOptionPane.showMessageDialog(
                    this,
                    "Movie suggestion sent to " + friendUsername + "!"
                );
            } else {
                JOptionPane.showMessageDialog(
                    this,
                    "Could not find both user accounts."
                );
            }

        } catch (Exception ex) {
            ex.printStackTrace();

            JOptionPane.showMessageDialog(
                this,
                "Could not send suggestion:\n" + ex.getMessage(),
                "Suggestion Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void showMessage(String message) {
        moviesPanel.removeAll();

        JLabel label = new JLabel(
            "<html><center>" +
            escapeHtml(message).replace("\n", "<br>") +
            "</center></html>",
            SwingConstants.CENTER
        );

        label.setForeground(TEXT);
        moviesPanel.add(label);
        moviesPanel.revalidate();
        moviesPanel.repaint();
    }

    private String escapeHtml(String value) {
        if (value == null) return "";

        return value.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#39;");
    }
}
