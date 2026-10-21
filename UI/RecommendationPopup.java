import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

public class RecommendationPopup extends JDialog {

    private final JPanel moviePanel = new JPanel();
    private Main main;

    public RecommendationPopup(Main main, Window owner,
            List<RecommendedMovie> movies) {

        super(owner, "Your 70mm Recommendations",
                Dialog.ModalityType.APPLICATION_MODAL);

        this.main = main;
        setSize(850, 620);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(Color.decode("#171415"));

        JLabel heading = new JLabel(
                "Films for your mood", SwingConstants.CENTER);
        heading.setFont(new Font("Arial", Font.BOLD, 26));
        heading.setForeground(Color.decode("#D6A36A"));
        heading.setBorder(BorderFactory.createEmptyBorder(
                15, 10, 10, 10));

        moviePanel.setLayout(new GridLayout(0, 3, 15, 15));
        moviePanel.setBackground(Color.decode("#171415"));
        moviePanel.setBorder(BorderFactory.createEmptyBorder(
                10, 15, 15, 15));

        for (RecommendedMovie movie : movies) {
            moviePanel.add(createMovieCard(movie));
        }

        JScrollPane scrollPane = new JScrollPane(moviePanel);
        scrollPane.getViewport().setBackground(Color.decode("#171415"));
        scrollPane.setBorder(null);

        JButton closeButton = new JButton("CLOSE");
        closeButton.addActionListener(e -> dispose());

        JPanel bottom = new JPanel();
        bottom.setBackground(Color.decode("#171415"));
        bottom.add(closeButton);

        add(heading, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    private JPanel createMovieCard(RecommendedMovie movie) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(Color.decode("#242021"));
        card.setBorder(BorderFactory.createLineBorder(
                Color.decode("#D6A36A")));

        JLabel poster = new JLabel("Loading poster...",
                SwingConstants.CENTER);
        poster.setForeground(Color.WHITE);
        poster.setPreferredSize(new Dimension(180, 210));

        loadPoster(movie.getPosterPath(), poster);

        String title = movie.getTitle() == null
                ? "Untitled"
                : movie.getTitle();

        String yearText = movie.getYear() == null
                ? ""
                : " (" + movie.getYear() + ")";

        JLabel titleLabel = new JLabel(
                "<html><div style='text-align:center;'>"
                + escapeHtml(title) + escapeHtml(yearText)
                + "</div></html>",
                SwingConstants.CENTER);

        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(
                5, 5, 5, 5));
        titleLabel.setCursor(
        	    Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        	);

        	titleLabel.addMouseListener(new java.awt.event.MouseAdapter() {
        	    @Override
        	    public void mouseClicked(java.awt.event.MouseEvent e) {
        	        dispose();
        	        main.showMovieDetails(movie.getId());
        	    }
        	});
        JButton addButton = new JButton("ADD TO WATCHLIST");
        addButton.setForeground(Color.WHITE);
        addButton.setBackground(Color.decode("#A64B52"));

        addButton.addActionListener(e -> {
            addButton.setEnabled(false);

            new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return saveToWatchlist(movie.getId());
                }

                @Override
                protected void done() {
                    try {
                        boolean added = get();

                        JOptionPane.showMessageDialog(
                                RecommendationPopup.this,
                                added ? "Movie added to your Watchlist!"
                                      : "This movie is already in your Watchlist."
                        );
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(
                                RecommendationPopup.this,
                                "Could not add movie.\n"
                                + ex.getMessage(),
                                "Watchlist Error",
                                JOptionPane.ERROR_MESSAGE
                        );
                    } finally {
                        addButton.setEnabled(true);
                    }
                }
            }.execute();
        });

        card.add(poster, BorderLayout.NORTH);
        card.add(titleLabel, BorderLayout.CENTER);
        card.add(addButton, BorderLayout.SOUTH);

        return card;
    }

    private void loadPoster(String posterPath, JLabel posterLabel) {
        if (posterPath == null || posterPath.isBlank()) {
            posterLabel.setText("Poster unavailable");
            return;
        }

        new SwingWorker<ImageIcon, Void>() {
            @Override
            protected ImageIcon doInBackground() {
                try {
                    ImageIcon original;

                    File file = new File(posterPath);

                    if (file.isFile()) {
                        original = new ImageIcon(file.getAbsolutePath());
                    } else {
                        String url = posterPath.startsWith("http://")
                                || posterPath.startsWith("https://")
                                ? posterPath
                                : "https://image.tmdb.org/t/p/w500"
                                    + (posterPath.startsWith("/")
                                        ? posterPath
                                        : "/" + posterPath);

                        original = new ImageIcon(new URL(url));
                    }

                    if (original.getIconWidth() <= 0
                            || original.getIconHeight() <= 0) {
                        return null;
                    }

                    Image scaled = original.getImage().getScaledInstance(
                            150, 205, Image.SCALE_SMOOTH);

                    return new ImageIcon(scaled);

                } catch (Exception ex) {
                    return null;
                }
            }

            @Override
            protected void done() {
                try {
                    ImageIcon icon = get();

                    if (icon == null) {
                        posterLabel.setText("Poster unavailable");
                    } else {
                        posterLabel.setText("");
                        posterLabel.setIcon(icon);
                    }
                } catch (Exception ex) {
                    posterLabel.setText("Poster unavailable");
                }
            }
        }.execute();
    }

    private boolean saveToWatchlist(int movieId) throws Exception {
        String sql =
            "INSERT IGNORE INTO watchlist_items (tmdb_id, status) " +
            "VALUES (?, 'WANTED')";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, movieId);
            return statement.executeUpdate() > 0;
        }
    }

    private String escapeHtml(String text) {
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;");
    }
}