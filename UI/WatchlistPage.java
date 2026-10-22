
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.io.File;
import java.sql.*;
import java.util.ArrayList;

public class WatchlistPage extends JPanel {

    Main main;

    JPanel moviePanel;

    Color BG = Color.decode("#14141C");
    Color CARD = Color.decode("#23232E");
    Color TEXT = Color.WHITE;
    Color ACCENT = Color.decode("#A64B52");

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

        moviePanel = new JPanel(new GridLayout(0, 3, 12, 12));
        moviePanel.setBackground(BG);

        JScrollPane scroll = new JScrollPane(moviePanel);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        add(scroll, BorderLayout.CENTER);

        ensureWatchlistTable();
        refresh();
    }

    void styleButton(JButton button) {
        button.setFocusPainted(false);
        button.setForeground(Color.WHITE);
        button.setBackground(ACCENT);
        button.setBorder(BorderFactory.createEmptyBorder(8, 13, 8, 13));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    void ensureWatchlistTable() {
        String sql = "CREATE TABLE IF NOT EXISTS watchlist_items (" +
                     "tmdb_id INT PRIMARY KEY, " +
                     "status VARCHAR(20) NOT NULL, " +
                     "CONSTRAINT fk_watchlist_movie " +
                     "FOREIGN KEY (tmdb_id) REFERENCES movies_new(tmdb_id) " +
                     "ON DELETE CASCADE)";

        try (Connection con = DatabaseManager.getConnection();
             Statement st = con.createStatement()) {

            st.executeUpdate(sql);

        } catch (Exception ex) {
            showError("Could not prepare watchlist table.", ex);
        }
    }

    void refresh() {
        moviePanel.removeAll();

        String sql = "SELECT m.tmdb_id, m.title " +
                     "FROM watchlist_items w " +
                     "JOIN movies_new m ON m.tmdb_id = w.tmdb_id " +
                     "WHERE w.status <> 'WATCHED' " +
                     "ORDER BY m.title";

        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("tmdb_id");
                String title = rs.getString("title");
                moviePanel.add(movieCard(id, title, false));
            }

        } catch (Exception ex) {
            showError("Could not load watchlist.", ex);
        }

        moviePanel.revalidate();
        moviePanel.repaint();
    }

    JPanel movieCard(int id, String title, boolean watched) {
        JPanel card = new JPanel(new BorderLayout(6, 8));
        card.setBackground(CARD);
        card.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel poster = new JLabel("", SwingConstants.CENTER);
        poster.setPreferredSize(new Dimension(150, 205));

        File file = new File("posters/" + id + ".jpg");

        if (file.exists()) {
            ImageIcon icon = new ImageIcon(file.getAbsolutePath());
            Image image = icon.getImage().getScaledInstance(
                150, 205, Image.SCALE_SMOOTH);
            poster.setIcon(new ImageIcon(image));
        } else {
            poster.setText("No poster");
            poster.setForeground(TEXT);
        }

        card.add(poster, BorderLayout.NORTH);

        JLabel name = new JLabel(
            "<html><div style='text-align:center;width:130px'>" +
            escapeHtml(title) + "</div></html>",
            SwingConstants.CENTER);

        name.setForeground(TEXT);
        name.setFont(new Font("SansSerif", Font.BOLD, 13));
        name.setCursor(new Cursor(Cursor.HAND_CURSOR));

        name.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                main.showMovieDetails(id);
            }
        });

        card.add(name, BorderLayout.CENTER);

        JPanel actions = new JPanel(new GridLayout(0, 1, 4, 4));
        actions.setOpaque(false);

        if (!watched) {
            JButton mark = new JButton("Mark Watched");
            styleButton(mark);

            mark.addActionListener(e -> markWatched(id));

            actions.add(mark);
        }

        JButton remove = new JButton("Remove");
        styleButton(remove);
        remove.addActionListener(e -> removeMovie(id));

        actions.add(remove);
        card.add(actions, BorderLayout.SOUTH);

        return card;
    }

    void markWatched(int movieId) {
        JPanel form = new JPanel(new GridLayout(3, 2, 10, 10));

        JTextField ratingField = new JTextField();
        JTextField dateField = new JTextField();
        JTextField reviewField = new JTextField();

        form.add(new JLabel("Rating (0-10)"));
        form.add(ratingField);

        form.add(new JLabel("Date watched (DD-MM-YYYY)"));
        form.add(dateField);

        form.add(new JLabel("Review"));
        form.add(reviewField);

        int result = JOptionPane.showConfirmDialog(
            this, form, "Mark Movie as Watched",
            JOptionPane.OK_CANCEL_OPTION);

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String rating = ratingField.getText().trim();
        String date = dateField.getText().trim();
        String review = reviewField.getText().trim();

        if (rating.isEmpty() || date.isEmpty()) {
            JOptionPane.showMessageDialog(
                this, "Enter a rating and date watched.");
            return;
        }

        double value;

        try {
            value = Double.parseDouble(rating);

            if (value < 0 || value > 10) {
                JOptionPane.showMessageDialog(
                    this, "Rating must be between 0 and 10.");
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter a valid rating.");
            return;
        }

        Connection con = null;

        try {
            con = DatabaseManager.getConnection();
            con.setAutoCommit(false);

            String insert = "INSERT INTO diary_entries " +
                            "(tmdb_id, rating, watched_date, review) " +
                            "VALUES (?, ?, ?, ?)";

            try (PreparedStatement ps = con.prepareStatement(insert)) {
                ps.setInt(1, movieId);
                ps.setDouble(2, value);
                ps.setString(3, date);
                ps.setString(4, review);
                ps.executeUpdate();
            }

            // Remove the movie from the watchlist only after adding it to Diary.
            String delete = "DELETE FROM watchlist_items WHERE tmdb_id = ?";

            try (PreparedStatement ps = con.prepareStatement(delete)) {
                ps.setInt(1, movieId);
                ps.executeUpdate();
            }

            con.commit();
            con.setAutoCommit(true);

            refresh();

            JOptionPane.showMessageDialog(
                this, "Movie moved to your Diary!");

            // Open Diary; DiaryPage refreshes its data whenever it becomes visible.
            main.showPage("diary");

        } catch (Exception ex) {
            if (con != null) {
                try {
                    con.rollback();
                    con.setAutoCommit(true);
                } catch (SQLException ignored) {
                }
            }

            showError("Could not mark movie as watched.", ex);

        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    void openAddMovieDialog() {
        JDialog dialog = new JDialog(
            SwingUtilities.getWindowAncestor(this),
            "Add Movie to Watchlist",
            Dialog.ModalityType.APPLICATION_MODAL);

        dialog.setSize(500, 450);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JTextField search = new JTextField();
        search.setBorder(BorderFactory.createTitledBorder("Search movie"));

        DefaultListModel<Movie> model = new DefaultListModel<>();
        JList<Movie> list = new JList<>(model);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JLabel status = new JLabel("Type to search movies");
        JButton addSelected = new JButton("Add Selected Movie");
        styleButton(addSelected);

        dialog.add(search, BorderLayout.NORTH);
        dialog.add(new JScrollPane(list), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(8, 8));
        bottom.add(status, BorderLayout.CENTER);
        bottom.add(addSelected, BorderLayout.EAST);
        dialog.add(bottom, BorderLayout.SOUTH);

        ArrayList<Movie> allMovies = new ArrayList<>();

        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(
                 "SELECT m.tmdb_id, m.title FROM movies_new m " +
                 "WHERE NOT EXISTS (SELECT 1 FROM watchlist_items w " +
                 "WHERE w.tmdb_id = m.tmdb_id) ORDER BY m.title");
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                allMovies.add(new Movie(
                    rs.getInt("tmdb_id"), rs.getString("title")));
            }

            status.setText(allMovies.size() + " movies available");

        } catch (Exception ex) {
            showError("Could not load movies.", ex);
        }

        search.getDocument().addDocumentListener(new DocumentListener() {
            void filter() {
                model.clear();
                String text = search.getText().trim().toLowerCase();

                for (Movie movie : allMovies) {
                    if (movie.title.toLowerCase().contains(text)) {
                        model.addElement(movie);
                    }
                }
            }

            public void insertUpdate(DocumentEvent e) {
                filter();
            }

            public void removeUpdate(DocumentEvent e) {
                filter();
            }

            public void changedUpdate(DocumentEvent e) {
                filter();
            }
        });

        addSelected.addActionListener(e -> {
            Movie selected = list.getSelectedValue();

            if (selected == null) {
                JOptionPane.showMessageDialog(
                    dialog, "Please select a movie first.");
                return;
            }

            if (insertWanted(selected.id)) {
                dialog.dispose();
                refresh();
            }
        });

        dialog.setVisible(true);
    }

    boolean insertWanted(int movieId) {
        String sql = "INSERT INTO watchlist_items (tmdb_id, status) " +
                     "VALUES (?, 'WANTED')";

        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, movieId);
            ps.executeUpdate();
            return true;

        } catch (Exception ex) {
            showError("Could not add movie. It may already be in your list.", ex);
            return false;
        }
    }

    void removeMovie(int movieId) {
        int answer = JOptionPane.showConfirmDialog(
            this, "Remove this movie from your watchlist?",
            "Confirm removal", JOptionPane.YES_NO_OPTION);

        if (answer != JOptionPane.YES_OPTION) {
            return;
        }

        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(
                 "DELETE FROM watchlist_items WHERE tmdb_id = ?")) {

            ps.setInt(1, movieId);
            ps.executeUpdate();
            refresh();

        } catch (Exception ex) {
            showError("Could not remove movie.", ex);
        }
    }

    String escapeHtml(String value) {
        if (value == null) {
            return "";
        }

        return value.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;");
    }

    void showError(String message, Exception ex) {
        ex.printStackTrace();

        JOptionPane.showMessageDialog(
            this, message + "\n" + ex.getMessage(),
            "Watchlist Error", JOptionPane.ERROR_MESSAGE);
    }

    static class Movie {
        int id;
        String title;

        Movie(int id, String title) {
            this.id = id;
            this.title = title == null ? "Untitled movie" : title;
        }

        public String toString() {
            return title;
        }
    }
}
