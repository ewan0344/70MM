
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

    void createUI() {
        setLayout(new BorderLayout());

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.decode("#171415"));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.decode("#171415"));

        JButton backButton = new JButton("←");
        backButton.setForeground(Color.decode("#D6A36A"));
        backButton.setBackground(Color.decode("#A64B52"));
        backButton.addActionListener(e -> main.showPage("home"));
        topPanel.add(backButton, BorderLayout.WEST);

        JLabel label = new JLabel("Diary", JLabel.CENTER);
        label.setForeground(Color.decode("#D6A36A"));
        label.setFont(new Font("Arial", Font.BOLD, 30));
        topPanel.add(label, BorderLayout.CENTER);

        panel.add(topPanel, BorderLayout.NORTH);

        diaryPanel = new JPanel();
        diaryPanel.setLayout(new BoxLayout(diaryPanel, BoxLayout.Y_AXIS));
        diaryPanel.setBackground(Color.decode("#171415"));

        JScrollPane scroll = new JScrollPane(diaryPanel);
        panel.add(scroll, BorderLayout.CENTER);

        JButton addmovie = new JButton("Add movie");
        addmovie.setForeground(Color.decode("#D6A36A"));
        addmovie.setBackground(Color.decode("#A64B52"));
        addmovie.addActionListener(e -> addMovieDialog());

        panel.add(addmovie, BorderLayout.SOUTH);
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
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                this, "Could not create diary table: " + ex.getMessage());
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
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                this, "Could not load diary: " + ex.getMessage());
        }

        diaryPanel.revalidate();
        diaryPanel.repaint();
    }

    void addMovieDialog() {
        JPanel moviePanel = new JPanel(new GridLayout(4, 2, 10, 10));

        JTextField nameField = new JTextField();
        JTextField ratingField = new JTextField();

        JTextField dateField = new JTextField();
        dateField.setEditable(false);
        dateField.setBackground(Color.WHITE);

        JButton calendarButton = new JButton("Calendar");

        JPanel datePanel = new JPanel(new BorderLayout(5, 0));
        datePanel.add(dateField, BorderLayout.CENTER);
        datePanel.add(calendarButton, BorderLayout.EAST);

        calendarButton.addActionListener(e -> showCalendar(dateField));

        JTextField reviewField = new JTextField();

        moviePanel.add(new JLabel("Movie name"));
        moviePanel.add(nameField);
        moviePanel.add(new JLabel("Rating (0-10)"));
        moviePanel.add(ratingField);
        moviePanel.add(new JLabel("Date watched"));
        moviePanel.add(datePanel);
        moviePanel.add(new JLabel("Review"));
        moviePanel.add(reviewField);

        int[] movieId = {-1};
        boolean[] selecting = {false};

        JPopupMenu suggestions = new JPopupMenu();

        nameField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                search();
            }

            public void removeUpdate(DocumentEvent e) {
                search();
            }

            public void changedUpdate(DocumentEvent e) {
                search();
            }

            void search() {
                if (selecting[0]) {
                    return;
                }

                movieId[0] = -1;
                suggestions.setVisible(false);
                suggestions.removeAll();

                String text = nameField.getText().trim();

                if (text.isEmpty()) {
                    return;
                }

                String sql = "SELECT tmdb_id, title FROM movies_new " +
                             "WHERE title LIKE ? ORDER BY title LIMIT 10";

                try (Connection con = DatabaseManager.getConnection();
                     PreparedStatement ps = con.prepareStatement(sql)) {

                    ps.setString(1, "%" + text + "%");

                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            int id = rs.getInt("tmdb_id");
                            String title = rs.getString("title");

                            JMenuItem item = new JMenuItem(title);

                            item.addActionListener(a -> {
                                selecting[0] = true;
                                movieId[0] = id;
                                nameField.setText(title);
                                selecting[0] = false;
                                suggestions.setVisible(false);
                            });

                            suggestions.add(item);
                        }
                    }

                    if (suggestions.getComponentCount() > 0
                            && nameField.isShowing()) {
                        suggestions.show(
                            nameField, 0, nameField.getHeight());
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        int result = JOptionPane.showConfirmDialog(
            this, moviePanel, "Add Movie",
            JOptionPane.OK_CANCEL_OPTION);

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        if (movieId[0] == -1) {
            JOptionPane.showMessageDialog(
                this, "Select a movie from the suggestions.");
            return;
        }

        String rating = ratingField.getText().trim();
        String date = dateField.getText().trim();
        String review = reviewField.getText().trim();

        if (rating.isEmpty() || date.isEmpty()) {
            JOptionPane.showMessageDialog(
                this, "Enter a rating and select a date.");
            return;
        }

        try {
            double value = Double.parseDouble(rating);

            if (value < 0 || value > 10) {
                JOptionPane.showMessageDialog(
                    this, "Rating must be between 0 and 10.");
                return;
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter a valid rating.");
            return;
        }

        String sql = "INSERT INTO diary_entries " +
                     "(tmdb_id, rating, watched_date, review) " +
                     "VALUES (?, ?, ?, ?)";

        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, movieId[0]);
            ps.setDouble(2, Double.parseDouble(rating));
            ps.setString(3, date);
            ps.setString(4, review);
            ps.executeUpdate();

            refreshDiary();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                this, "Could not add movie: " + ex.getMessage());
        }
    }

    // Open calendar with month and year selectors
    void showCalendar(JTextField dateField) {
        Window owner = SwingUtilities.getWindowAncestor(this);

        JDialog calendar = new JDialog(
            owner instanceof Frame ? (Frame) owner : null,
            "Select Date Watched",
            true
        );

        calendar.setSize(390, 400);
        calendar.setMinimumSize(new Dimension(390, 400));
        calendar.setLocationRelativeTo(this);
        calendar.setLayout(new BorderLayout(5, 5));

        JPanel top = new JPanel(new BorderLayout(5, 5));
        top.setBackground(Color.decode("#171415"));

        JButton previous = new JButton("←");
        JButton next = new JButton("→");

        JLabel monthLabel = new JLabel("", JLabel.CENTER);
        monthLabel.setForeground(Color.decode("#D6A36A"));
        monthLabel.setFont(new Font("Arial", Font.BOLD, 18));

        JButton yearButton = new JButton();
        yearButton.setForeground(Color.decode("#D6A36A"));
        yearButton.setBackground(Color.decode("#242021"));
        yearButton.setFocusPainted(false);
        yearButton.setMargin(new Insets(3, 8, 3, 8));

        JPanel center = new JPanel(new BorderLayout(5, 0));
        center.setBackground(Color.decode("#171415"));
        center.add(monthLabel, BorderLayout.CENTER);
        center.add(yearButton, BorderLayout.EAST);

        top.add(previous, BorderLayout.WEST);
        top.add(center, BorderLayout.CENTER);
        top.add(next, BorderLayout.EAST);

        JPanel daysPanel = new JPanel(new GridLayout(7, 7, 3, 3));
        daysPanel.setBackground(Color.decode("#171415"));
        daysPanel.setBorder(
            BorderFactory.createEmptyBorder(5, 5, 5, 5));

        calendar.add(top, BorderLayout.NORTH);
        calendar.add(daysPanel, BorderLayout.CENTER);

        YearMonth[] currentMonth = {YearMonth.now()};

        try {
            if (!dateField.getText().isEmpty()) {
                LocalDate selectedDate = LocalDate.parse(
                    dateField.getText(),
                    DateTimeFormatter.ofPattern("dd-MM-yyyy")
                );

                currentMonth[0] = YearMonth.from(selectedDate);
            }
        } catch (Exception ex) {
            currentMonth[0] = YearMonth.now();
        }

        Runnable refresh = () -> {
            yearButton.setText("Year ▾");

            updateCalendar(
                currentMonth[0],
                monthLabel,
                daysPanel,
                calendar,
                dateField
            );
        };

        previous.addActionListener(e -> {
            currentMonth[0] = currentMonth[0].minusMonths(1);
            refresh.run();
        });

        next.addActionListener(e -> {
            currentMonth[0] = currentMonth[0].plusMonths(1);
            refresh.run();
        });

        yearButton.addActionListener(e -> {
            int currentYear = currentMonth[0].getYear();
            int thisYear = Year.now().getValue();

            Integer[] years = new Integer[101];

            for (int i = 0; i < years.length; i++) {
                years[i] = thisYear - 80 + i;
            }

            JComboBox<Integer> yearList = new JComboBox<>(years);
            yearList.setSelectedItem(currentYear);

            int result = JOptionPane.showConfirmDialog(
                calendar,
                yearList,
                "Select Year",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
            );

            if (result == JOptionPane.OK_OPTION) {
                Integer selectedYear =
                    (Integer) yearList.getSelectedItem();

                if (selectedYear != null) {
                    currentMonth[0] = YearMonth.of(
                        selectedYear,
                        currentMonth[0].getMonth()
                    );

                    refresh.run();
                }
            }
        });

        refresh.run();
        calendar.setVisible(true);
    }

    // Draw numbered dates in a seven-column calendar
    void updateCalendar(
            YearMonth month,
            JLabel monthLabel,
            JPanel daysPanel,
            JDialog calendar,
            JTextField dateField) {

        monthLabel.setText(month.getMonth().toString());

        daysPanel.removeAll();
        daysPanel.setLayout(new GridLayout(7, 7, 3, 3));

        String[] weekdays = {
            "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"
        };

        for (String day : weekdays) {
            JLabel label = new JLabel(day, JLabel.CENTER);
            label.setForeground(Color.decode("#D6A36A"));
            label.setFont(new Font("Arial", Font.BOLD, 13));
            daysPanel.add(label);
        }

        int firstDay = month.atDay(1).getDayOfWeek().getValue();

        for (int i = 1; i < firstDay; i++) {
            daysPanel.add(new JLabel(""));
        }

        for (int day = 1; day <= month.lengthOfMonth(); day++) {
            int selectedDay = day;

            JButton dayButton = new JButton(String.valueOf(day));
            dayButton.setBackground(Color.decode("#A64B52"));
            dayButton.setForeground(Color.WHITE);
            dayButton.setMargin(new Insets(2, 2, 2, 2));

            dayButton.addActionListener(e -> {
                LocalDate selectedDate = month.atDay(selectedDay);

                DateTimeFormatter formatter =
                    DateTimeFormatter.ofPattern("dd-MM-yyyy");

                dateField.setText(selectedDate.format(formatter));
                calendar.dispose();
            });

            daysPanel.add(dayButton);
        }

        while (daysPanel.getComponentCount() < 49) {
            daysPanel.add(new JLabel(""));
        }

        daysPanel.revalidate();
        daysPanel.repaint();
    }

    void addToDiary(
            int entryId, int id, String title,
            String rating, String date, String review) {

        JPanel movie = new JPanel(new BorderLayout(10, 10));
        movie.setBackground(Color.decode("#241E20"));
        movie.setBorder(
            BorderFactory.createEmptyBorder(10, 10, 10, 10));
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
        details.setBackground(Color.decode("#241E20"));

        JLabel movieTitle = new JLabel(title);
        movieTitle.setForeground(Color.decode("#D6A36A"));
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
        removeButton.setBackground(Color.decode("#A64B52"));

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
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(
                        this, "Could not remove entry: " + ex.getMessage());
                }
            }
        });

        movie.add(removeButton, BorderLayout.SOUTH);

        diaryPanel.add(movie);
        diaryPanel.add(Box.createVerticalStrut(10));
    }
}
