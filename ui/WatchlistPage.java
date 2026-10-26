
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.io.File;
import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class WatchlistPage extends JPanel {

    Main main;
    JPanel moviePanel;

    Color BG = Color.decode("#14141C");
    Color CARD = Color.decode("#23232E");
    Color TEXT = Color.WHITE;
    Color ACCENT = Color.decode("#A64B52");
    Color GOLD = Color.decode("#D6A36A");

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

        JLabel title = new JLabel("My Watchlist", JLabel.CENTER);
        title.setForeground(TEXT);
        title.setFont(new Font("Arial", Font.BOLD, 25));

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

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                refresh();
            }
        });
    }

    void styleButton(JButton button) {
        button.setFocusPainted(false);
        button.setForeground(Color.WHITE);
        button.setBackground(ACCENT);
        button.setBorder(
            BorderFactory.createEmptyBorder(8, 13, 8, 13)
        );
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    // Each account gets its own watchlist rows.
    void ensureWatchlistTable() {
        String sql =
            "CREATE TABLE IF NOT EXISTS watchlist_items_new (" +
            "user_id INT NOT NULL, " +
            "tmdb_id INT NOT NULL, " +
            "status VARCHAR(20) NOT NULL DEFAULT 'WANTED', " +
            "PRIMARY KEY (user_id, tmdb_id), " +
            "FOREIGN KEY (tmdb_id) REFERENCES movies_new(tmdb_id) " +
            "ON DELETE CASCADE, " +
            "FOREIGN KEY (user_id) REFERENCES mm70_users.users(user_id) " +
            "ON DELETE CASCADE)";

        try (Connection con = DatabaseManager.getConnection();
             Statement st = con.createStatement()) {

            st.executeUpdate(sql);

        } catch (Exception ex) {
            showError("Could not prepare the watchlist table.", ex);
        }
    }

    void refresh() {
        moviePanel.removeAll();

        String sql =
            "SELECT m.tmdb_id, m.title, w.status " +
            "FROM watchlist_items_new w " +
            "JOIN movies_new m ON m.tmdb_id = w.tmdb_id " +
            "JOIN mm70_users.users u ON u.user_id = w.user_id " +
            "WHERE u.username = ? AND w.status <> 'WATCHED' " +
            "ORDER BY m.title";

        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, main.getUsername());

            try (ResultSet rs = ps.executeQuery()) {
                int count = 0;

                while (rs.next()) {
                    moviePanel.add(movieCard(
                        rs.getInt("tmdb_id"),
                        rs.getString("title")
                    ));
                    count++;
                }

                if (count == 0) {
                    JLabel empty = new JLabel(
                        "Your watchlist is empty. Add a movie!",
                        SwingConstants.CENTER
                    );
                    empty.setForeground(TEXT);
                    moviePanel.add(empty);
                }
            }

        } catch (Exception ex) {
            showError("Could not load your watchlist.", ex);
        }

        moviePanel.revalidate();
        moviePanel.repaint();
    }

    JPanel movieCard(int id, String title) {
        JPanel card = new JPanel(new BorderLayout(6, 8));
        card.setBackground(CARD);
        card.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel poster = new JLabel("", SwingConstants.CENTER);
        poster.setPreferredSize(new Dimension(150, 205));

        File file = new File("posters/" + id + ".jpg");

        if (file.exists()) {
            ImageIcon icon = new ImageIcon(file.getAbsolutePath());
            Image image = icon.getImage().getScaledInstance(
                150, 205, Image.SCALE_SMOOTH
            );
            poster.setIcon(new ImageIcon(image));
        } else {
            poster.setText("No poster");
            poster.setForeground(TEXT);
        }

        card.add(poster, BorderLayout.NORTH);

        JLabel name = new JLabel(
            "<html><div style='text-align:center;width:130px'>" +
            escapeHtml(title) + "</div></html>",
            SwingConstants.CENTER
        );
        name.setForeground(GOLD);
        name.setFont(new Font("Arial", Font.BOLD, 13));
        name.setCursor(new Cursor(Cursor.HAND_CURSOR));
        name.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                main.showMovieDetails(id);
            }
        });

        card.add(name, BorderLayout.CENTER);

        JPanel actions = new JPanel(new GridLayout(0, 1, 4, 4));
        actions.setOpaque(false);

        JButton watched = new JButton("Mark Watched");
        styleButton(watched);
        watched.addActionListener(e -> markWatched(id));
        actions.add(watched);

        JButton remove = new JButton("Remove");
        styleButton(remove);
        remove.addActionListener(e -> removeMovie(id));
        actions.add(remove);

        card.add(actions, BorderLayout.SOUTH);
        return card;
    }

    // Add a movie for the currently logged-in account.
    boolean insertWanted(int movieId) {
        String sql =
            "INSERT INTO watchlist_items_new (user_id, tmdb_id, status) " +
            "SELECT u.user_id, ?, 'WANTED' " +
            "FROM mm70_users.users u WHERE u.username = ? " +
            "ON DUPLICATE KEY UPDATE status = " +
            "IF(status = 'WATCHED', status, 'WANTED')";

        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, movieId);
            ps.setString(2, main.getUsername());

            int result = ps.executeUpdate();

            if (result == 0) {
                JOptionPane.showMessageDialog(
                    this, "Your account was not found. Please log in again."
                );
                return false;
            }

            return true;

        } catch (Exception ex) {
            showError("Could not add movie to your watchlist.", ex);
            return false;
        }
    }

    void removeMovie(int movieId) {
        int answer = JOptionPane.showConfirmDialog(
            this,
            "Remove this movie from your watchlist?",
            "Confirm removal",
            JOptionPane.YES_NO_OPTION
        );

        if (answer != JOptionPane.YES_OPTION) return;

        String sql =
            "DELETE w FROM watchlist_items_new w " +
            "JOIN mm70_users.users u ON u.user_id = w.user_id " +
            "WHERE w.tmdb_id = ? AND u.username = ?";

        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, movieId);
            ps.setString(2, main.getUsername());
            ps.executeUpdate();
            refresh();

        } catch (Exception ex) {
            showError("Could not remove movie.", ex);
        }
    }

    void openAddMovieDialog() {
        JDialog dialog = new JDialog(
            SwingUtilities.getWindowAncestor(this),
            "Add Movie to Watchlist",
            Dialog.ModalityType.APPLICATION_MODAL
        );

        dialog.setSize(500, 450);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JTextField search = new JTextField();
        search.setBorder(
            BorderFactory.createTitledBorder("Search movie")
        );

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
        ArrayList<Integer> existingIds = new ArrayList<>();

        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(
                "SELECT tmdb_id, title FROM movies_new ORDER BY title");
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                allMovies.add(new Movie(
                    rs.getInt("tmdb_id"),
                    rs.getString("title")
                ));
            }

        } catch (Exception ex) {
            showError("Could not load movies.", ex);
            dialog.dispose();
            return;
        }

        String existingSql =
            "SELECT w.tmdb_id FROM watchlist_items_new w " +
            "JOIN mm70_users.users u ON u.user_id = w.user_id " +
            "WHERE u.username = ?";

        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(existingSql)) {

            ps.setString(1, main.getUsername());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    existingIds.add(rs.getInt("tmdb_id"));
                }
            }

        } catch (Exception ex) {
            showError("Could not check your watchlist.", ex);
            dialog.dispose();
            return;
        }

        status.setText(allMovies.size() + " movies in database");

        search.getDocument().addDocumentListener(new DocumentListener() {
            void filter() {
                model.clear();

                String text = search.getText().trim().toLowerCase();

                if (text.isEmpty()) {
                    status.setText("Type to search movies");
                    return;
                }

                int matches = 0;
                int alreadyAdded = 0;

                for (Movie movie : allMovies) {
                    if (movie.title.toLowerCase().contains(text)) {
                        matches++;

                        if (existingIds.contains(movie.id)) {
                            alreadyAdded++;
                        } else {
                            model.addElement(movie);
                        }
                    }
                }

                if (matches == 0) {
                    status.setText("Movie not found!");
                } else if (model.isEmpty() && alreadyAdded > 0) {
                    status.setText("Movie already in your watchlist!");
                } else {
                    status.setText(model.size() + " matching movie(s)");
                }
            }

            public void insertUpdate(DocumentEvent e) { filter(); }
            public void removeUpdate(DocumentEvent e) { filter(); }
            public void changedUpdate(DocumentEvent e) { filter(); }
        });

        addSelected.addActionListener(e -> {
            Movie selected = list.getSelectedValue();

            if (selected == null) {
                JOptionPane.showMessageDialog(
                    dialog, "Please select a movie first."
                );
                return;
            }

            if (insertWanted(selected.id)) {
                dialog.dispose();
                refresh();
            }
        });

        dialog.setVisible(true);
    }

    void markWatched(int movieId) {
        JTextField ratingField = new JTextField(10);

        JTextField dateField = new JTextField(10);
        dateField.setEditable(false);

        JButton calendarButton = new JButton("Calendar");
        calendarButton.addActionListener(e -> showCalendar(dateField));

        JPanel datePanel = new JPanel(new BorderLayout(5, 0));
        datePanel.add(dateField, BorderLayout.CENTER);
        datePanel.add(calendarButton, BorderLayout.EAST);

        JTextArea reviewArea = new JTextArea(5, 22);
        reviewArea.setLineWrap(true);
        reviewArea.setWrapStyleWord(true);

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel("Rating (0-10)"));
        form.add(ratingField);
        form.add(new JLabel("Date watched"));
        form.add(datePanel);
        form.add(new JLabel("Review"));
        form.add(new JScrollPane(reviewArea));

        int result = JOptionPane.showConfirmDialog(
            this, form, "Mark Movie as Watched",
            JOptionPane.OK_CANCEL_OPTION
        );

        if (result != JOptionPane.OK_OPTION) return;

        double rating;

        try {
            rating = Double.parseDouble(ratingField.getText().trim());

            if (rating < 0 || rating > 10) {
                JOptionPane.showMessageDialog(
                    this, "Rating must be between 0 and 10."
                );
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter a valid rating.");
            return;
        }

        String date = dateField.getText().trim();

        if (date.isEmpty()) {
            JOptionPane.showMessageDialog(
                this, "Please select the date watched."
            );
            return;
        }

        Connection con = null;

        try {
            con = DatabaseManager.getConnection();
            con.setAutoCommit(false);

            String insert =
                "INSERT INTO diary_entries " +
                "(tmdb_id, rating, watched_date, review, user_id) " +
                "SELECT ?, ?, ?, ?, u.user_id " +
                "FROM mm70_users.users u WHERE u.username = ?";

            int inserted;

            try (PreparedStatement ps = con.prepareStatement(insert)) {
                ps.setInt(1, movieId);
                ps.setDouble(2, rating);
                ps.setString(3, date);
                ps.setString(4, reviewArea.getText().trim());
                ps.setString(5, main.getUsername());
                inserted = ps.executeUpdate();
            }

            if (inserted == 0) {
                throw new SQLException("Logged-in account was not found.");
            }

            String delete =
                "DELETE w FROM watchlist_items_new w " +
                "JOIN mm70_users.users u ON u.user_id = w.user_id " +
                "WHERE w.tmdb_id = ? AND u.username = ?";

            try (PreparedStatement ps = con.prepareStatement(delete)) {
                ps.setInt(1, movieId);
                ps.setString(2, main.getUsername());
                ps.executeUpdate();
            }

            con.commit();
            main.showPage("diary");

            JOptionPane.showMessageDialog(
                this, "Movie moved to your Diary!"
            );

        } catch (Exception ex) {
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException ignored) {}
            }

            showError("Could not mark movie as watched.", ex);

        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    void showCalendar(JTextField dateField) {
        JDialog calendar = new JDialog(
            SwingUtilities.getWindowAncestor(this),
            "Select Date Watched",
            Dialog.ModalityType.APPLICATION_MODAL
        );

        calendar.setSize(420, 370);
        calendar.setLocationRelativeTo(this);
        calendar.setLayout(new BorderLayout(5, 5));

        JPanel top = new JPanel(new BorderLayout(5, 5));
        JButton previous = new JButton("←");
        JButton next = new JButton("→");
        JLabel monthLabel = new JLabel("", JLabel.CENTER);
        monthLabel.setForeground(GOLD);
        monthLabel.setFont(new Font("Arial", Font.BOLD, 15));

        top.add(previous, BorderLayout.WEST);
        top.add(monthLabel, BorderLayout.CENTER);
        top.add(next, BorderLayout.EAST);
        calendar.add(top, BorderLayout.NORTH);

        // Seven weekday columns and six possible weeks, plus the header row.
        JPanel daysPanel = new JPanel(new GridLayout(7, 7, 3, 3));
        daysPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        calendar.add(daysPanel, BorderLayout.CENTER);

        YearMonth[] month = {YearMonth.now()};

        Runnable update = () -> {
            monthLabel.setText(
                month[0].getMonth().toString() + " " + month[0].getYear()
            );
            daysPanel.removeAll();

            String[] weekdays = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
            for (String day : weekdays) {
                JLabel label = new JLabel(day, JLabel.CENTER);
                label.setFont(new Font("Arial", Font.BOLD, 12));
                daysPanel.add(label);
            }

            int firstDay = month[0].atDay(1).getDayOfWeek().getValue();
            int daysInMonth = month[0].lengthOfMonth();

            // Fill 42 date cells so every month has a stable calendar layout.
            for (int cell = 0; cell < 42; cell++) {
                int dayNumber = cell - (firstDay - 1) + 1;

                if (dayNumber < 1 || dayNumber > daysInMonth) {
                    daysPanel.add(new JLabel(""));
                    continue;
                }

                final int selectedDay = dayNumber;
                JButton button = new JButton(String.valueOf(selectedDay));
                button.setBackground(ACCENT);
                button.setForeground(Color.WHITE);
                button.setFocusPainted(false);
                button.setFont(new Font("Arial", Font.BOLD, 12));
                button.setMargin(new Insets(2, 2, 2, 2));
                button.setCursor(new Cursor(Cursor.HAND_CURSOR));

                button.addActionListener(e -> {
                    LocalDate selectedDate = month[0].atDay(selectedDay);
                    dateField.setText(
                        selectedDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                    );
                    calendar.dispose();
                });

                daysPanel.add(button);
            }

            daysPanel.revalidate();
            daysPanel.repaint();
        };

        previous.addActionListener(e -> {
            month[0] = month[0].minusMonths(1);
            update.run();
        });

        next.addActionListener(e -> {
            month[0] = month[0].plusMonths(1);
            update.run();
        });

        update.run();
        calendar.setVisible(true);
    }

    String escapeHtml(String value) {
        if (value == null) return "";

        return value.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;");
    }

    void showError(String message, Exception ex) {
        ex.printStackTrace();

        JOptionPane.showMessageDialog(
            this,
            message + "\n" + ex.getMessage(),
            "Watchlist Error",
            JOptionPane.ERROR_MESSAGE
        );
    }

    static class Movie {
        int id;
        String title;

        Movie(int id, String title) {
            this.id = id;
            this.title = title == null ? "Untitled movie" : title;
        }

        @Override
        public String toString() {
            return title;
        }
    }
}
