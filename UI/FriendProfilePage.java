
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.sql.*;

public class FriendProfilePage extends JFrame {

    Color BG = Color.decode("#171415");
    Color CARD = Color.decode("#241E20");
    Color GOLD = Color.decode("#D6A36A");
    Color RED = Color.decode("#A64B52");
    Color TEXT = Color.decode("#F1E9DD");

    String friendUsername;
    JPanel moviesPanel;

    public FriendProfilePage(String username) {

        friendUsername = username;

        setTitle("70mm - " + friendUsername + "'s Profile");
        setSize(650, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(BG);
        mainPanel.setBorder(
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        );

        JLabel title = new JLabel(
            friendUsername + "'s Profile",
            SwingConstants.CENTER
        );
        title.setFont(new Font("Arial", Font.BOLD, 26));
        title.setForeground(GOLD);
        mainPanel.add(title, BorderLayout.NORTH);

        moviesPanel = new JPanel(new GridLayout(0, 3, 10, 10));
        moviesPanel.setBackground(BG);

        JScrollPane scroll = new JScrollPane(moviesPanel);
        scroll.getViewport().setBackground(BG);
        scroll.setBorder(BorderFactory.createEmptyBorder());

        mainPanel.add(scroll, BorderLayout.CENTER);

        JButton closeButton = new JButton("Close");
        closeButton.setBackground(RED);
        closeButton.setForeground(TEXT);
        closeButton.addActionListener(e -> dispose());

        mainPanel.add(closeButton, BorderLayout.SOUTH);

        setContentPane(mainPanel);

        loadMovies();

        setVisible(true);
    }

    private void loadMovies() {

        moviesPanel.removeAll();

        String sql =
            "SELECT m.tmdb_id, m.title, d.rating " +
            "FROM diary_entries d " +
            "JOIN movies_new m ON d.tmdb_id = m.tmdb_id " +
            "JOIN mm70_users.users u ON u.user_id = d.user_id " +
            "WHERE u.username = ? " +
            "ORDER BY d.entry_id DESC LIMIT 30";

        try (Connection con = DatabaseManager.getConnection()) {

            // Check whether diary_entries contains user_id.
            DatabaseMetaData meta = con.getMetaData();

            try (ResultSet columns = meta.getColumns(
                    con.getCatalog(), null, "diary_entries", "user_id")) {

                if (!columns.next()) {
                    showMessage(
                        "The diary database needs a user_id column.\n" +
                        "Run the SQL setup shown below in MySQL Workbench."
                    );
                    return;
                }
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setString(1, friendUsername);

                try (ResultSet rs = ps.executeQuery()) {

                    int count = 0;

                    while (rs.next()) {

                        int id = rs.getInt("tmdb_id");
                        String movieTitle = rs.getString("title");
                        String rating = rs.getString("rating");

                        JPanel card = new JPanel(
                            new BorderLayout(5, 5)
                        );
                        card.setBackground(CARD);
                        card.setBorder(
                            BorderFactory.createEmptyBorder(8, 8, 8, 8)
                        );

                        JLabel poster = new JLabel(
                            "", SwingConstants.CENTER
                        );
                        poster.setPreferredSize(new Dimension(150, 180));

                        File file = new File("posters/" + id + ".jpg");

                        if (file.exists()) {

                            ImageIcon icon = new ImageIcon(
                                file.getAbsolutePath()
                            );

                            Image image = icon.getImage().getScaledInstance(
                                130, 170, Image.SCALE_SMOOTH
                            );

                            poster.setIcon(new ImageIcon(image));

                        } else {
                            poster.setText("No Poster");
                            poster.setForeground(TEXT);
                        }

                        JLabel name = new JLabel(
                            "<html><center>" +
                            escapeHtml(movieTitle) +
                            "</center></html>",
                            SwingConstants.CENTER
                        );
                        name.setForeground(GOLD);

                        JLabel score = new JLabel(
                            "Rating: " + rating + "/10",
                            SwingConstants.CENTER
                        );
                        score.setForeground(TEXT);

                        JPanel details = new JPanel(
                            new GridLayout(2, 1, 3, 3)
                        );
                        details.setBackground(CARD);
                        details.add(name);
                        details.add(score);

                        card.add(poster, BorderLayout.CENTER);
                        card.add(details, BorderLayout.SOUTH);

                        moviesPanel.add(card);
                        count++;
                    }

                    if (count == 0) {
                        showMessage(
                            "No diary movies found for " +
                            friendUsername + "."
                        );
                    }
                }
            }

        } catch (Exception ex) {

            ex.printStackTrace();

            showMessage(
                "Could not load this profile.\n" +
                "Error: " + ex.getMessage()
            );
        }

        moviesPanel.revalidate();
        moviesPanel.repaint();
    }

    private void showMessage(String message) {
        moviesPanel.removeAll();

        JLabel label = new JLabel(
            "<html><center>" + escapeHtml(message)
            .replace("\n", "<br>") +
            "</center></html>",
            SwingConstants.CENTER
        );

        label.setForeground(TEXT);
        moviesPanel.add(label);

        moviesPanel.revalidate();
        moviesPanel.repaint();
    }

    private String escapeHtml(String value) {
        if (value == null) return "";

        return value.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#39;");
    }
}
