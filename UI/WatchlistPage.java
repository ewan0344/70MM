import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
public class WatchlistPage extends JPanel {
    private final Main main;
    private final JPanel watchedPanel = new JPanel(new GridLayout(0, 3, 12, 12));
    private final JPanel wantedPanel = new JPanel(new GridLayout(0, 3, 12, 12));
    private static final Color BG = new Color(20, 20, 28);
    private static final Color CARD = new Color(35, 35, 46);
    private static final Color TEXT = new Color(245, 245, 250);
    private static final Color ACCENT = new Color(190, 55, 75);
    public WatchlistPage(Main main) {
        this.main = main;
        setLayout(new BorderLayout(12, 12));
        setBackground(BG);
        setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JButton back = new JButton("← Back");
        styleButton(back);
        back.addActionListener(e -> main.showPage("home"));
        JLabel title = new JLabel("My Watchlist");
        title.setForeground(TEXT);
        title.setFont(new Font("SansSerif", Font.BOLD, 26));
        JButton add = new JButton("+ Add Movie");
        styleButton(add);
        add.addActionListener(e -> openAddMovieDialog());
        top.add(back, BorderLayout.WEST);
        top.add(title, BorderLayout.CENTER);
        top.add(add, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);
        JPanel lists = new JPanel(new GridLayout(1, 2, 18, 0));
        lists.setOpaque(false);
        lists.add(makeSection("WANTED", wantedPanel));
        lists.add(makeSection("WATCHED", watchedPanel));
        JScrollPane scroll = new JScrollPane(lists);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
        ensureWatchlistTable();
        refresh();
    }
    private JPanel makeSection(String heading, JPanel cards) {
        JPanel section = new JPanel(new BorderLayout(0, 10));
        section.setOpaque(false);
        JLabel label = new JLabel(heading);
        label.setForeground(new Color(230, 190, 120));
        label.setFont(new Font("SansSerif", Font.BOLD, 17));
        cards.setOpaque(false);
        section.add(label, BorderLayout.NORTH);
        section.add(cards, BorderLayout.CENTER);
        return section;
    }
    private void styleButton(JButton button) {
        button.setFocusPainted(false);
        button.setForeground(Color.WHITE);
        button.setBackground(ACCENT);
        button.setBorder(BorderFactory.createEmptyBorder(8, 13, 8, 13));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
    private void ensureWatchlistTable() {
        String sql =
            "CREATE TABLE IF NOT EXISTS watchlist_items (" +
            "tmdb_id INT PRIMARY KEY, " +
            "status VARCHAR(20) NOT NULL, " +
            "FOREIGN KEY (tmdb_id) REFERENCES movies_new(tmdb_id) " +
            "ON DELETE CASCADE)";
        try (Connection con = DatabaseManager.getConnection();
             Statement st = con.createStatement()) {
            st.executeUpdate(sql);
        } catch (SQLException ex) {
            showError("Could not prepare watchlist table.", ex);
        }
    }
    private void refresh() {
        watchedPanel.removeAll();
        wantedPanel.removeAll();
        String sql =
            "SELECT m.tmdb_id, m.title, m.poster_path, w.status " +
            "FROM watchlist_items w " +
            "JOIN movies_new m ON m.tmdb_id = w.tmdb_id " +
            "ORDER BY m.title";
        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Movie movie = new Movie(
                    rs.getInt("tmdb_id"),
                    rs.getString("title"),
                    rs.getString("poster_path")
                );
                if ("WATCHED".equalsIgnoreCase(rs.getString("status"))) {
                    watchedPanel.add(movieCard(movie, true));
                } else {
                    wantedPanel.add(movieCard(movie, false));
                }
            }
        } catch (SQLException ex) {
            showError("Could not load your watchlist.", ex);
        }
        watchedPanel.revalidate();
        watchedPanel.repaint();
        wantedPanel.revalidate();
        wantedPanel.repaint();
    }
    private JPanel movieCard(Movie movie, boolean watched) {
        JPanel card = new JPanel(new BorderLayout(6, 8));
        card.setBackground(CARD);
        card.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel poster = new JLabel("Loading poster...", SwingConstants.CENTER);
        poster.setForeground(Color.LIGHT_GRAY);
        poster.setPreferredSize(new Dimension(150, 205));
        card.add(poster, BorderLayout.NORTH);
        JLabel name = new JLabel(
            "<html><div style='text-align:center;width:130px'>" +
            escapeHtml(movie.title) + "</div></html>",
            SwingConstants.CENTER
        );
        name.setForeground(TEXT);
        name.setFont(new Font("SansSerif", Font.BOLD, 13));
        card.add(name, BorderLayout.CENTER);
<<<<<<< HEAD
=======
        //clickable title
        name.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        name.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                main.showMovieDetails(movie.id);
            }
        });

>>>>>>> branch 'main' of https://github.com/ewan0344/70MM.git
        JPanel actions = new JPanel(new GridLayout(0, 1, 4, 4));
        actions.setOpaque(false);
        if (!watched) {
            JButton mark = new JButton("Mark Watched");
            styleButton(mark);
            mark.addActionListener(e -> updateStatus(movie.id, "WATCHED"));
            actions.add(mark);
        }
        JButton remove = new JButton("Remove");
        styleButton(remove);
        remove.addActionListener(e -> removeMovie(movie.id));
        actions.add(remove);
        card.add(actions, BorderLayout.SOUTH);
        // Download the poster in the background.
        new SwingWorker<ImageIcon, Void>() {
            @Override
            protected ImageIcon doInBackground() {
                return loadPoster(movie.posterPath, 150, 205);
            }
            @Override
            protected void done() {
                try {
                    ImageIcon icon = get();
                    if (icon != null) {
                        poster.setText("");
                        poster.setIcon(icon);
                    } else {
                        poster.setText("No poster available");
                    }
                } catch (Exception ex) {
                    poster.setText("Poster unavailable");
                }
            }
        }.execute();
        return card;
    }
    private void openAddMovieDialog() {
        JDialog dialog = new JDialog(
            SwingUtilities.getWindowAncestor(this),
            "Add Movie to Watchlist",
            Dialog.ModalityType.APPLICATION_MODAL
        );
        dialog.setSize(570, 560);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(BG);
        dialog.setLayout(new BorderLayout(10, 10));
        JTextField search = new JTextField();
        search.setFont(new Font("SansSerif", Font.PLAIN, 16));
        search.setBorder(BorderFactory.createTitledBorder("Search movie"));
        DefaultListModel<Movie> model = new DefaultListModel<>();
        JList<Movie> list = new JList<>(model);
        list.setBackground(CARD);
        list.setForeground(TEXT);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setFixedCellHeight(35);
        JLabel status = new JLabel("Loading movies...");
        status.setForeground(TEXT);
        JButton addSelected = new JButton("Add Selected Movie");
        styleButton(addSelected);
        dialog.add(search, BorderLayout.NORTH);
        dialog.add(new JScrollPane(list), BorderLayout.CENTER);
        JPanel bottom = new JPanel(new BorderLayout(8, 8));
        bottom.setOpaque(false);
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        bottom.add(status, BorderLayout.CENTER);
        bottom.add(addSelected, BorderLayout.EAST);
        dialog.add(bottom, BorderLayout.SOUTH);
        List<Movie> allMovies = new ArrayList<>();
        // Read movies from MySQL in the background.
        new SwingWorker<List<Movie>, Void>() {
            @Override
            protected List<Movie> doInBackground() throws Exception {
                List<Movie> movies = new ArrayList<>();
                String sql =
                    "SELECT m.tmdb_id, m.title, m.poster_path " +
                    "FROM movies_new m " +
                    "WHERE NOT EXISTS (" +
                    "SELECT 1 FROM watchlist_items w " +
                    "WHERE w.tmdb_id = m.tmdb_id) " +
                    "ORDER BY m.title";
                try (Connection con = DatabaseManager.getConnection();
                     PreparedStatement ps = con.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        movies.add(new Movie(
                            rs.getInt("tmdb_id"),
                            rs.getString("title"),
                            rs.getString("poster_path")
                        ));
                    }
                }
                return movies;
            }
            @Override
            protected void done() {
                try {
                    allMovies.addAll(get());
                    filterMovies(search.getText(), allMovies, model);
                    status.setText(allMovies.size() + " movies available");
                } catch (Exception ex) {
                    status.setText("Could not load movies.");
                    showError("Could not load movies from movies_new.", ex);
                }
            }
        }.execute();
        search.getDocument().addDocumentListener(new DocumentListener() {
            private void changed() {
                filterMovies(search.getText(), allMovies, model);
            }
            public void insertUpdate(DocumentEvent e) {
                changed();
            }
            public void removeUpdate(DocumentEvent e) {
                changed();
            }
            public void changedUpdate(DocumentEvent e) {
                changed();
            }
        });
        addSelected.addActionListener(e -> {
            Movie selected = list.getSelectedValue();
            if (selected == null) {
                JOptionPane.showMessageDialog(dialog, "Please select a movie first.");
                return;
            }
            if (insertWanted(selected.id)) {
                dialog.dispose();
                refresh();
            }
        });
        dialog.setVisible(true);
    }
    private void filterMovies(
        String query,
        List<Movie> movies,
        DefaultListModel<Movie> model
    ) {
        String q = query == null ? "" : query.trim().toLowerCase();
        model.clear();
        for (Movie movie : movies) {
            if (movie.title.toLowerCase().contains(q)) {
                model.addElement(movie);
            }
        }
    }
    private ImageIcon loadPoster(String path, int width, int height) {
        if (path == null || path.trim().isEmpty()) {
            return null;
        }
        String url = path.trim();
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            if (!url.startsWith("/")) {
                url = "/" + url;
            }
            url = "https://image.tmdb.org/t/p/w342" + url;
        }
        try {
            URL posterUrl = new URL(url);
            ImageIcon original = new ImageIcon(posterUrl);
            if (original.getIconWidth() <= 0) {
                return null;
            }
            Image scaled = original.getImage().getScaledInstance(
                width, height, Image.SCALE_SMOOTH
            );
            return new ImageIcon(scaled);
        } catch (Exception ex) {
            return null;
        }
    }
    private boolean insertWanted(int movieId) {
        String sql =
            "INSERT INTO watchlist_items (tmdb_id, status) VALUES (?, 'WANTED')";
        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, movieId);
            ps.executeUpdate();
            return true;
        } catch (SQLException ex) {
            showError("Could not add this movie. It may already be in your watchlist.", ex);
            return false;
        }
    }
    private void updateStatus(int movieId, String status) {
        String sql = "UPDATE watchlist_items SET status = ? WHERE tmdb_id = ?";
        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, movieId);
            ps.executeUpdate();
            refresh();
        } catch (SQLException ex) {
            showError("Could not update movie status.", ex);
        }
    }
    private void removeMovie(int movieId) {
        int answer = JOptionPane.showConfirmDialog(
            this,
            "Remove this movie from your watchlist?",
            "Confirm removal",
            JOptionPane.YES_NO_OPTION
        );
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }
        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(
                 "DELETE FROM watchlist_items WHERE tmdb_id = ?")) {
            ps.setInt(1, movieId);
            ps.executeUpdate();
            refresh();
        } catch (SQLException ex) {
            showError("Could not remove movie.", ex);
        }
    }
    private void showError(String message, Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(
            this,
            message + "\n" + ex.getMessage(),
            "Watchlist Error",
            JOptionPane.ERROR_MESSAGE
        );
    }
    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;");
    }
    private static class Movie {
        final int id;
        final String title;
        final String posterPath;
        Movie(int id, String title, String posterPath) {
            this.id = id;
            this.title = title == null ? "Untitled movie" : title;
            this.posterPath = posterPath;
        }
        @Override
        public String toString() {
            return title;
        }
    }
}