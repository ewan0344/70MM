
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

        JPanel header = new JPanel(new GridLayout(2, 1, 4, 4));
        header.setBackground(BG);

        JLabel profileTitle = new JLabel(
            friendUsername + "'s Profile", SwingConstants.CENTER);
        profileTitle.setFont(new Font("Arial", Font.BOLD, 26));
        profileTitle.setForeground(GOLD);

        JLabel favouritesTitle = new JLabel(
            "Favourites", SwingConstants.CENTER);
        favouritesTitle.setFont(new Font("Arial", Font.BOLD, 20));
        favouritesTitle.setForeground(TEXT);

        header.add(profileTitle);
        header.add(favouritesTitle);
        content.add(header, BorderLayout.NORTH);

        moviesPanel = new JPanel(new GridLayout(0, 3, 10, 10));
        moviesPanel.setBackground(BG);

        JScrollPane scroll = new JScrollPane(moviesPanel);
        scroll.getViewport().setBackground(BG);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        content.add(scroll, BorderLayout.CENTER);

        JButton closeButton = new JButton("Close");
        styleButton(closeButton);
        closeButton.addActionListener(e -> dispose());

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
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
            "SELECT m.tmdb_id, m.title, " +
            "(SELECT d.rating FROM diary_entries d " +
            " WHERE d.user_id = u.user_id AND d.tmdb_id = m.tmdb_id " +
            " ORDER BY d.entry_id DESC LIMIT 1) AS rating " +
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
                    int id = rs.getInt("tmdb_id");
                    String title = rs.getString("title");
                    String rating = rs.getString("rating");

                    JPanel card = new JPanel(new BorderLayout(5, 7));
                    card.setBackground(CARD);
                    card.setBorder(
                        BorderFactory.createEmptyBorder(8, 8, 8, 8));

                    JLabel poster = new JLabel("", SwingConstants.CENTER);
                    poster.setPreferredSize(new Dimension(150, 180));

                    File file = new File("posters/" + id + ".jpg");

                    if (file.exists()) {
                        ImageIcon icon = new ImageIcon(file.getAbsolutePath());
                        Image image = icon.getImage().getScaledInstance(
                            130, 170, Image.SCALE_SMOOTH);
                        poster.setIcon(new ImageIcon(image));
                    } else {
                        poster.setText("No Poster");
                        poster.setForeground(TEXT);
                    }

                    JLabel name = new JLabel(
                        "<html><center>" + escapeHtml(title) +
                        "</center></html>", SwingConstants.CENTER);
                    name.setForeground(GOLD);
                    name.setCursor(new Cursor(Cursor.HAND_CURSOR));

                    name.addMouseListener(new java.awt.event.MouseAdapter() {
                        @Override
                        public void mouseClicked(java.awt.event.MouseEvent e) {
                            main.showMovieDetails(id);
                        }
                    });

                    JLabel score = new JLabel(
                        rating == null
                            ? "Not rated"
                            : "Rating: " + rating + "/10",
                        SwingConstants.CENTER);
                    score.setForeground(TEXT);

                    JButton addButton = new JButton("Add to Watchlist");
                    styleButton(addButton);
                    addButton.addActionListener(e -> addToWatchlist(id));

                    JPanel details = new JPanel(new GridLayout(0, 1, 5, 5));
                    details.setBackground(CARD);
                    details.add(name);
                    details.add(score);
                    details.add(addButton);

                    card.add(poster, BorderLayout.CENTER);
                    card.add(details, BorderLayout.SOUTH);
                    moviesPanel.add(card);
                    count++;
                }

                if (count == 0) {
                    showMessage(
                        friendUsername + " hasn't added any favourites yet.");
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            showMessage("Could not load favourites: " + ex.getMessage());
        }

        moviesPanel.revalidate();
        moviesPanel.repaint();
    }

    private void addToWatchlist(int movieId) {
        String sql =
            "INSERT INTO watchlist_items (user_id, tmdb_id, status) " +
            "VALUES ((SELECT user_id FROM mm70_users.users WHERE username = ?), " +
            "?, 'WANTED')";

        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, main.getUsername());
            ps.setInt(2, movieId);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(
                this, "Added to your watchlist!");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                this,
                "Could not add to watchlist. Check that watchlist_items " +
                "has a user_id column.\nError: " + ex.getMessage());
        }
    }

    private void showMessage(String message) {
        moviesPanel.removeAll();

        JLabel label = new JLabel(
            "<html><center>" +
            escapeHtml(message).replace("\n", "<br>") +
            "</center></html>",
            SwingConstants.CENTER);

        label.setForeground(TEXT);
        moviesPanel.add(label);
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
