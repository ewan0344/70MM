import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class MovieDetailsPage extends JPanel {

    private Main main;
    private int movieId;

    public MovieDetailsPage(Main main, int movieId) {
        this.main = main;
        this.movieId = movieId;

        setLayout(new BorderLayout(15, 15));
        setBackground(Color.decode("#171415"));
        setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        // Top section
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> main.showPage("home"));

        JLabel heading = new JLabel("70mm | MOVIE DETAILS");
        heading.setForeground(Color.decode("#D6A36A"));
        heading.setFont(new Font("Arial", Font.BOLD, 22));

        topPanel.add(backButton, BorderLayout.WEST);
        topPanel.add(heading, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);

        // Movie information section
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBackground(Color.decode("#171415"));

        JLabel loadingLabel = new JLabel("Loading movie details...");
        loadingLabel.setForeground(Color.WHITE);
        detailsPanel.add(loadingLabel);

        add(detailsPanel, BorderLayout.CENTER);

        loadMovieDetails(detailsPanel);
    }

    private void loadMovieDetails(JPanel detailsPanel) {

        String sql = "SELECT title, duration, director, production_house, "
                   + "release_year, genres, age_rating "
                   + "FROM movies_new WHERE tmdb_id = ?";

        SwingWorker<String[], Void> worker = new SwingWorker<String[], Void>() {

            @Override
            protected String[] doInBackground() throws Exception {

                try (Connection con = DatabaseManager.getConnection();
                     PreparedStatement ps = con.prepareStatement(sql)) {

                    ps.setInt(1, movieId);

                    try (ResultSet rs = ps.executeQuery()) {

                        if (rs.next()) {
                            String[] movie = new String[7];

                            movie[0] = rs.getString("title");
                            movie[1] = rs.getString("duration");
                            movie[2] = rs.getString("director");
                            movie[3] = rs.getString("production_house");
                            movie[4] = rs.getString("release_year");
                            movie[5] = rs.getString("genres");
                            movie[6] = rs.getString("age_rating");

                            return movie;
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
                            movie[0] == null ? "Untitled" : movie[0]
                        );

                        titleLabel.setFont(
                            new Font("Arial", Font.BOLD, 30)
                        );
                        titleLabel.setForeground(Color.decode("#D6A36A"));
                        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

                        detailsPanel.add(titleLabel);
                        detailsPanel.add(Box.createVerticalStrut(20));

                        addDetail(detailsPanel, "Duration",
                            movie[1] == null ? "Not available"
                            : movie[1] + " minutes");

                        addDetail(detailsPanel, "Director", movie[2]);
                        addDetail(detailsPanel, "Production House", movie[3]);
                        addDetail(detailsPanel, "Release Year", movie[4]);
                        addDetail(detailsPanel, "Genres", movie[5]);
                        addDetail(detailsPanel, "Age Rating", movie[6]);
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

    private void addDetail(JPanel panel, String heading, String value) {

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row.setBackground(Color.decode("#171415"));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel headingLabel = new JLabel(heading + ": ");
        headingLabel.setFont(new Font("Arial", Font.BOLD, 16));
        headingLabel.setForeground(Color.decode("#D6A36A"));

        JLabel valueLabel = new JLabel(
            value == null || value.isBlank()
                ? "Not available"
                : value
        );
        valueLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        valueLabel.setForeground(Color.WHITE);

        row.add(headingLabel);
        row.add(valueLabel);

        panel.add(row);
        panel.add(Box.createVerticalStrut(8));
    }
}