import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import javax.imageio.ImageIO;

public class AdminDashboard extends JPanel {

    private final Main main;

    private final Color background = Color.decode("#171415");
    private final Color sidebarColor = Color.decode("#171415");
    private final Color red = Color.decode("#A64B52");
    private final Color gold = Color.decode("#D6A36A");
    private final Color cream = Color.decode("#F1E9DD");
    private final Color cardColor = Color.decode("#242021");

    private JLabel movieCountLabel;
    private JLabel userCountLabel;

    public AdminDashboard(Main main) {
        this.main = main;

        setLayout(new BorderLayout());
        setBackground(background);

        add(createSidebar(), BorderLayout.WEST);
        add(createDashboardContent(), BorderLayout.CENTER);

        refreshStats();
    }

    // ==================== SIDEBAR ====================

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(sidebarColor);
        sidebar.setPreferredSize(new Dimension(205, 0));
        sidebar.setBorder(new EmptyBorder(25, 12, 20, 12));

        JPanel navigation = new JPanel();
        navigation.setOpaque(false);
        navigation.setLayout(
            new BoxLayout(navigation, BoxLayout.Y_AXIS)
        );

        JLabel adminLabel = new JLabel("A D M I N");
        adminLabel.setFont(new Font("Arial", Font.BOLD, 14));
        adminLabel.setForeground(cream);
        adminLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        navigation.add(adminLabel);
        navigation.add(Box.createVerticalStrut(35));

        navigation.add(createNavButton("Dashboard", () -> {
            refreshStats();
            showDashboardMessage();
        }));

        navigation.add(Box.createVerticalStrut(10));
        navigation.add(createNavButton("Add Movie", this::addMovie));

        navigation.add(Box.createVerticalStrut(10));
        navigation.add(createNavButton(
            "Manage Movies", this::manageMovies
        ));

        navigation.add(Box.createVerticalStrut(10));
        navigation.add(createNavButton("Users", this::showUsers));

        navigation.add(Box.createVerticalStrut(10));
        navigation.add(createNavButton(
            "Analytics", this::showAnalytics
        ));

        sidebar.add(navigation, BorderLayout.NORTH);

        JButton logout = createNavButton("Logout", () -> {
            int answer = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to log out?",
                "Logout",
                JOptionPane.YES_NO_OPTION
            );

            if (answer == JOptionPane.YES_OPTION) {
                main.setUsername("Username");
                main.showPage("login");
            }
        });

        sidebar.add(logout, BorderLayout.SOUTH);

        return sidebar;
    }

    private JButton createNavButton(
            String text, Runnable action) {

        JButton button = new JButton(text);

        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setForeground(cream);
        button.setBackground(red);
        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.setFocusPainted(false);
        button.setBorder(
            BorderFactory.createLineBorder(gold)
        );
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        button.setPreferredSize(new Dimension(180, 46));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addActionListener(e -> action.run());

        return button;
    }

    // ==================== DASHBOARD HOME ====================

    private JPanel createDashboardContent() {
        JPanel content = new JPanel(new BorderLayout(0, 25));
        content.setBackground(background);
        content.setBorder(new EmptyBorder(20, 25, 25, 25));

        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(
            new BoxLayout(heading, BoxLayout.Y_AXIS)
        );

        JLabel logo = new JLabel("70mm");
        logo.setFont(new Font("Impact", Font.BOLD, 76));
        logo.setForeground(gold);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("Admin Dashboard");
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setForeground(cream);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel(
            "MANAGE YOUR MOVIE COLLECTION"
        );
        subtitle.setFont(new Font("Arial", Font.PLAIN, 12));
        subtitle.setForeground(cream);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        heading.add(logo);
        heading.add(Box.createVerticalStrut(0));
        heading.add(title);
        heading.add(Box.createVerticalStrut(7));
        heading.add(subtitle);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(
            new BoxLayout(body, BoxLayout.Y_AXIS)
        );

        body.add(createStatistics());
        body.add(Box.createVerticalStrut(25));

        JLabel quickTitle = new JLabel("Quick Actions");
        quickTitle.setForeground(cream);
        quickTitle.setFont(new Font("Arial", Font.BOLD, 23));
        quickTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        body.add(quickTitle);
        body.add(Box.createVerticalStrut(15));

        JPanel actions = new JPanel(
            new GridLayout(1, 3, 10, 0)
        );
        actions.setOpaque(false);

        actions.add(createActionButton(
            "Add Movie", this::addMovie
        ));
        actions.add(createActionButton(
            "Manage Movies", this::manageMovies
        ));
        actions.add(createActionButton(
            "Manage Users", this::showUsers
        ));

        body.add(actions);

        content.add(heading, BorderLayout.NORTH);
        content.add(body, BorderLayout.CENTER);

        return content;
    }

    private void showDashboardMessage() {
        // Dashboard is already displayed.
        // Refreshing its statistics updates the visible counts.
        revalidate();
        repaint();
    }

    // ==================== STATISTICS CARDS ====================

    private JPanel createStatistics() {
        JPanel panel = new JPanel(
            new GridLayout(1, 2, 15, 0)
        );
        panel.setOpaque(false);

        JPanel moviesCard = createCard();
        JPanel usersCard = createCard();

        JLabel moviesTitle = createLabel(
            "TOTAL MOVIES", 12, cream
        );
        movieCountLabel = createLabel(
            "Loading...", 32, gold
        );

        JLabel usersTitle = createLabel(
            "REGISTERED USERS", 12, cream
        );
        userCountLabel = createLabel(
            "Loading...", 32, gold
        );

        moviesCard.add(moviesTitle);
        moviesCard.add(Box.createVerticalStrut(12));
        moviesCard.add(movieCountLabel);

        usersCard.add(usersTitle);
        usersCard.add(Box.createVerticalStrut(12));
        usersCard.add(userCountLabel);

        panel.add(moviesCard);
        panel.add(usersCard);
        panel.setPreferredSize(new Dimension(480, 125));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 125));

        return panel;
    }

    private JPanel createCard() {
        JPanel card = new JPanel();
        card.setBackground(cardColor);
        card.setLayout(
            new BoxLayout(card, BoxLayout.Y_AXIS)
        );
        card.setBorder(new EmptyBorder(20, 20, 20, 20));

        return card;
    }

    private JLabel createLabel(
            String text, int size, Color color) {

        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.PLAIN, size));
        label.setForeground(color);

        return label;
    }

    private JButton createActionButton(
            String title, Runnable action) {

        JButton button = new JButton(title);

        button.setFont(new Font("Arial", Font.BOLD, 13));
        button.setBackground(red);
        button.setForeground(cream);
        button.setFocusPainted(false);
        button.setBorder(
            BorderFactory.createLineBorder(gold)
        );
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addActionListener(e -> action.run());

        return button;
    }

    // ==================== DATABASE COUNTS ====================

    public void refreshStats() {
        if (movieCountLabel == null || userCountLabel == null) {
            return;
        }

        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(
                 "SELECT COUNT(*) FROM movies_new"
             );
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                movieCountLabel.setText(
                    String.valueOf(rs.getLong(1))
                );
            }

        } catch (Exception e) {
            movieCountLabel.setText("Unavailable");
            System.err.println(
                "Movie count error: " + e.getMessage()
            );
        }

        try {
            UserDatabase.setup();

            try (Connection con = DriverManager.getConnection(
                     UserDatabase.DATABASE_URL,
                     UserDatabase.USER,
                     UserDatabase.PASSWORD
                 );
                 PreparedStatement ps = con.prepareStatement(
                     "SELECT COUNT(*) FROM users"
                 );
                 ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    userCountLabel.setText(
                        String.valueOf(rs.getLong(1))
                    );
                }
            }

        } catch (Exception e) {
            userCountLabel.setText("Unavailable");
            System.err.println(
                "User count error: " + e.getMessage()
            );
        }
    }
    // ==================== ADD MOVIE ====================

    private void addMovie() {
        JTextField idField = new JTextField();
        idField.setEditable(false);

        // Generate an unused ID by checking the current movies_new table.
        try {
            idField.setText(String.valueOf(generateUniqueMovieId()));
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                this,
                "Could not generate a movie ID:\n" + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        JTextField titleField = new JTextField();
        JTextField yearField = new JTextField();
        JTextField durationField = new JTextField();
        JTextField genresField = new JTextField();
        JTextField ageRatingField = new JTextField();
        JTextField directorField = new JTextField();
        JTextField productionField = new JTextField();

        JTextField posterField = new JTextField();
        posterField.setEditable(false);

        JButton browseButton = new JButton("Choose Poster");
        final File[] selectedPoster = {null};

        browseButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Choose movie poster");

            int result = chooser.showOpenDialog(this);

            if (result == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();

                try {
                    BufferedImage image = ImageIO.read(file);

                    if (image == null) {
                        JOptionPane.showMessageDialog(
                            this, "Please choose a valid image."
                        );
                        return;
                    }

                    selectedPoster[0] = file;
                    posterField.setText(file.getAbsolutePath());

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                        this, "Could not read this image."
                    );
                }
            }
        });

        JPanel posterPanel = new JPanel(new BorderLayout(5, 0));
        posterPanel.add(posterField, BorderLayout.CENTER);
        posterPanel.add(browseButton, BorderLayout.EAST);

        JPanel form = new JPanel(
            new GridLayout(0, 2, 8, 8)
        );

        form.add(new JLabel("TMDB ID:"));
        form.add(idField);
        form.add(new JLabel("Movie title:"));
        form.add(titleField);
        form.add(new JLabel("Release year:"));
        form.add(yearField);
        form.add(new JLabel("Duration (minutes):"));
        form.add(durationField);
        form.add(new JLabel("Genres:"));
        form.add(genresField);
        form.add(new JLabel("Age rating:"));
        form.add(ageRatingField);
        form.add(new JLabel("Director:"));
        form.add(directorField);
        form.add(new JLabel("Production house:"));
        form.add(productionField);
        form.add(new JLabel("Poster image:"));
        form.add(posterPanel);

        int answer = JOptionPane.showConfirmDialog(
            this,
            form,
            "Add Movie",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );

        if (answer != JOptionPane.OK_OPTION) {
            return;
        }

        int id;
        int year;
        int duration;

        try {
            id = Integer.parseInt(idField.getText().trim());
            year = Integer.parseInt(yearField.getText().trim());
            duration = Integer.parseInt(
                durationField.getText().trim()
            );

            if (id <= 0 || year <= 0 || duration <= 0) {
                throw new NumberFormatException();
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(
                this,
                "TMDB ID, year and duration must be positive numbers."
            );
            return;
        }

        String title = titleField.getText().trim();

        if (title.isEmpty() || selectedPoster[0] == null) {
            JOptionPane.showMessageDialog(
                this, "Enter the title and choose a poster."
            );
            return;
        }

        Path posterDirectory = Paths.get("posters");
        Path posterPath = posterDirectory.resolve(id + ".jpg");

        boolean posterWritten = false;
        boolean movieInserted = false;

        try {
            Files.createDirectories(posterDirectory);

            BufferedImage image = ImageIO.read(selectedPoster[0]);

            if (image == null) {
                throw new Exception("Unsupported poster image.");
            }

            BufferedImage rgb = new BufferedImage(
                image.getWidth(),
                image.getHeight(),
                BufferedImage.TYPE_INT_RGB
            );

            Graphics2D graphics = rgb.createGraphics();
            graphics.setColor(Color.WHITE);
            graphics.fillRect(
                0, 0, rgb.getWidth(), rgb.getHeight()
            );
            graphics.drawImage(image, 0, 0, null);
            graphics.dispose();

            if (!ImageIO.write(rgb, "jpg", posterPath.toFile())) {
                throw new Exception("Could not save poster as JPEG.");
            }

            posterWritten = true;

            String sql =
                "INSERT INTO movies_new " +
                "(tmdb_id, title, release_year, duration, genres, " +
                "age_rating, director, production_house, poster_path) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (Connection con = DatabaseManager.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setInt(1, id);
                ps.setString(2, title);
                ps.setInt(3, year);
                ps.setInt(4, duration);
                ps.setString(5, genresField.getText().trim());
                ps.setString(6, ageRatingField.getText().trim());
                ps.setString(7, directorField.getText().trim());
                ps.setString(8, productionField.getText().trim());
                ps.setString(9, "posters/" + id + ".jpg");

                ps.executeUpdate();
                movieInserted = true;
            }

            JOptionPane.showMessageDialog(
                this, "Movie and poster saved successfully!"
            );

            refreshStats();

        } catch (SQLIntegrityConstraintViolationException ex) {
            JOptionPane.showMessageDialog(
                this, "That TMDB ID already exists."
            );

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                this,
                "Could not add movie:\n" + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
            );

        } finally {
            // Only remove the poster if the database insert failed.
            if (posterWritten && !movieInserted) {
                try {
                    Files.deleteIfExists(posterPath);
                } catch (Exception ex) {
                    System.err.println(
                        "Could not clean up poster: " + ex.getMessage()
                    );
                }
            }
        }
    }

    /**
     * Returns a random positive ID that is not currently used in movies_new.
     * Each candidate is checked against the database; if it already exists,
     * another candidate is generated.
     */
    private int generateUniqueMovieId() throws SQLException {
        java.security.SecureRandom random = new java.security.SecureRandom();

        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(
                 "SELECT 1 FROM movies_new WHERE tmdb_id = ?")) {

            for (int attempt = 0; attempt < 100; attempt++) {
                // Random 8-digit ID, between 10,000,000 and 99,999,999.
                int candidate = 10_000_000 + random.nextInt(90_000_000);
                ps.setInt(1, candidate);

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return candidate;
                    }
                }
            }
        }

        throw new SQLException(
            "Could not find an unused movie ID after 100 attempts. Try again."
        );
    }

    // ==================== MANAGE / DELETE MOVIES ====================
    private void manageMovies() {
        DefaultTableModel model = new DefaultTableModel(
            new String[]{"TMDB ID", "Title", "Year", "Director"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        String sql =
            "SELECT tmdb_id, title, release_year, director " +
            "FROM movies_new ORDER BY title";

        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("tmdb_id"),
                    rs.getString("title"),
                    rs.getInt("release_year"),
                    rs.getString("director")
                });
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                this, "Could not load movies:\n" + ex.getMessage()
            );
            return;
        }

        // Search field: filters the movie titles as the user types.
        JTextField searchField = new JTextField(24);
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));
        searchField.setBackground(cardColor);
        searchField.setForeground(cream);
        searchField.setCaretColor(cream);
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(gold),
            new EmptyBorder(7, 8, 7, 8)
        ));

        JLabel searchLabel = new JLabel("Search by movie name:");
        searchLabel.setForeground(cream);
        searchLabel.setFont(new Font("Arial", Font.BOLD, 13));

        JPanel searchPanel = new JPanel(new BorderLayout(10, 0));
        searchPanel.setBackground(background);
        searchPanel.add(searchLabel, BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);

        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(25);
        table.setBackground(cardColor);
        table.setForeground(cream);
        table.setGridColor(gold);
        table.setSelectionBackground(red);
        table.setSelectionForeground(cream);
        table.getTableHeader().setBackground(red);
        table.getTableHeader().setForeground(cream);

        // Case-insensitive live filtering on the Title column (column 1).
        javax.swing.table.TableRowSorter<DefaultTableModel> sorter =
            new javax.swing.table.TableRowSorter<>(model);
        table.setRowSorter(sorter);

        searchField.getDocument().addDocumentListener(
            new javax.swing.event.DocumentListener() {
                private void filterMovies() {
                    String query = searchField.getText().trim();
                    if (query.isEmpty()) {
                        sorter.setRowFilter(null);
                    } else {
                        sorter.setRowFilter(
                            javax.swing.RowFilter.regexFilter(
                                "(?i)" + java.util.regex.Pattern.quote(query), 1
                            )
                        );
                    }
                }

                @Override
                public void insertUpdate(javax.swing.event.DocumentEvent e) {
                    filterMovies();
                }

                @Override
                public void removeUpdate(javax.swing.event.DocumentEvent e) {
                    filterMovies();
                }

                @Override
                public void changedUpdate(javax.swing.event.DocumentEvent e) {
                    filterMovies();
                }
            }
        );

        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension(650, 350));

        JButton deleteButton = new JButton("Delete Selected Movie");
        JButton closeButton = new JButton("Close");

        styleDialogButton(deleteButton);
        styleDialogButton(closeButton);

        JPanel buttons = new JPanel();
        buttons.setBackground(background);
        buttons.add(deleteButton);
        buttons.add(closeButton);

        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(background);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.add(searchPanel, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);

        JDialog dialog = new JDialog(
            (Frame) SwingUtilities.getWindowAncestor(this),
            "Manage Movies",
            true
        );

        dialog.setContentPane(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);

        deleteButton.addActionListener(e -> {
            int viewRow = table.getSelectedRow();

            if (viewRow < 0) {
                JOptionPane.showMessageDialog(
                    dialog, "Select a movie first."
                );
                return;
            }

            // Convert filtered/sorted table row to the underlying model row.
            int modelRow = table.convertRowIndexToModel(viewRow);
            int id = ((Number) model.getValueAt(modelRow, 0)).intValue();
            String title = String.valueOf(model.getValueAt(modelRow, 1));

            int confirm = JOptionPane.showConfirmDialog(
                dialog,
                "Permanently delete \"" + title + "\"?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            try (Connection con = DatabaseManager.getConnection();
                 PreparedStatement ps = con.prepareStatement(
                     "DELETE FROM movies_new WHERE tmdb_id = ?"
                 )) {

                ps.setInt(1, id);
                int deleted = ps.executeUpdate();

                if (deleted > 0) {
                    deletePosterFile(Paths.get("posters", id + ".jpg"));
                    model.removeRow(modelRow);
                    refreshStats();

                    JOptionPane.showMessageDialog(
                        dialog, "Movie deleted from the database."
                    );
                } else {
                    JOptionPane.showMessageDialog(
                        dialog, "Movie was not found."
                    );
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                    dialog,
                    "Could not delete movie.\n" +
                    "A related table may be preventing deletion.\n\n" +
                    ex.getMessage(),
                    "Delete Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        });

        closeButton.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }

    private void deletePosterFile(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (Exception ex) {
            System.err.println(
                "Could not delete poster: " + ex.getMessage()
            );
        }
    }

    // ==================== REGISTERED USERS ====================

    private void showUsers() {
        DefaultTableModel model = new DefaultTableModel(
            new String[]{"User ID", "Username"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        try {
            UserDatabase.setup();

            try (Connection con = DriverManager.getConnection(
                     UserDatabase.DATABASE_URL,
                     UserDatabase.USER,
                     UserDatabase.PASSWORD
                 );
                 PreparedStatement ps = con.prepareStatement(
                     "SELECT user_id, username FROM users ORDER BY user_id"
                 );
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getInt("user_id"),
                        rs.getString("username")
                    });
                }
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                this, "Could not load users:\n" + ex.getMessage()
            );
            return;
        }

        JTable table = new JTable(model);
        table.setRowHeight(25);
        table.setBackground(cardColor);
        table.setForeground(cream);
        table.setGridColor(gold);
        table.getTableHeader().setBackground(red);
        table.getTableHeader().setForeground(cream);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension(400, 300));

        JOptionPane.showMessageDialog(
            this,
            scroll,
            "70mm - Registered Users",
            JOptionPane.PLAIN_MESSAGE
        );
    }

    // ==================== ANALYTICS ====================

    private void showAnalytics() {
        refreshStats();

        JOptionPane.showMessageDialog(
            this,
            "70mm DATABASE OVERVIEW\n\n" +
            "Total movies: " + movieCountLabel.getText() + "\n" +
            "Registered users: " + userCountLabel.getText(),
            "Analytics",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    // ==================== DIALOG BUTTON STYLE ====================

    private void styleDialogButton(JButton button) {
        button.setFont(new Font("Arial", Font.BOLD, 13));
        button.setForeground(cream);
        button.setBackground(red);
        button.setFocusPainted(false);
        button.setBorder(
            BorderFactory.createLineBorder(gold)
        );
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}