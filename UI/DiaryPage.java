
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.io.File;
import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

public class DiaryPage extends JPanel {

    Main main;
    JPanel diaryPanel;

    Color BG = Color.decode("#171415");
    Color CARD = Color.decode("#241E20");
    Color GOLD = Color.decode("#D6A36A");
    Color RED = Color.decode("#A64B52");

    public DiaryPage(Main main) {
        this.main = main;
        createUI();
        createDiaryTable();
        refreshDiary();

        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent e) {
                refreshDiary();
            }
        });
    }

    void createUI() {
        setLayout(new BorderLayout());

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(BG);

        JButton backButton = new JButton("←");
        backButton.setForeground(GOLD);
        backButton.setBackground(RED);
        backButton.addActionListener(e -> main.showPage("home"));
        topPanel.add(backButton, BorderLayout.WEST);

        JLabel label = new JLabel("Diary", JLabel.CENTER);
        label.setForeground(GOLD);
        label.setFont(new Font("Arial", Font.BOLD, 30));
        topPanel.add(label, BorderLayout.CENTER);

        panel.add(topPanel, BorderLayout.NORTH);

        diaryPanel = new JPanel();
        diaryPanel.setLayout(new BoxLayout(diaryPanel, BoxLayout.Y_AXIS));
        diaryPanel.setBackground(BG);

        JScrollPane scroll = new JScrollPane(diaryPanel);
        panel.add(scroll, BorderLayout.CENTER);

        JButton addMovie = new JButton("Add movie");
        addMovie.setForeground(GOLD);
        addMovie.setBackground(RED);
        addMovie.addActionListener(e -> addMovieDialog());

        panel.add(addMovie, BorderLayout.SOUTH);
        add(panel, BorderLayout.CENTER);
    }

    void createDiaryTable() {
        String sql = "CREATE TABLE IF NOT EXISTS diary_entries (" +
                "entry_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "tmdb_id INT NOT NULL, " +
                "rating DECIMAL(3,1) NOT NULL, " +
                "watched_date VARCHAR(20) NOT NULL, " +
                "review TEXT, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (tmdb_id) REFERENCES movies_new(tmdb_id) " +
                "ON DELETE CASCADE)";

        try (Connection con = DatabaseManager.getConnection();
             Statement st = con.createStatement()) {
            st.executeUpdate(sql);
        } catch (Exception ex) {
            showError("Could not create diary table.", ex);
        }
    }

    void refreshDiary() {
        diaryPanel.removeAll();

        String sql = "SELECT d.entry_id, m.tmdb_id, m.title, " +
                "d.rating, d.watched_date, d.review " +
                "FROM diary_entries d " +
                "JOIN movies_new m ON d.tmdb_id = m.tmdb_id " +
                "ORDER BY d.entry_id DESC";

        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                addToDiary(
                        rs.getInt("entry_id"),
                        rs.getInt("tmdb_id"),
                        rs.getString("title"),
                        rs.getString("rating"),
                        rs.getString("watched_date"),
                        rs.getString("review")
                );
            }
        } catch (Exception ex) {
            showError("Could not load diary.", ex);
        }

        diaryPanel.revalidate();
        diaryPanel.repaint();
    }

    void addMovieDialog() {
        JPanel moviePanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        JTextField nameField = new JTextField(22);
        JTextField ratingField = new JTextField(18);
        JTextField dateField = new JTextField(12);
        JTextField reviewField = new JTextField(18);

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

        JPanel namePanel = new JPanel();
        namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.Y_AXIS));
        namePanel.add(nameField);
        namePanel.add(suggestionScroll);

        int[] selectedMovieId = {-1};
        boolean[] selecting = {false};

        nameField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                searchMovies();
            }

            public void removeUpdate(DocumentEvent e) {
                searchMovies();
            }

            public void changedUpdate(DocumentEvent e) {
                searchMovies();
            }

            void searchMovies() {
                if (selecting[0]) {
                    return;
                }

                selectedMovieId[0] = -1;
                suggestionModel.clear();
                suggestionScroll.setVisible(false);

                String text = nameField.getText().trim();

                if (text.isEmpty()) {
                    namePanel.revalidate();
                    namePanel.repaint();
                    return;
                }

                String sql = "SELECT tmdb_id, title FROM movies_new " +
                        "WHERE title LIKE ? ORDER BY title LIMIT 100";

                try (Connection con = DatabaseManager.getConnection();
                     PreparedStatement ps = con.prepareStatement(sql)) {

                    ps.setString(1, text + "%");

                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            suggestionModel.addElement(
                                    new Movie(rs.getInt("tmdb_id"),
                                              rs.getString("title")));
                        }
                    }

                    suggestionScroll.setVisible(!suggestionModel.isEmpty());
                    namePanel.revalidate();
                    namePanel.repaint();

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        suggestionList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                return;
            }

            Movie selected = suggestionList.getSelectedValue();

            if (selected != null) {
                selecting[0] = true;
                selectedMovieId[0] = selected.id;
                nameField.setText(selected.title);
                suggestionScroll.setVisible(false);
                selecting[0] = false;
            }
        });

        addField(moviePanel, gbc, 0, "Movie name", namePanel);
        addField(moviePanel, gbc, 1, "Rating (0-10)", ratingField);
        addField(moviePanel, gbc, 2, "Date watched", datePanel);
        addField(moviePanel, gbc, 3, "Review", reviewField);

        JOptionPane optionPane = new JOptionPane(
                moviePanel,
                JOptionPane.PLAIN_MESSAGE,
                JOptionPane.OK_CANCEL_OPTION
        );

        JDialog dialog = optionPane.createDialog(this, "Add Movie");
        dialog.setResizable(false);
        dialog.setSize(460, 310);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

        Object choice = optionPane.getValue();

        if (!(choice instanceof Integer)
                || (Integer) choice != JOptionPane.OK_OPTION) {
            return;
        }

        if (selectedMovieId[0] == -1) {
            JOptionPane.showMessageDialog(this,
                    "Select a movie from the suggestions.");
            return;
        }

        String rating = ratingField.getText().trim();
        String date = dateField.getText().trim();
        String review = reviewField.getText().trim();

        if (rating.isEmpty() || date.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Enter a rating and select a date.");
            return;
        }

        double value;

        try {
            value = Double.parseDouble(rating);
            if (value < 0 || value > 10) {
                JOptionPane.showMessageDialog(this,
                        "Rating must be between 0 and 10.");
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter a valid rating.");
            return;
        }

        String sql = "INSERT INTO diary_entries " +
                "(tmdb_id, rating, watched_date, review) VALUES (?, ?, ?, ?)";

        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, selectedMovieId[0]);
            ps.setDouble(2, value);
            ps.setString(3, date);
            ps.setString(4, review);
            ps.executeUpdate();
            refreshDiary();
        } catch (Exception ex) {
            showError("Could not add movie.", ex);
        }
    }

    void addField(JPanel panel, GridBagConstraints gbc,
                  int row, String label, JComponent field) {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(field, gbc);
    }

    void showCalendar(JTextField dateField) {
        Window owner = SwingUtilities.getWindowAncestor(this);

        JDialog calendar = new JDialog(
                owner instanceof Frame ? (Frame) owner : null,
                "Select Date Watched", true);

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
                currentMonth[0], monthLabel, daysPanel, dateField, calendar);

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
                    JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                Integer year = (Integer) yearList.getSelectedItem();
                if (year != null) {
                    currentMonth[0] = YearMonth.of(
                            year, currentMonth[0].getMonth());
                    update.run();
                }
            }
        });

        update.run();
        calendar.setVisible(true);
    }

    void updateCalendar(YearMonth month, JLabel monthLabel,
                        JPanel daysPanel, JTextField dateField,
                        JDialog calendar) {
        monthLabel.setText(month.getMonth().toString() + " " + month.getYear());
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
                dateField.setText(month.atDay(selectedDay).format(
                        DateTimeFormatter.ofPattern("dd-MM-yyyy")));
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

    void addToDiary(int entryId, int id, String title,
                    String rating, String date, String review) {
        JPanel movie = new JPanel(new BorderLayout(10, 10));
        movie.setBackground(CARD);
        movie.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        movie.setMaximumSize(new Dimension(650, 180));
        movie.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel poster = new JLabel();
        poster.setPreferredSize(new Dimension(100, 150));

        File file = new File("posters/" + id + ".jpg");

        if (file.exists()) {
            ImageIcon icon = new ImageIcon(file.getAbsolutePath());
            Image image = icon.getImage().getScaledInstance(
                    100, 150, Image.SCALE_SMOOTH);
            poster.setIcon(new ImageIcon(image));
        } else {
            poster.setText("No Poster");
            poster.setForeground(Color.WHITE);
        }

        JPanel details = new JPanel(new GridLayout(4, 1));
        details.setBackground(CARD);

        JLabel movieTitle = new JLabel(title);
        movieTitle.setForeground(GOLD);
        movieTitle.setFont(new Font("Arial", Font.BOLD, 20));
        movieTitle.setCursor(new Cursor(Cursor.HAND_CURSOR));
        movieTitle.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                main.showMovieDetails(id);
            }
        });

        JLabel movieRating = new JLabel("Rating: " + rating + "/10");
        movieRating.setForeground(Color.WHITE);
        JLabel movieDate = new JLabel("Watched: " + date);
        movieDate.setForeground(Color.WHITE);
        JLabel movieReview = new JLabel("Review: " + review);
        movieReview.setForeground(Color.WHITE);

        details.add(movieTitle);
        details.add(movieRating);
        details.add(movieDate);
        details.add(movieReview);

        movie.add(poster, BorderLayout.WEST);
        movie.add(details, BorderLayout.CENTER);

        JButton removeButton = new JButton("Remove");
        removeButton.setForeground(Color.WHITE);
        removeButton.setBackground(RED);

        removeButton.addActionListener(e -> {
            int answer = JOptionPane.showConfirmDialog(
                    this, "Remove this diary entry?",
                    "Confirm", JOptionPane.YES_NO_OPTION);

            if (answer == JOptionPane.YES_OPTION) {
                try (Connection con = DatabaseManager.getConnection();
                     PreparedStatement ps = con.prepareStatement(
                             "DELETE FROM diary_entries WHERE entry_id = ?")) {
                    ps.setInt(1, entryId);
                    ps.executeUpdate();
                    refreshDiary();
                } catch (Exception ex) {
                    showError("Could not remove entry.", ex);
                }
            }
        });

        movie.add(removeButton, BorderLayout.SOUTH);
        diaryPanel.add(movie);
        diaryPanel.add(Box.createVerticalStrut(10));
    }

    void showError(String message, Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this,
                message + "\n" + ex.getMessage(),
                "Diary Error", JOptionPane.ERROR_MESSAGE);
    }

    static class Movie {
        int id;
        String title;

        Movie(int id, String title) {
            this.id = id;
            this.title = title;
        }

        public String toString() {
            return title;
        }
    }
}
