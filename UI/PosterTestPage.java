
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PosterTestPage extends JPanel {

    private Main main;
    private JTextField searchField;
    private JPopupMenu suggestions;
    private JPanel resultsPanel;

    public PosterTestPage(Main main) {
        this.main = main;

        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);

        // Top section
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBackground(Color.WHITE);

        JButton backButton = new JButton("← Back");
        backButton.addActionListener(e -> main.showPage("diary"));

        JLabel heading = new JLabel("Poster Testing");
        heading.setFont(new Font("Arial", Font.BOLD, 24));

        JPanel headingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headingPanel.setBackground(Color.WHITE);
        headingPanel.add(backButton);
        headingPanel.add(heading);

        searchField = new JTextField();
        searchField.setFont(new Font("Arial", Font.PLAIN, 16));
        searchField.setPreferredSize(new Dimension(300, 35));

        suggestions = new JPopupMenu();

        searchField.getDocument().addDocumentListener(
            new javax.swing.event.DocumentListener() {
                public void insertUpdate(
                        javax.swing.event.DocumentEvent e) {
                    updateSuggestions();
                }

                public void removeUpdate(
                        javax.swing.event.DocumentEvent e) {
                    updateSuggestions();
                }

                public void changedUpdate(
                        javax.swing.event.DocumentEvent e) {
                    updateSuggestions();
                }
            }
        );

        JPanel searchPanel = new JPanel(new BorderLayout(5, 5));
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setBorder(
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        );

        searchPanel.add(new JLabel("Search movie:"), BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);

        topPanel.add(headingPanel, BorderLayout.NORTH);
        topPanel.add(searchPanel, BorderLayout.CENTER);

        // Area displaying selected movies and posters
        resultsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        resultsPanel.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(resultsPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void updateSuggestions() {
        String query = searchField.getText().trim();

        suggestions.setVisible(false);

        if (query.isEmpty()) {
            return;
        }

        List<Movie> movies = searchMovies(query);

        if (movies.isEmpty()) {
            return;
        }

        suggestions.removeAll();

        for (Movie movie : movies) {
            JMenuItem item = new JMenuItem(
                movie.title + " (" + movie.id + ")"
            );

            item.addActionListener(e -> {
                suggestions.setVisible(false);
                searchField.setText(movie.title);
                addMovieCard(movie);
            });

            suggestions.add(item);
        }

        suggestions.show(searchField, 0, searchField.getHeight());
    }

    private List<Movie> searchMovies(String query) {
        List<Movie> movies = new ArrayList<>();

        String sql =
            "SELECT tmdb_id, title FROM movies_new " +
            "WHERE title LIKE ? ORDER BY title LIMIT 10";

        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, query + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    movies.add(new Movie(
                        rs.getInt("tmdb_id"),
                        rs.getString("title")
                    ));
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                this,
                "Could not search movies. Check your database connection."
            );
        }

        return movies;
    }

    private void addMovieCard(Movie movie) {
        // Avoid adding the same movie twice
        for (Component component : resultsPanel.getComponents()) {
            if (component instanceof JPanel
                    && movie.id == (int) ((JPanel) component).getClientProperty("movieId")) {
                return;
            }
        }

        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.putClientProperty("movieId", movie.id);
        card.setPreferredSize(new Dimension(145, 255));
        card.setBackground(Color.WHITE);
        card.setBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY)
        );

        JLabel posterLabel = new JLabel("", SwingConstants.CENTER);
        posterLabel.setPreferredSize(new Dimension(135, 195));

        File posterFile = new File(
            "posters" + File.separator + movie.id + ".jpg"
        );

        if (posterFile.exists()) {
            ImageIcon original = new ImageIcon(posterFile.getAbsolutePath());

            Image scaled = original.getImage().getScaledInstance(
                130, 190, Image.SCALE_SMOOTH
            );

            posterLabel.setIcon(new ImageIcon(scaled));
        } else {
            posterLabel.setText("Poster not found");
        }

        JLabel titleLabel = new JLabel(
            "<html><center>" + escapeHtml(movie.title) +
            "</center></html>",
            SwingConstants.CENTER
        );

        titleLabel.setVerticalAlignment(SwingConstants.TOP);

        card.add(posterLabel, BorderLayout.CENTER);
        card.add(titleLabel, BorderLayout.SOUTH);

        resultsPanel.add(card);
        resultsPanel.revalidate();
        resultsPanel.repaint();
    }

    private String escapeHtml(String text) {
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;");
    }

    private static class Movie {
        int id;
        String title;

        Movie(int id, String title) {
            this.id = id;
            this.title = title;
        }
    }
}
