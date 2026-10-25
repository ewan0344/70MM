
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.io.File;
import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class DiaryPage extends JPanel {

    private final Main main;
    private JPanel diaryPanel;

    private final Color BG = Color.decode("#171415");
    private final Color CARD = Color.decode("#241E20");
    private final Color GOLD = Color.decode("#D6A36A");
    private final Color RED = Color.decode("#A64B52");
    private final Color TEXT = Color.decode("#F1E9DD");

    public DiaryPage(Main main) {
        this.main = main;

        createUI();
        createDiaryTable();
        refreshDiary();

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                refreshDiary();
            }
        });
    }

    private void createUI() {
        setLayout(new BorderLayout());
        setBackground(BG);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(BG);

        JButton backButton = new JButton("←");
        backButton.setForeground(GOLD);
        backButton.setBackground(RED);
        backButton.setFocusPainted(false);
        backButton.addActionListener(e -> main.showPage("home"));

        JLabel label = new JLabel("Diary", SwingConstants.CENTER);
        label.setForeground(GOLD);
        label.setFont(new Font("Arial", Font.BOLD, 30));

        topPanel.add(backButton, BorderLayout.WEST);
        topPanel.add(label, BorderLayout.CENTER);

        panel.add(topPanel, BorderLayout.NORTH);

        diaryPanel = new JPanel();
        diaryPanel.setLayout(new BoxLayout(diaryPanel, BoxLayout.Y_AXIS));
        diaryPanel.setBackground(BG);
        diaryPanel.setBorder(
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        );

        JScrollPane scroll = new JScrollPane(diaryPanel);
        scroll.getViewport().setBackground(BG);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        panel.add(scroll, BorderLayout.CENTER);

        JButton addMovie = new JButton("Add movie");
        addMovie.setForeground(TEXT);
        addMovie.setBackground(RED);
        addMovie.setFocusPainted(false);
        addMovie.addActionListener(e -> addMovieDialog());

        JPanel bottom = new JPanel();
        bottom.setBackground(BG);
        bottom.add(addMovie);

        panel.add(bottom, BorderLayout.SOUTH);

        add(panel, BorderLayout.CENTER);
    }

    private void createDiaryTable() {
        String sql =
            "CREATE TABLE IF NOT EXISTS diary_entries (" +
            "entry_id INT AUTO_INCREMENT PRIMARY KEY, " +
            "tmdb_id INT NOT NULL, " +
            "rating DECIMAL(3,1) NOT NULL, " +
            "watched_date VARCHAR(20) NOT NULL, " +
            "review TEXT, " +
            "user_id INT NULL, " +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            "FOREIGN KEY (tmdb_id) REFERENCES movies_new(tmdb_id) " +
            "ON DELETE CASCADE)";

        try (Connection con = DatabaseManager.getConnection();
             Statement st = con.createStatement()) {

            st.executeUpdate(sql);

            boolean hasUserId = false;

            try (ResultSet columns = con.getMetaData().getColumns(
                    con.getCatalog(), null, "diary_entries", "user_id")) {
                hasUserId = columns.next();
            }

            if (!hasUserId) {
                st.executeUpdate(
                    "ALTER TABLE diary_entries ADD COLUMN user_id INT NULL"
                );
            }

        } catch (Exception ex) {
            showError("Could not create diary table.", ex);
        }
    }

    private void refreshDiary() {
        diaryPanel.removeAll();

        String sql =
            "SELECT d.entry_id, m.tmdb_id, m.title, " +
            "d.rating, d.watched_date, d.review " +
            "FROM diary_entries d " +
            "JOIN movies_new m ON d.tmdb_id = m.tmdb_id " +
            "JOIN mm70_users.users u ON u.user_id = d.user_id " +
            "WHERE u.username = ? " +
            "ORDER BY d.entry_id DESC";

        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, main.getUsername());

            try (ResultSet rs = ps.executeQuery()) {
                int count = 0;

                while (rs.next()) {
                    addToDiary(
                        rs.getInt("entry_id"),
                        rs.getInt("tmdb_id"),
                        rs.getString("title"),
                        rs.getString("rating"),
                        rs.getString("watched_date"),
                        rs.getString("review")
                    );
                    count++;
                }

                if (count == 0) {
                    JLabel empty = new JLabel(
                        "No diary movies yet. Click Add movie to begin.",
                        SwingConstants.CENTER
                    );
                    empty.setForeground(TEXT);
                    empty.setAlignmentX(Component.CENTER_ALIGNMENT);

                    diaryPanel.add(Box.createVerticalStrut(20));
                    diaryPanel.add(empty);
                }
            }

        } catch (Exception ex) {
            showError("Could not load diary.", ex);
        }

        diaryPanel.revalidate();
        diaryPanel.repaint();
    }

    private void addMovieDialog() {
        JPanel moviePanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        JTextField nameField = new JTextField(22);
        JTextField ratingField = new JTextField(18);
        JTextField dateField = new JTextField(12);

        JTextArea reviewArea = new JTextArea(5, 22);
        reviewArea.setLineWrap(true);
        reviewArea.setWrapStyleWord(true);
        reviewArea.setFont(new Font("Arial", Font.PLAIN, 14));

        JScrollPane reviewScroll = new JScrollPane(reviewArea);
        reviewScroll.setPreferredSize(new Dimension(280, 100));

        dateField.setEditable(false);
        dateField.setBackground(Color.WHITE);

        JButton calendarButton = new JButton("Calendar");
        calendarButton.addActionListener(e -> showCalendar(dateField));

        JPanel datePanel = new JPanel(new BorderLayout(5, 0));
        datePanel.add(dateField, BorderLayout.CENTER);
        datePanel.add(calendarButton, BorderLayout.EAST);

        DefaultListModel<Movie> suggestionModel = new DefaultListModel<>();
        JList<Movie> suggestionList = new JList<>(suggestionModel);
        suggestionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        suggestionList.setVisibleRowCount(5);
        suggestionList.setFixedCellHeight(24);

        JScrollPane suggestionScroll = new JScrollPane(suggestionList);
        suggestionScroll.setPreferredSize(new Dimension(280, 120));
        suggestionScroll.setVisible(false);

        JLabel searchStatus = new JLabel("Type to search movies");
        searchStatus.setForeground(Color.DARK_GRAY);

        JPanel namePanel = new JPanel();
        namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.Y_AXIS));
        namePanel.add(nameField);
        namePanel.add(suggestionScroll);
        namePanel.add(searchStatus);

        int[] selectedMovieId = {-1};
        boolean[] selecting = {false};

        ArrayList<Movie> allMovies = new ArrayList<>();

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
            showError("Could not load movies from database.", ex);
            return;
        }

        nameField.getDocument().addDocumentListener(new DocumentListener() {

            private void filterMovies() {
                if (selecting[0]) return;

                selectedMovieId[0] = -1;
                suggestionList.clearSelection();
                suggestionModel.clear();

                String text = nameField.getText().trim().toLowerCase();

                if (text.isEmpty()) {
                    suggestionScroll.setVisible(false);
                    searchStatus.setText("Type to search movies");
                    namePanel.revalidate();
                    namePanel.repaint();
                    return;
                }

                for (Movie movie : allMovies) {
                    if (movie.title.toLowerCase().contains(text)) {
                        suggestionModel.addElement(movie);

                        if (suggestionModel.getSize() >= 30) {
                            break;
                        }
                    }
                }

                if (suggestionModel.isEmpty()) {
                    suggestionScroll.setVisible(false);
                    searchStatus.setText("Movie not found!");
                    searchStatus.setForeground(RED);
                } else {
                    suggestionScroll.setVisible(true);
                    searchStatus.setText(
                        suggestionModel.getSize() + " matching movies found"
                    );
                    searchStatus.setForeground(Color.DARK_GRAY);
                }

                namePanel.revalidate();
                namePanel.repaint();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                filterMovies();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterMovies();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterMovies();
            }
        });

        suggestionList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;

            Movie selected = suggestionList.getSelectedValue();

            if (selected != null) {
                selecting[0] = true;
                selectedMovieId[0] = selected.id;
                nameField.setText(selected.title);
                suggestionScroll.setVisible(false);
                searchStatus.setText("Selected: " + selected.title);
                searchStatus.setForeground(GOLD);
                selecting[0] = false;

                namePanel.revalidate();
                namePanel.repaint();
            }
        });

        addField(moviePanel, gbc, 0, "Movie name", namePanel);
        addField(moviePanel, gbc, 1, "Rating (0-10)", ratingField);
        addField(moviePanel, gbc, 2, "Date watched", datePanel);
        addField(moviePanel, gbc, 3, "Review", reviewScroll);

        JOptionPane optionPane = new JOptionPane(
            moviePanel,
            JOptionPane.PLAIN_MESSAGE,
            JOptionPane.OK_CANCEL_OPTION
        );

        JDialog dialog = optionPane.createDialog(this, "Add Movie");
        dialog.setResizable(true);
        dialog.setSize(520, 470);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

        Object choice = optionPane.getValue();

        if (!(choice instanceof Integer)
                || (Integer) choice != JOptionPane.OK_OPTION) {
            return;
        }

        if (selectedMovieId[0] == -1) {
            JOptionPane.showMessageDialog(
                this,
                "Select a movie from the suggestions."
            );
            return;
        }

        String rating = ratingField.getText().trim();
        String date = dateField.getText().trim();
        String review = reviewArea.getText().trim();

        if (rating.isEmpty() || date.isEmpty()) {
            JOptionPane.showMessageDialog(
                this, "Enter a rating and select a date."
            );
            return;
        }

        double value;

        try {
            value = Double.parseDouble(rating);

            if (value < 0 || value > 10) {
                JOptionPane.showMessageDialog(
                    this, "Rating must be between 0 and 10."
                );
                return;
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(
                this, "Enter a valid rating."
            );
            return;
        }

        String sql =
            "INSERT INTO diary_entries " +
            "(tmdb_id, rating, watched_date, review, user_id) " +
            "SELECT ?, ?, ?, ?, u.user_id " +
            "FROM mm70_users.users u WHERE u.username = ?";

        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, selectedMovieId[0]);
            ps.setDouble(2, value);
            ps.setString(3, date);
            ps.setString(4, review);
            ps.setString(5, main.getUsername());

            int inserted = ps.executeUpdate();

            if (inserted == 0) {
                JOptionPane.showMessageDialog(
                    this,
                    "Your account was not found. Please log in again."
                );
                return;
            }

            refreshDiary();

        } catch (Exception ex) {
            showError("Could not add movie.", ex);
        }
    }

    private void addField(JPanel panel, GridBagConstraints gbc,
                          int row, String label, JComponent field) {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;

        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;

        if (row == 3) {
            gbc.weighty = 1;
            gbc.fill = GridBagConstraints.BOTH;
        } else {
            gbc.weighty = 0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
        }

        panel.add(field, gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL;
    }

    private void showCalendar(JTextField dateField) {
        Window owner = SwingUtilities.getWindowAncestor(this);

        JDialog calendar = new JDialog(
            owner instanceof Frame ? (Frame) owner : null,
            "Select Date Watched",
            true
        );

        calendar.setSize(390, 400);
        calendar.setLocationRelativeTo(this);
        calendar.setLayout(new BorderLayout(5, 5));

        JPanel top = new JPanel(new BorderLayout(5, 5));
        top.setBackground(BG);

        JButton previous = new JButton("←");
        JButton next = new JButton("→");

        JLabel monthLabel = new JLabel("", JLabel.CENTER);
        monthLabel.setForeground(GOLD);
        monthLabel.setFont(new Font("Arial", Font.BOLD, 17));

        JButton yearButton = new JButton("Year ▾");
        yearButton.setForeground(GOLD);
        yearButton.setBackground(CARD);

        JPanel center = new JPanel(new BorderLayout(5, 0));
        center.setBackground(BG);
        center.add(monthLabel, BorderLayout.CENTER);
        center.add(yearButton, BorderLayout.EAST);

        top.add(previous, BorderLayout.WEST);
        top.add(center, BorderLayout.CENTER);
        top.add(next, BorderLayout.EAST);

        JPanel daysPanel = new JPanel(new GridLayout(7, 7, 3, 3));
        daysPanel.setBackground(BG);
        daysPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        calendar.add(top, BorderLayout.NORTH);
        calendar.add(daysPanel, BorderLayout.CENTER);

        YearMonth[] currentMonth = {YearMonth.now()};

        Runnable update = () -> updateCalendar(
            currentMonth[0], monthLabel, daysPanel, dateField, calendar
        );

        previous.addActionListener(e -> {
            currentMonth[0] = currentMonth[0].minusMonths(1);
            update.run();
        });

        next.addActionListener(e -> {
            currentMonth[0] = currentMonth[0].plusMonths(1);
            update.run();
        });

        yearButton.addActionListener(e -> {
            int thisYear = Year.now().getValue();
            Integer[] years = new Integer[101];

            for (int i = 0; i < years.length; i++) {
                years[i] = thisYear - 80 + i;
            }

            JComboBox<Integer> yearList = new JComboBox<>(years);
            yearList.setSelectedItem(currentMonth[0].getYear());

            int result = JOptionPane.showConfirmDialog(
                calendar, yearList, "Select Year",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
            );

            if (result == JOptionPane.OK_OPTION) {
                Integer year = (Integer) yearList.getSelectedItem();

                if (year != null) {
                    currentMonth[0] = YearMonth.of(
                        year, currentMonth[0].getMonth()
                    );
                    update.run();
                }
            }
        });

        update.run();
        calendar.setVisible(true);
    }

    private void updateCalendar(YearMonth month, JLabel monthLabel,
                                JPanel daysPanel, JTextField dateField,
                                JDialog calendar) {
        monthLabel.setText(
            month.getMonth().toString() + " " + month.getYear()
        );

        daysPanel.removeAll();

        String[] weekdays = {
            "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"
        };

        for (String day : weekdays) {
            JLabel label = new JLabel(day, JLabel.CENTER);
            label.setForeground(GOLD);
            daysPanel.add(label);
        }

        int firstDay = month.atDay(1).getDayOfWeek().getValue();

        for (int i = 1; i < firstDay; i++) {
            daysPanel.add(new JLabel(""));
        }

        for (int day = 1; day <= month.lengthOfMonth(); day++) {
            int selectedDay = day;
            JButton button = new JButton(String.valueOf(day));
            button.setBackground(RED);
            button.setForeground(Color.WHITE);

            button.addActionListener(e -> {
                dateField.setText(
                    month.atDay(selectedDay).format(
                        DateTimeFormatter.ofPattern("dd-MM-yyyy")
                    )
                );
                calendar.dispose();
            });

            daysPanel.add(button);
        }

        while (daysPanel.getComponentCount() < 49) {
            daysPanel.add(new JLabel(""));
        }

        daysPanel.revalidate();
        daysPanel.repaint();
    }

    private void addToDiary(int entryId, int id, String title,
                            String rating, String date, String review) {
        JPanel movie = new JPanel(new BorderLayout(10, 10));
        movie.setBackground(CARD);
        movie.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        movie.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        movie.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel poster = new JLabel("", SwingConstants.CENTER);
        poster.setPreferredSize(new Dimension(110, 150));

        File file = new File("posters/" + id + ".jpg");

        if (file.exists()) {
            ImageIcon icon = new ImageIcon(file.getAbsolutePath());
            Image image = icon.getImage().getScaledInstance(
                100, 150, Image.SCALE_SMOOTH
            );
            poster.setIcon(new ImageIcon(image));
        } else {
            poster.setText("No Poster");
            poster.setForeground(TEXT);
        }

        JPanel details = new JPanel(new GridLayout(4, 1, 3, 3));
        details.setBackground(CARD);

        JLabel movieTitle = new JLabel(
            title == null ? "Untitled" : title
        );
        movieTitle.setForeground(GOLD);
        movieTitle.setFont(new Font("Arial", Font.BOLD, 20));
        movieTitle.setCursor(new Cursor(Cursor.HAND_CURSOR));

        movieTitle.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                main.showMovieDetails(id);
            }
        });

        JLabel movieRating = new JLabel("Rating: " + rating + "/10");
        movieRating.setForeground(TEXT);

        JLabel movieDate = new JLabel("Watched: " + date);
        movieDate.setForeground(TEXT);

        JLabel movieReview = new JLabel(
            "<html>Review: " + escapeHtml(review) + "</html>"
        );
        movieReview.setForeground(TEXT);

        details.add(movieTitle);
        details.add(movieRating);
        details.add(movieDate);
        details.add(movieReview);

        movie.add(poster, BorderLayout.WEST);
        movie.add(details, BorderLayout.CENTER);

        JButton removeButton = new JButton("Remove Diary Entry");
        removeButton.setForeground(TEXT);
        removeButton.setBackground(RED);
        removeButton.setFocusPainted(false);

        removeButton.addActionListener(e -> {
            int answer = JOptionPane.showConfirmDialog(
                this,
                "Remove this diary entry?",
                "Confirm",
                JOptionPane.YES_NO_OPTION
            );

            if (answer == JOptionPane.YES_OPTION) {
                String sql =
                    "DELETE d FROM diary_entries d " +
                    "JOIN mm70_users.users u ON u.user_id = d.user_id " +
                    "WHERE d.entry_id = ? AND u.username = ?";

                try (Connection con = DatabaseManager.getConnection();
                     PreparedStatement ps = con.prepareStatement(sql)) {

                    ps.setInt(1, entryId);
                    ps.setString(2, main.getUsername());

                    int deleted = ps.executeUpdate();

                    if (deleted > 0) {
                        refreshDiary();
                    } else {
                        JOptionPane.showMessageDialog(
                            this,
                            "Entry not found or not owned by your account."
                        );
                    }

                } catch (Exception ex) {
                    showError("Could not remove entry.", ex);
                }
            }
        });

        boolean favourite = false;

        try {
            favourite = isFavourite(id);
        } catch (SQLException ex) {
            showError("Could not check favourite status.", ex);
        }

        JButton favouriteButton = new JButton(
            favourite ? "♥ Remove Favourite" : "♡ Add Favourite"
        );
        favouriteButton.setForeground(TEXT);
        favouriteButton.setBackground(RED);
        favouriteButton.setFocusPainted(false);

        favouriteButton.addActionListener(e -> toggleFavourite(id));

        JPanel bottomButtons = new JPanel(
            new FlowLayout(FlowLayout.LEFT, 8, 0)
        );
        bottomButtons.setBackground(CARD);
        bottomButtons.add(removeButton);
        bottomButtons.add(favouriteButton);

        movie.add(bottomButtons, BorderLayout.SOUTH);

        diaryPanel.add(movie);
        diaryPanel.add(Box.createVerticalStrut(10));
    }

    private boolean isFavourite(int movieId) throws SQLException {
        String sql =
            "SELECT 1 FROM mm70_users.user_favourites f " +
            "JOIN mm70_users.users u ON u.user_id = f.user_id " +
            "WHERE u.username = ? AND f.tmdb_id = ?";

        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, main.getUsername());
            ps.setInt(2, movieId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void toggleFavourite(int movieId) {
        try {
            if (isFavourite(movieId)) {
                String sql =
                    "DELETE f FROM mm70_users.user_favourites f " +
                    "JOIN mm70_users.users u ON u.user_id = f.user_id " +
                    "WHERE u.username = ? AND f.tmdb_id = ?";

                try (Connection con = DatabaseManager.getConnection();
                     PreparedStatement ps = con.prepareStatement(sql)) {

                    ps.setString(1, main.getUsername());
                    ps.setInt(2, movieId);

                    int deleted = ps.executeUpdate();

                    if (deleted == 0) {
                        JOptionPane.showMessageDialog(
                            this, "Favourite was not found."
                        );
                        return;
                    }
                }

                JOptionPane.showMessageDialog(
                    this, "Removed from Favourites."
                );

            } else {
                String sql =
                    "INSERT INTO mm70_users.user_favourites " +
                    "(user_id, tmdb_id) " +
                    "SELECT user_id, ? FROM mm70_users.users " +
                    "WHERE username = ? " +
                    "ON DUPLICATE KEY UPDATE tmdb_id = VALUES(tmdb_id)";

                try (Connection con = DatabaseManager.getConnection();
                     PreparedStatement ps = con.prepareStatement(sql)) {

                    ps.setInt(1, movieId);
                    ps.setString(2, main.getUsername());

                    int inserted = ps.executeUpdate();

                    if (inserted == 0) {
                        JOptionPane.showMessageDialog(
                            this,
                            "Could not find your account. Please log in again."
                        );
                        return;
                    }
                }

                JOptionPane.showMessageDialog(
                    this, "Added to Favourites!"
                );
            }

            refreshDiary();

        } catch (Exception ex) {
            showError("Could not update favourite.", ex);
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

    private void showError(String message, Exception ex) {
        ex.printStackTrace();

        JOptionPane.showMessageDialog(
            this,
            message + "\n" + ex.getMessage(),
            "Diary Error",
            JOptionPane.ERROR_MESSAGE
        );
    }

    private static class Movie {
        private final int id;
        private final String title;

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
