
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
        styleButton(back);
        back.addActionListener(e -> main.showPage("profile"));

        JLabel title = new JLabel("My Favourites", SwingConstants.CENTER);
        title.setForeground(GOLD);
        title.setFont(new Font("Arial", Font.BOLD, 26));

        JButton refresh = new JButton("Refresh");
        styleButton(refresh);
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
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        add(top, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(
                    java.awt.event.ComponentEvent e) {
                loadMovies();
            }
        });

        loadMovies();
    }

    private void styleButton(JButton button) {
        button.setBackground(RED);
        button.setForeground(TEXT);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void loadMovies() {
        moviesPanel.removeAll();

        // Load only movies explicitly saved as favourites.
        // The most recent diary rating is displayed when available.
        String sql =
            "SELECT m.tmdb_id, m.title, " +
            "(SELECT d.rating FROM diary_entries d " +
            " JOIN mm70_users.users du ON du.user_id = d.user_id " +
            " WHERE du.user_id = u.user_id " +
            " AND d.tmdb_id = m.tmdb_id " +
            " ORDER BY d.entry_id DESC LIMIT 1) AS rating " +
            "FROM mm70_users.user_favourites f " +
            "JOIN mm70_users.users u ON u.user_id = f.user_id " +
            "JOIN movies_new m ON m.tmdb_id = f.tmdb_id " +
            "WHERE u.username = ? " +
            "ORDER BY f.created_at DESC";

        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, main.getUsername());

            try (ResultSet rs = ps.executeQuery()) {
                int count = 0;

                while (rs.next()) {
                    int id = rs.getInt("tmdb_id");
                    String movieTitle = rs.getString("title");
                    String rating = rs.getString("rating");

                    JPanel card = new JPanel(new BorderLayout(5, 5));
                    card.setBackground(CARD);
                    card.setBorder(
                        BorderFactory.createEmptyBorder(8, 8, 8, 8)
                    );

                    JLabel poster = new JLabel("", SwingConstants.CENTER);
                    poster.setPreferredSize(new Dimension(180, 200));

                    File file = new File("posters/" + id + ".jpg");

                    if (file.exists()) {
                        ImageIcon icon = new ImageIcon(
                            file.getAbsolutePath()
                        );

                        Image image = icon.getImage().getScaledInstance(
                            150, 200, Image.SCALE_SMOOTH
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
                    name.setCursor(new Cursor(Cursor.HAND_CURSOR));

                    name.addMouseListener(
                        new java.awt.event.MouseAdapter() {
                            @Override
                            public void mouseClicked(
                                    java.awt.event.MouseEvent e) {
                                main.showMovieDetails(id);
                            }
                        }
                    );

                    JLabel score = new JLabel(
                        rating == null
                            ? "Not rated in Diary"
                            : "Rating: " + rating + "/10",
                        SwingConstants.CENTER
                    );
                    score.setForeground(TEXT);

                    JButton removeButton =
                        new JButton("♥ Remove Favourite");
                    styleButton(removeButton);

                    removeButton.addActionListener(e -> {
                        removeFavourite(id);
                    });

                    JPanel details = new JPanel(
                        new GridLayout(3, 1, 5, 5)
                    );
                    details.setBackground(CARD);
                    details.add(name);
                    details.add(score);
                    details.add(removeButton);

                    card.add(poster, BorderLayout.CENTER);
                    card.add(details, BorderLayout.SOUTH);

                    moviesPanel.add(card);
                    count++;
                }

                if (count == 0) {
                    JLabel empty = new JLabel(
                        "<html><center>You haven't added any favourites yet.<br>" +
                        "Open your Diary and press ♡ Add Favourite below a movie.</center></html>",
                        SwingConstants.CENTER
                    );
                    empty.setForeground(TEXT);
                    moviesPanel.add(empty);
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();

            JLabel error = new JLabel(
                "<html><center>Could not load favourites.<br>" +
                escapeHtml(ex.getMessage()) + "</center></html>",
                SwingConstants.CENTER
            );
            error.setForeground(TEXT);
            moviesPanel.add(error);
        }

        moviesPanel.revalidate();
        moviesPanel.repaint();
    }

    private void removeFavourite(int movieId) {
        String sql =
            "DELETE f FROM mm70_users.user_favourites f " +
            "JOIN mm70_users.users u ON u.user_id = f.user_id " +
            "WHERE u.username = ? AND f.tmdb_id = ?";

        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, main.getUsername());
            ps.setInt(2, movieId);

            int deleted = ps.executeUpdate();

            if (deleted > 0) {
                loadMovies();
            } else {
                JOptionPane.showMessageDialog(
                    this, "Favourite was not found."
                );
            }

        } catch (Exception ex) {
            ex.printStackTrace();

            JOptionPane.showMessageDialog(
                this,
                "Could not remove favourite:\n" + ex.getMessage()
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
