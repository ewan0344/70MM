import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Custom movie lists for 70mm.
 * A list works like a playlist: create it, open it, and add/remove movies.
 */
public class ListPage extends JPanel {

    private final Main main;

    private static final Color BG = new Color(23, 20, 21);
    private static final Color CARD = new Color(34, 32, 33);
    private static final Color TEXT = new Color(241, 233, 221);
    private static final Color GOLD = new Color(214, 163, 106);
    private static final Color ACCENT = new Color(166, 75, 82);

    private final CardLayout bodyLayout = new CardLayout();
    private final JPanel bodyPanel = new JPanel(bodyLayout);

    private final JPanel listPanel = new JPanel();
    private final JPanel moviePanel = new JPanel(new GridLayout(0, 3, 12, 12));

    private final JLabel pageTitle = new JLabel("My Lists", SwingConstants.CENTER);
    private final JButton backButton = new JButton("← Back");
    private final JButton actionButton = new JButton("+ Create List");

    private final ConcurrentHashMap<String, ImageIcon> posterCache =
            new ConcurrentHashMap<>();

    // -1 means the user is looking at the list overview.
    private int currentListId = -1;
    private String currentListName = "";

    public ListPage(Main main) {
        this.main = main;

        setLayout(new BorderLayout(12, 12));
        setBackground(BG);
        setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JPanel top = new JPanel(new BorderLayout(10, 0));
        top.setOpaque(false);

        styleButton(backButton);
        styleButton(actionButton);

        pageTitle.setForeground(GOLD);
        pageTitle.setFont(new Font("SansSerif", Font.BOLD, 25));

        top.add(backButton, BorderLayout.WEST);
        top.add(pageTitle, BorderLayout.CENTER);
        top.add(actionButton, BorderLayout.EAST);

        listPanel.setBackground(BG);
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBorder(BorderFactory.createEmptyBorder(20, 22, 20, 22));

        JScrollPane listScroll = new JScrollPane(listPanel);
        styleScrollPane(listScroll);

        moviePanel.setBackground(BG);
        moviePanel.setOpaque(true);
        moviePanel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JScrollPane movieScroll = new JScrollPane(moviePanel);
        styleScrollPane(movieScroll);

        bodyPanel.add(listScroll, "overview");
        bodyPanel.add(movieScroll, "movies");

        add(top, BorderLayout.NORTH);
        add(bodyPanel, BorderLayout.CENTER);

        backButton.addActionListener(e -> {
            if (currentListId != -1) {
                showOverview();
            } else {
                main.showPage("home");
            }
        });

        actionButton.addActionListener(e -> {
            if (currentListId == -1) {
                createList();
            } else {
                openAddMovieDialog(currentListId);
            }
        });

        ensureListTables();
        showOverview();
    }

    private void styleButton(JButton button) {
        button.setFocusPainted(false);
        button.setForeground(TEXT);
        button.setBackground(ACCENT);
        button.setBorder(BorderFactory.createEmptyBorder(8, 13, 8, 13));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void styleScrollPane(JScrollPane scroll) {
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(BG);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
    }

    /** Create the two tables the first time this page is opened. */
    private void ensureListTables() {
        String createLists =
                "CREATE TABLE IF NOT EXISTS custom_lists (" +
                "id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
                "name VARCHAR(100) NOT NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";

        String createItems =
                "CREATE TABLE IF NOT EXISTS custom_list_movies (" +
                "list_id INT NOT NULL, " +
                "tmdb_id INT NOT NULL, " +
                "added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "PRIMARY KEY (list_id, tmdb_id), " +
                "CONSTRAINT fk_custom_list_movies_list " +
                "FOREIGN KEY (list_id) REFERENCES custom_lists(id) " +
                "ON DELETE CASCADE, " +
                "CONSTRAINT fk_custom_list_movies_movie " +
                "FOREIGN KEY (tmdb_id) REFERENCES movies_new(tmdb_id) " +
                "ON DELETE CASCADE)";

        try (Connection con = DatabaseManager.getConnection();
             Statement statement = con.createStatement()) {
            statement.executeUpdate(createLists);
            statement.executeUpdate(createItems);
        } catch (SQLException ex) {
            showError("Could not prepare the Lists database tables.", ex);
        }
    }

    private void showOverview() {
        currentListId = -1;
        currentListName = "";
        pageTitle.setText("My Lists");
        backButton.setText("← Back");
        actionButton.setText("+ Create List");
        bodyLayout.show(bodyPanel, "overview");
        refreshLists();
    }

    private void refreshLists() {
        listPanel.removeAll();

        String sql =
                "SELECT l.id, l.name, COUNT(lm.tmdb_id) AS movie_count " +
                "FROM custom_lists l " +
                "LEFT JOIN custom_list_movies lm ON lm.list_id = l.id " +
                "GROUP BY l.id, l.name " +
                "ORDER BY l.id DESC";

        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            boolean found = false;
            while (rs.next()) {
                found = true;
                int id = rs.getInt("id");
                String name = rs.getString("name");
                int count = rs.getInt("movie_count");
                listPanel.add(makeListCard(id, name, count));
                listPanel.add(Box.createVerticalStrut(10));
            }

            if (!found) {
                JLabel empty = new JLabel(
                        "No lists yet. Click '+ Create List' to make your first playlist.");
                empty.setForeground(TEXT);
                empty.setFont(new Font("SansSerif", Font.PLAIN, 15));
                empty.setAlignmentX(Component.LEFT_ALIGNMENT);
                listPanel.add(Box.createVerticalStrut(18));
                listPanel.add(empty);
            }

        } catch (SQLException ex) {
            showError("Could not load your lists.", ex);
        }

        listPanel.revalidate();
        listPanel.repaint();
    }

    private JPanel makeListCard(int listId, String name, int movieCount) {
        JPanel card = new JPanel(new BorderLayout(12, 0));
        card.setBackground(CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(65, 58, 58)),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 82));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel labels = new JPanel();
        labels.setOpaque(false);
        labels.setLayout(new BoxLayout(labels, BoxLayout.Y_AXIS));

        JLabel nameLabel = new JLabel(name);
        nameLabel.setForeground(GOLD);
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 18));

        JLabel countLabel = new JLabel(
                movieCount + (movieCount == 1 ? " movie" : " movies"));
        countLabel.setForeground(TEXT);
        countLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));

        labels.add(nameLabel);
        labels.add(Box.createVerticalStrut(5));
        labels.add(countLabel);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 7, 5));
        buttons.setOpaque(false);

        JButton open = new JButton("Open");
        styleButton(open);
        open.addActionListener(e -> openList(listId, name));

        JButton delete = new JButton("Delete");
        styleButton(delete);
        delete.addActionListener(e -> deleteList(listId, name));

        buttons.add(open);
        buttons.add(delete);

        card.add(labels, BorderLayout.CENTER);
        card.add(buttons, BorderLayout.EAST);
        return card;
    }

    private void createList() {
        String name = JOptionPane.showInputDialog(
                this,
                "Enter a name for your movie list:",
                "Create List",
                JOptionPane.PLAIN_MESSAGE);

        if (name == null) {
            return;
        }

        name = name.trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "List name cannot be empty.");
            return;
        }
        if (name.length() > 100) {
            JOptionPane.showMessageDialog(this,
                    "Please keep the list name under 100 characters.");
            return;
        }

        String sql = "INSERT INTO custom_lists (name) VALUES (?)";
        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.executeUpdate();
            refreshLists();
        } catch (SQLException ex) {
            showError("Could not create the list.", ex);
        }
    }

    private void deleteList(int listId, String name) {
        int answer = JOptionPane.showConfirmDialog(
                this,
                "Delete the list '" + name + "' and all movies inside it?",
                "Delete List",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (answer != JOptionPane.YES_OPTION) {
            return;
        }

        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "DELETE FROM custom_lists WHERE id = ?")) {
            ps.setInt(1, listId);
            ps.executeUpdate();
            refreshLists();
        } catch (SQLException ex) {
            showError("Could not delete the list.", ex);
        }
    }

    private void openList(int listId, String name) {
        currentListId = listId;
        currentListName = name;
        pageTitle.setText(name);
        backButton.setText("← Lists");
        actionButton.setText("+ Add Movie");
        bodyLayout.show(bodyPanel, "movies");
        refreshMovies();
    }

    private void refreshMovies() {
        moviePanel.removeAll();

        String sql =
                "SELECT m.tmdb_id, m.title, m.poster_path " +
                "FROM custom_list_movies lm " +
                "JOIN movies_new m ON m.tmdb_id = lm.tmdb_id " +
                "WHERE lm.list_id = ? " +
                "ORDER BY lm.added_at DESC, m.title";

        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, currentListId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Movie movie = new Movie(
                            rs.getInt("tmdb_id"),
                            rs.getString("title"),
                            rs.getString("poster_path"));
                    moviePanel.add(makeMovieCard(movie));
                }
            }

            if (moviePanel.getComponentCount() == 0) {
                JLabel empty = new JLabel(
                        "This list is empty. Click '+ Add Movie' to get started.",
                        SwingConstants.CENTER);
                empty.setForeground(TEXT);
                empty.setFont(new Font("SansSerif", Font.PLAIN, 15));
                moviePanel.add(empty);
            }

        } catch (SQLException ex) {
            showError("Could not load movies in this list.", ex);
        }

        moviePanel.revalidate();
        moviePanel.repaint();
    }

    private JPanel makeMovieCard(Movie movie) {
        JPanel card = new JPanel(new BorderLayout(6, 8));
        card.setBackground(CARD);
        card.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel poster = new JLabel("Loading poster...", SwingConstants.CENTER);
        poster.setForeground(Color.LIGHT_GRAY);
        poster.setPreferredSize(new Dimension(150, 205));
        card.add(poster, BorderLayout.NORTH);

        JLabel title = new JLabel(
                "<html><div style='text-align:center;width:130px'>" +
                escapeHtml(movie.title) + "</div></html>",
                SwingConstants.CENTER);
        title.setForeground(TEXT);
        title.setFont(new Font("SansSerif", Font.BOLD, 13));
        title.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        title.setToolTipText("Click to view movie details");
        title.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                main.showMovieDetails(movie.id);
            }
        });
        card.add(title, BorderLayout.CENTER);

        JButton remove = new JButton("Remove from List");
        styleButton(remove);
        remove.addActionListener(e -> removeMovieFromList(movie));
        card.add(remove, BorderLayout.SOUTH);

        new SwingWorker<ImageIcon, Void>() {
            @Override
            protected ImageIcon doInBackground() {
                return loadPoster(movie.posterPath, 150, 205);
            }

            @Override
            protected void done() {
                try {
                    ImageIcon icon = get();
                    if (icon == null) {
                        poster.setText("No poster available");
                    } else {
                        poster.setText("");
                        poster.setIcon(icon);
                    }
                } catch (Exception ex) {
                    poster.setText("Poster unavailable");
                }
            }
        }.execute();

        return card;
    }

    private void openAddMovieDialog(int listId) {
        JDialog dialog = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                "Add Movie to " + currentListName,
                Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(570, 560);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(BG);
        dialog.setLayout(new BorderLayout(10, 10));

        JTextField search = new JTextField();
        search.setFont(new Font("SansSerif", Font.PLAIN, 16));
        search.setToolTipText("Type a movie title to filter");
        search.setBorder(BorderFactory.createTitledBorder("Search movies"));

        DefaultListModel<Movie> model = new DefaultListModel<>();
        JList<Movie> choices = new JList<>(model);
        choices.setBackground(CARD);
        choices.setForeground(TEXT);
        choices.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        choices.setFixedCellHeight(45);
        choices.setCellRenderer(new MovieRenderer());

        JLabel status = new JLabel("Loading movies...");
        status.setForeground(TEXT);

        JButton addSelected = new JButton("Add Selected Movie");
        styleButton(addSelected);

        dialog.add(search, BorderLayout.NORTH);
        dialog.add(new JScrollPane(choices), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(8, 8));
        bottom.setOpaque(false);
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        bottom.add(status, BorderLayout.CENTER);
        bottom.add(addSelected, BorderLayout.EAST);
        dialog.add(bottom, BorderLayout.SOUTH);

        List<Movie> allMovies = new ArrayList<>();

        new SwingWorker<List<Movie>, Void>() {
            @Override
            protected List<Movie> doInBackground() throws Exception {
                List<Movie> result = new ArrayList<>();
                String sql =
                        "SELECT m.tmdb_id, m.title, m.poster_path " +
                        "FROM movies_new m " +
                        "WHERE NOT EXISTS (" +
                        "SELECT 1 FROM custom_list_movies lm " +
                        "WHERE lm.tmdb_id = m.tmdb_id AND lm.list_id = ?) " +
                        "ORDER BY m.title";

                try (Connection con = DatabaseManager.getConnection();
                     PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, listId);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            result.add(new Movie(
                                    rs.getInt("tmdb_id"),
                                    rs.getString("title"),
                                    rs.getString("poster_path")));
                        }
                    }
                }
                return result;
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

            @Override
            public void insertUpdate(DocumentEvent e) {
                changed();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                changed();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                changed();
            }
        });

        addSelected.addActionListener(e -> {
            Movie selected = choices.getSelectedValue();
            if (selected == null) {
                JOptionPane.showMessageDialog(dialog, "Please select a movie first.");
                return;
            }

            if (saveMovieToList(listId, selected.id)) {
                dialog.dispose();
                refreshMovies();
            }
        });

        dialog.setVisible(true);
    }

    private void filterMovies(
            String query,
            List<Movie> movies,
            DefaultListModel<Movie> model) {
        String q = query == null ? "" : query.trim().toLowerCase();
        model.clear();
        for (Movie movie : movies) {
            if (movie.title.toLowerCase().contains(q)) {
                model.addElement(movie);
            }
        }
    }

    private class MovieRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(
                JList<?> list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);
            Movie movie = (Movie) value;
            label.setText(movie.title);
            label.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
            return label;
        }
    }

    private boolean saveMovieToList(int listId, int movieId) {
        String sql =
                "INSERT IGNORE INTO custom_list_movies (list_id, tmdb_id) " +
                "VALUES (?, ?)";

        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, listId);
            ps.setInt(2, movieId);
            int inserted = ps.executeUpdate();
            if (inserted == 0) {
                JOptionPane.showMessageDialog(this,
                        "This movie is already in the list.");
                return false;
            }
            return true;
        } catch (SQLException ex) {
            showError("Could not add the movie to this list.", ex);
            return false;
        }
    }

    private void removeMovieFromList(Movie movie) {
        int answer = JOptionPane.showConfirmDialog(
                this,
                "Remove '" + movie.title + "' from '" + currentListName + "'?",
                "Remove Movie",
                JOptionPane.YES_NO_OPTION);

        if (answer != JOptionPane.YES_OPTION) {
            return;
        }

        String sql =
                "DELETE FROM custom_list_movies WHERE list_id = ? AND tmdb_id = ?";
        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, currentListId);
            ps.setInt(2, movie.id);
            ps.executeUpdate();
            refreshMovies();
        } catch (SQLException ex) {
            showError("Could not remove the movie from this list.", ex);
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

        String cacheKey = url + "_" + width + "x" + height;
        ImageIcon cached = posterCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        try {
            ImageIcon original = new ImageIcon(new URL(url));
            if (original.getIconWidth() <= 0) {
                return null;
            }
            Image scaled = original.getImage().getScaledInstance(
                    width, height, Image.SCALE_SMOOTH);
            ImageIcon result = new ImageIcon(scaled);
            posterCache.put(cacheKey, result);
            return result;
        } catch (Exception ex) {
            return null;
        }
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

    private void showError(String message, Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(
                this,
                message + "\n" + ex.getMessage(),
                "Lists Error",
                JOptionPane.ERROR_MESSAGE);
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
