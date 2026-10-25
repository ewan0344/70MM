import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.sql.*;

public class ProfilePage extends JPanel {

    private final Main main;
    private JPanel recentMovies;

    private final Color BG = Color.decode("#171415");
    private final Color ACCENT = Color.decode("#A64B52");
    private final Color GOLD = Color.decode("#D6A36A");
    private final Color TEXT = Color.decode("#F1E9DD");

    private JLabel usernameLabel;

    public ProfilePage(Main main) {
        this.main = main;

        setBackground(BG);
        setLayout(new BorderLayout());

        // Top bar
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(BG);
        top.setBorder(BorderFactory.createLineBorder(GOLD));

        JButton backButton = new JButton("←");
        styleButton(backButton);
        backButton.addActionListener(e -> main.showPage("home"));

        JLabel title = new JLabel("Profile", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(TEXT);

        JLabel logo = new JLabel("70mm", SwingConstants.RIGHT);
        logo.setFont(new Font("Impact", Font.BOLD, 40));
        logo.setForeground(GOLD);

        top.add(backButton, BorderLayout.WEST);
        top.add(title, BorderLayout.CENTER);
        top.add(logo, BorderLayout.EAST);

        // Username and activity
        JPanel info = new JPanel(new GridLayout(2, 1));
        info.setBackground(BG);

        usernameLabel = new JLabel(main.getUsername(), SwingConstants.CENTER);
        usernameLabel.setForeground(TEXT);
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 20));

        JLabel activity = new JLabel("Recently Watched", SwingConstants.CENTER);
        activity.setForeground(GOLD);
        activity.setFont(new Font("Arial", Font.BOLD, 18));

        info.add(usernameLabel);
        info.add(activity);

        // Recent movies
        recentMovies = new JPanel(new GridLayout(1, 4, 10, 10));
        recentMovies.setBackground(BG);
        recentMovies.setBorder(
                BorderFactory.createEmptyBorder(5, 15, 10, 15));

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(BG);
        center.add(info, BorderLayout.NORTH);
        center.add(recentMovies, BorderLayout.CENTER);

        // Navigation buttons
        JPanel options = new JPanel(new GridLayout(1, 5, 8, 10));
        options.setBackground(BG);
        options.setBorder(
                BorderFactory.createEmptyBorder(10, 10, 20, 10));

        JButton diary = new JButton("Diary");
        JButton watchlist = new JButton("Watchlist");
        JButton list = new JButton("List");
        JButton friends = new JButton("Friends");
        JButton favourites = new JButton("Favourites");

        for (JButton button : new JButton[]{
                diary, watchlist, list, friends, favourites}) {
            styleButton(button);
            button.setMargin(new Insets(5, 2, 5, 2));
        }

        diary.addActionListener(e -> main.showPage("diary"));
        watchlist.addActionListener(e -> main.showPage("watchlist"));
        list.addActionListener(e -> main.showPage("list"));
        friends.addActionListener(e -> main.openFriendsWindow());
        favourites.addActionListener(e -> main.showPage("favourites"));

        options.add(diary);
        options.add(watchlist);
        options.add(list);
        options.add(friends);
        options.add(favourites);

        add(top, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
        add(options, BorderLayout.SOUTH);

        // Refresh when profile is shown
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                usernameLabel.setText(main.getUsername());
                loadRecentMovies();
            }
        });

        loadRecentMovies();
    }

    public void loadRecentMovies() {
        recentMovies.removeAll();

        String sql =
                "SELECT m.tmdb_id, m.title, d.rating " +
                "FROM diary_entries d " +
                "JOIN movies_new m ON d.tmdb_id = m.tmdb_id " +
                "JOIN mm70_users.users u ON u.user_id = d.user_id " +
                "WHERE u.username = ? " +
                "AND d.entry_id = (" +
                "    SELECT MAX(d2.entry_id) " +
                "    FROM diary_entries d2 " +
                "    WHERE d2.tmdb_id = d.tmdb_id " +
                "      AND d2.user_id = d.user_id" +
                ") " +
                "ORDER BY d.entry_id DESC " +
                "LIMIT 4";

        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, main.getUsername());

            try (ResultSet rs = ps.executeQuery()) {
                int count = 0;

                while (rs.next()) {
                    int movieId = rs.getInt("tmdb_id");
                    String movieTitle = rs.getString("title");
                    String rating = rs.getString("rating");

                    JPanel card = new JPanel(new BorderLayout(5, 5));
                    card.setBackground(Color.decode("#241E20"));

                    JLabel poster = new JLabel("", SwingConstants.CENTER);
                    poster.setPreferredSize(new Dimension(120, 155));

                    File file = new File("posters/" + movieId + ".jpg");

                    if (file.isFile()) {
                        ImageIcon icon = new ImageIcon(file.getAbsolutePath());

                        Image image = icon.getImage().getScaledInstance(
                                110, 145, Image.SCALE_SMOOTH);

                        poster.setIcon(new ImageIcon(image));
                    } else {
                        poster.setText("No Poster");
                        poster.setForeground(TEXT);
                    }

                    JLabel name = new JLabel(
                            "<html><center>" + escapeHtml(movieTitle)
                                    + "</center></html>",
                            SwingConstants.CENTER);

                    name.setForeground(GOLD);
                    name.setCursor(new Cursor(Cursor.HAND_CURSOR));

                    name.addMouseListener(new java.awt.event.MouseAdapter() {
                        @Override
                        public void mouseClicked(java.awt.event.MouseEvent e) {
                            main.showMovieDetails(movieId);
                        }
                    });

                    JLabel score = new JLabel(
                            "★ " + (rating == null ? "-" : rating) + "/10",
                            SwingConstants.CENTER);

                    score.setForeground(TEXT);

                    card.add(poster, BorderLayout.NORTH);
                    card.add(name, BorderLayout.CENTER);
                    card.add(score, BorderLayout.SOUTH);

                    recentMovies.add(card);
                    count++;
                }

                if (count == 0) {
                    JLabel empty = new JLabel(
                            "No movies watched yet",
                            SwingConstants.CENTER);

                    empty.setForeground(TEXT);
                    recentMovies.add(empty);
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();

            JLabel error = new JLabel(
                    "Could not load recent movies",
                    SwingConstants.CENTER);

            error.setForeground(TEXT);
            recentMovies.add(error);
        }

        recentMovies.revalidate();
        recentMovies.repaint();
    }

    private void styleButton(JButton button) {
        button.setBackground(ACCENT);
        button.setForeground(TEXT);
        button.setFocusPainted(false);
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }

        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}