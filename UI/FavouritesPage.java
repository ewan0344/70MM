
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.sql.*;

public class FavouritesPage extends JPanel {

    private final Main main;
    private final JPanel moviesPanel;

    private final Color BG = Color.decode("#171415");
    private final Color CARD = Color.decode("#241E20");
    private final Color GOLD = Color.decode("#D6A36A");
    private final Color RED = Color.decode("#A64B52");
    private final Color TEXT = Color.decode("#F1E9DD");

    public FavouritesPage(Main main) {
        this.main = main;

        setLayout(new BorderLayout(10, 10));
        setBackground(BG);

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(BG);

        JButton back = new JButton("← Back");
        back.setBackground(RED);
        back.setForeground(TEXT);
        back.addActionListener(e -> main.showPage("profile"));

        JLabel title = new JLabel("My Favourites", SwingConstants.CENTER);
        title.setForeground(GOLD);
        title.setFont(new Font("Arial", Font.BOLD, 26));

        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> loadMovies());

        top.add(back, BorderLayout.WEST);
        top.add(title, BorderLayout.CENTER);
        top.add(refresh, BorderLayout.EAST);

        moviesPanel = new JPanel(new GridLayout(0, 3, 10, 10));
        moviesPanel.setBackground(BG);
        moviesPanel.setBorder(
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        );

        JScrollPane scroll = new JScrollPane(moviesPanel);
        scroll.getViewport().setBackground(BG);

        add(top, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);

        loadMovies();
    }

    private void loadMovies() {
        moviesPanel.removeAll();

        String sql =
            "SELECT m.tmdb_id, m.title, d.rating, " +
            "CASE WHEN f.tmdb_id IS NULL THEN 0 ELSE 1 END AS is_favourite " +
            "FROM diary_entries d " +
            "JOIN movies_new m ON m.tmdb_id = d.tmdb_id " +
            "JOIN mm70_users.users u ON u.username = ? " +
            "LEFT JOIN mm70_users.user_favourites f " +
            "ON f.user_id = u.user_id AND f.tmdb_id = m.tmdb_id " +
            "WHERE d.user_id = u.user_id " +
            "ORDER BY d.entry_id DESC";

        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, main.getUsername());

            try (ResultSet rs = ps.executeQuery()) {
                int count = 0;

                while (rs.next()) {
                    int id = rs.getInt("tmdb_id");
                    String movieTitle = rs.getString("title");
                    String rating = rs.getString("rating");
                    boolean favourite = rs.getInt("is_favourite") == 1;

                    JPanel card = new JPanel(new BorderLayout(5, 5));
                    card.setBackground(CARD);
                    card.setBorder(
                        BorderFactory.createEmptyBorder(8, 8, 8, 8)
                    );

                    JLabel poster = new JLabel("", SwingConstants.CENTER);
                    poster.setPreferredSize(new Dimension(130, 170));

                    File file = new File("posters/" + id + ".jpg");

                    if (file.exists()) {
                        ImageIcon icon = new ImageIcon(file.getAbsolutePath());
                        Image image = icon.getImage().getScaledInstance(
                            120, 160, Image.SCALE_SMOOTH
                        );
                        poster.setIcon(new ImageIcon(image));
                    } else {
                        poster.setText("No Poster");
                        poster.setForeground(TEXT);
                    }

                    JLabel name = new JLabel(
                        "<html><center>" + escapeHtml(movieTitle) +
                        "</center></html>",
                        SwingConstants.CENTER
                    );
                    name.setForeground(GOLD);

                    JLabel score = new JLabel(
                        "Rating: " + rating + "/10",
                        SwingConstants.CENTER
                    );
                    score.setForeground(TEXT);

                    JButton favouriteButton = new JButton(
                        favourite ? "♥ Remove Favourite" : "♡ Add Favourite"
                    );
                    favouriteButton.setBackground(RED);
                    favouriteButton.setForeground(TEXT);
                    favouriteButton.setFocusPainted(false);

                    favouriteButton.addActionListener(e -> {
                        changeFavourite(id, !favourite);
                        loadMovies();
                    });

                    JPanel details = new JPanel(new GridLayout(3, 1, 4, 4));
                    details.setBackground(CARD);
                    details.add(name);
                    details.add(score);
                    details.add(favouriteButton);

                    card.add(poster, BorderLayout.CENTER);
                    card.add(details, BorderLayout.SOUTH);

                    moviesPanel.add(card);
                    count++;
                }

                if (count == 0) {
                    JLabel empty = new JLabel(
                        "<html><center>No diary movies found.<br>" +
                        "Add movies to your diary first.</center></html>",
                        SwingConstants.CENTER
                    );
                    empty.setForeground(TEXT);
                    moviesPanel.add(empty);
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();

            JLabel error = new JLabel(
                "Could not load favourites: " + ex.getMessage(),
                SwingConstants.CENTER
            );
            error.setForeground(TEXT);
            moviesPanel.add(error);
        }

        moviesPanel.revalidate();
        moviesPanel.repaint();
    }

    private void changeFavourite(int movieId, boolean add) {
        String sql;

        if (add) {
            sql =
                "INSERT INTO mm70_users.user_favourites (user_id, tmdb_id) " +
                "SELECT user_id, ? FROM mm70_users.users WHERE username = ? " +
                "AND EXISTS (" +
                "SELECT 1 FROM diary_entries d " +
                "WHERE d.tmdb_id = ? AND d.user_id = user_id" +
                ") " +
                "ON DUPLICATE KEY UPDATE tmdb_id = VALUES(tmdb_id)";
        } else {
            sql =
                "DELETE f FROM mm70_users.user_favourites f " +
                "JOIN mm70_users.users u ON u.user_id = f.user_id " +
                "WHERE u.username = ? AND f.tmdb_id = ? " +
                "AND EXISTS (" +
                "SELECT 1 FROM diary_entries d " +
                "WHERE d.tmdb_id = f.tmdb_id AND d.user_id = u.user_id" +
                ")";
        }

        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            if (add) {
                ps.setInt(1, movieId);
                ps.setString(2, main.getUsername());
                ps.setInt(3, movieId);
            } else {
                ps.setString(1, main.getUsername());
                ps.setInt(2, movieId);
            }

            ps.executeUpdate();

        } catch (Exception ex) {
            ex.printStackTrace();

            JOptionPane.showMessageDialog(
                this,
                "Could not update favourite:\n" + ex.getMessage()
            );
        }
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
