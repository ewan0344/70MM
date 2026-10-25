

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class MovieDetailsPage extends JPanel {

    private Main main;
    private int movieId;

    private final Color BG = Color.decode("#171415");
    private final Color GOLD = Color.decode("#D6A36A");
    private final Color RED = Color.decode("#A64B52");

    public MovieDetailsPage(Main main, int movieId) {
        this.main = main;
        this.movieId = movieId;

        setLayout(new BorderLayout(15, 15));
        setBackground(BG);
        setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JButton backButton = new JButton("Back");
        backButton.setBackground(RED);
        backButton.setForeground(Color.WHITE);
        backButton.addActionListener(e -> main.showPage("home"));

        JLabel heading = new JLabel("70mm | MOVIE DETAILS");
        heading.setForeground(GOLD);
        heading.setFont(new Font("Arial", Font.BOLD, 22));

        topPanel.add(backButton, BorderLayout.WEST);
        topPanel.add(heading, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBackground(BG);

        JScrollPane scroll = new JScrollPane(detailsPanel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(BG);
        add(scroll, BorderLayout.CENTER);

        loadMovieDetails(detailsPanel);
    }

    private void loadMovieDetails(JPanel detailsPanel) {
        String sql =
            "SELECT title, duration, director, production_house, " +
            "release_year, genres, age_rating " +
            "FROM movies_new WHERE tmdb_id = ?";

        SwingWorker<String[], Void> worker = new SwingWorker<String[], Void>() {
            @Override
            protected String[] doInBackground() throws Exception {
                try (Connection con = DatabaseManager.getConnection();
                     PreparedStatement ps = con.prepareStatement(sql)) {

                    ps.setInt(1, movieId);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            return new String[] {
                                rs.getString("title"),
                                rs.getString("duration"),
                                rs.getString("director"),
                                rs.getString("production_house"),
                                rs.getString("release_year"),
                                rs.getString("genres"),
                                rs.getString("age_rating")
                            };
                        }
                    }
                }
                return null;
            }

            @Override
            protected void done() {
                detailsPanel.removeAll();

                try {
                    String[] movie = get();

                    if (movie == null) {
                        addDetail(detailsPanel, "Movie", "Movie not found");
                    } else {
                        JLabel titleLabel = new JLabel(
                            movie[0] == null ? "Untitled" : movie[0]);
                        titleLabel.setFont(new Font("Arial", Font.BOLD, 30));
                        titleLabel.setForeground(GOLD);
                        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

                        detailsPanel.add(titleLabel);
                        detailsPanel.add(Box.createVerticalStrut(20));

                        addDetail(detailsPanel, "Duration",
                            movie[1] == null ? null : movie[1] + " minutes");
                        addDetail(detailsPanel, "Director", movie[2]);
                        addDetail(detailsPanel, "Production House", movie[3]);
                        addDetail(detailsPanel, "Release Year", movie[4]);
                        addDetail(detailsPanel, "Genres", movie[5]);
                        addDetail(detailsPanel, "Age Rating", movie[6]);

                        detailsPanel.add(Box.createVerticalStrut(20));

                        JButton favouriteButton = new JButton("♡ Add to Favourites");
                        favouriteButton.setBackground(RED);
                        favouriteButton.setForeground(Color.WHITE);
                        favouriteButton.setFocusPainted(false);
                        favouriteButton.setAlignmentX(Component.LEFT_ALIGNMENT);
                        favouriteButton.addActionListener(
                            e -> addToFavourites(favouriteButton));

                        detailsPanel.add(favouriteButton);
                    }

                } catch (Exception ex) {
                    addDetail(detailsPanel, "Error",
                        "Could not load movie details.");
                    ex.printStackTrace();
                }

                detailsPanel.revalidate();
                detailsPanel.repaint();
            }
        };

        worker.execute();
    }

    private void addToFavourites(JButton button) {
        String sql =
            "INSERT INTO mm70_users.user_favourites (user_id, tmdb_id) " +
            "SELECT u.user_id, ? FROM mm70_users.users u " +
            "WHERE u.username = ? " +
            "AND EXISTS (SELECT 1 FROM diary_entries d " +
            "WHERE d.tmdb_id = ? AND d.user_id = u.user_id) " +
            "ON DUPLICATE KEY UPDATE tmdb_id = VALUES(tmdb_id)";

        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, movieId);
            ps.setString(2, main.getUsername());
            ps.setInt(3, movieId);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                button.setText("♥ In Favourites");
                JOptionPane.showMessageDialog(
                    this, "Movie added to your favourites!");
            } else {
                JOptionPane.showMessageDialog(
                    this,
                    "Add this movie to your diary first to favourite it.");
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                this, "Could not update favourites:\n" + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void addDetail(JPanel panel, String heading, String value) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row.setBackground(BG);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel headingLabel = new JLabel(heading + ": ");
        headingLabel.setFont(new Font("Arial", Font.BOLD, 16));
        headingLabel.setForeground(GOLD);

        JLabel valueLabel = new JLabel(
            value == null || value.isBlank() ? "Not available" : value);
        valueLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        valueLabel.setForeground(Color.WHITE);

        row.add(headingLabel);
        row.add(valueLabel);

        panel.add(row);
        panel.add(Box.createVerticalStrut(8));
    }
}
