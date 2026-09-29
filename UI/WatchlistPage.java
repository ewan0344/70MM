import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.*;
public class WatchlistPage extends JPanel {
    private JPanel watchedPanel, wantToWatchPanel;
    private ArrayList<Movie> watchedMovies = new ArrayList<>();
    private ArrayList<Movie> wantToWatchMovies = new ArrayList<>();
    private Map<String, String> posterDatabase = new HashMap<>();
    private static class Movie {
        String name, imageURL;
        Movie(String name, String imageURL) {
            this.name = name;
            this.imageURL = imageURL;
        }
    }
    public WatchlistPage(Main main) {
        setLayout(new BorderLayout());
        setBackground(new Color(35, 20, 55));
        createPosterDatabase();
        watchedMovies.add(createMovie("Avatar"));
        watchedMovies.add(createMovie("Interstellar"));
        watchedMovies.add(createMovie("Inception"));
        wantToWatchMovies.add(createMovie("Avengers: Endgame"));
        wantToWatchMovies.add(createMovie("Spider-Man"));
        wantToWatchMovies.add(createMovie("Harry Potter"));
        // Top Bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(48, 25, 70));
        topBar.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        JButton back = new JButton("←");
        styleButton(back, new Color(255, 105, 180));
        JLabel title = new JLabel("My Watchlist", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        JButton add = new JButton("+ Add Movie");
        styleButton(add, new Color(255, 140, 70));
        topBar.add(back, BorderLayout.WEST);
        topBar.add(title, BorderLayout.CENTER);
        topBar.add(add, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);
        // Main Content
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(new Color(35, 20, 55));
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 30, 20));
        JLabel watchedTitle = sectionTitle("✓  Watched Movies",
                new Color(255, 190, 230));
        content.add(watchedTitle);
        content.add(Box.createVerticalStrut(12));
        watchedPanel = moviePanel();
        content.add(watchedPanel);
        content.add(Box.createVerticalStrut(35));
        JLabel wantTitle = sectionTitle("♡  Want to Watch",
                new Color(255, 210, 120));
        content.add(wantTitle);
        content.add(Box.createVerticalStrut(12));
        wantToWatchPanel = moviePanel();
        content.add(wantToWatchPanel);
        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
        back.addActionListener(e -> main.showPage("home"));
        add.addActionListener(e -> addNewMovie());
        refreshMovies();
    }
    private JLabel sectionTitle(String text, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 22));
        label.setForeground(color);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }
    private JPanel moviePanel() {
        JPanel panel = new JPanel(new GridLayout(0, 3, 18, 18));
        panel.setBackground(new Color(35, 20, 55));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return panel;
    }
    private void createPosterDatabase() {
        posterDatabase.put("avatar",
            "https://image.tmdb.org/t/p/w500/kyeqWdyUXW608qlYkRqosgbbJyK.jpg");
        posterDatabase.put("interstellar",
            "https://image.tmdb.org/t/p/w500/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg");
        posterDatabase.put("inception",
            "https://image.tmdb.org/t/p/w500/oYuLEt3zVCKq57qu2F8dT7NIa6f.jpg");
        posterDatabase.put("avengers: endgame",
            "https://image.tmdb.org/t/p/w500/or06FN3Dka5tukK1e9sl16pB3iy.jpg");
        posterDatabase.put("spider-man",
            "https://image.tmdb.org/t/p/w500/1g0dhYtq4irTY1GPXvft6k4YLjm.jpg");
        posterDatabase.put("harry potter",
            "https://image.tmdb.org/t/p/w500/1DV1E7SxW9N8qQ8mXKX1Y7w5Q7r.jpg");
        posterDatabase.put("titanic",
            "https://image.tmdb.org/t/p/w500/9xjZS2rlVxm8SFx8kPC3aIGCOYQ.jpg");
        posterDatabase.put("batman",
            "https://image.tmdb.org/t/p/w500/74xTEgt7R36Fpooo50r9T25onhq.jpg");
        posterDatabase.put("joker",
            "https://image.tmdb.org/t/p/w500/udDclJoHjfjb8Ekgsd4FDteOkCU.jpg");
        posterDatabase.put("barbie",
            "https://image.tmdb.org/t/p/w500/iuFNMS8U5cb6xfzi51Dbkovj7vM.jpg");
    }
    private Movie createMovie(String name) {
        return new Movie(name, posterDatabase.get(name.toLowerCase().trim()));
    }
    private void addNewMovie() {
        String name = JOptionPane.showInputDialog(this,
                "Enter the movie you want to watch:",
                "Add Movie", JOptionPane.PLAIN_MESSAGE);
        if (name == null || name.trim().isEmpty()) return;
        name = name.trim();
        if (movieAlreadyExists(name)) {
            JOptionPane.showMessageDialog(this,
                    "This movie is already in your watchlist!",
                    "Movie Already Added",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Movie movie = createMovie(name);
        wantToWatchMovies.add(movie);
        refreshMovies();
        if (movie.imageURL == null || movie.imageURL.isEmpty())
            JOptionPane.showMessageDialog(this,
                    "Movie added!\n\nPoster is not available in the built-in poster list.",
                    "Movie Added",
                    JOptionPane.INFORMATION_MESSAGE);
    }
    private boolean movieAlreadyExists(String name) {
        for (Movie m : watchedMovies)
            if (m.name.equalsIgnoreCase(name)) return true;
        for (Movie m : wantToWatchMovies)
            if (m.name.equalsIgnoreCase(name)) return true;
        return false;
    }
    private void refreshMovies() {
        watchedPanel.removeAll();
        wantToWatchPanel.removeAll();
        for (Movie m : watchedMovies)
            watchedPanel.add(createWatchedCard(m));
        for (Movie m : wantToWatchMovies)
            wantToWatchPanel.add(createWantToWatchCard(m));
        watchedPanel.revalidate();
        watchedPanel.repaint();
        wantToWatchPanel.revalidate();
        wantToWatchPanel.repaint();
    }
    private JPanel createWatchedCard(Movie movie) {
        JPanel card = createCard();
        card.add(createPoster(movie.name, movie.imageURL), BorderLayout.NORTH);
        JPanel info = new JPanel(new BorderLayout());
        info.setBackground(new Color(55, 30, 75));
        info.setBorder(BorderFactory.createEmptyBorder(8, 5, 8, 5));
        JLabel name = new JLabel(movie.name, SwingConstants.CENTER);
        name.setFont(new Font("Arial", Font.BOLD, 16));
        name.setForeground(Color.WHITE);
        JButton remove = new JButton("Remove");
        styleButton(remove, new Color(220, 70, 100));
        remove.addActionListener(e -> {
            watchedMovies.remove(movie);
            refreshMovies();
        });
        info.add(name, BorderLayout.CENTER);
        info.add(remove, BorderLayout.SOUTH);
        card.add(info, BorderLayout.CENTER);
        return card;
    }
    private JPanel createWantToWatchCard(Movie movie) {
        JPanel card = createCard();
        card.add(createPoster(movie.name, movie.imageURL), BorderLayout.NORTH);
        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setBackground(new Color(55, 30, 75));
        info.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JLabel name = new JLabel(movie.name);
        name.setFont(new Font("Arial", Font.BOLD, 16));
        name.setForeground(Color.WHITE);
        name.setAlignmentX(Component.CENTER_ALIGNMENT);
        JButton watched = new JButton("✓ Mark as Watched");
        styleButton(watched, new Color(80, 200, 150));
        watched.setAlignmentX(Component.CENTER_ALIGNMENT);
        watched.addActionListener(e -> {
            wantToWatchMovies.remove(movie);
            watchedMovies.add(movie);
            refreshMovies();
        });
        JButton remove = new JButton("Remove");
        styleButton(remove, new Color(220, 70, 100));
        remove.setAlignmentX(Component.CENTER_ALIGNMENT);
        remove.addActionListener(e -> {
            wantToWatchMovies.remove(movie);
            refreshMovies();
        });
        info.add(name);
        info.add(Box.createVerticalStrut(8));
        info.add(watched);
        info.add(Box.createVerticalStrut(5));
        info.add(remove);
        card.add(info, BorderLayout.CENTER);
        return card;
    }
    private JPanel createCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(55, 30, 75));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(150, 80, 180), 2),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        return card;
    }
    private JLabel createPoster(String name, String url) {
        JLabel poster = new JLabel(name, SwingConstants.CENTER);
        poster.setPreferredSize(new Dimension(170, 230));
        poster.setOpaque(true);
        poster.setBackground(new Color(90, 50, 110));
        poster.setForeground(Color.WHITE);
        poster.setFont(new Font("Arial", Font.BOLD, 14));
        if (url != null && !url.isEmpty()) {
            try {
                Image image = new ImageIcon(new URL(url)).getImage()
                        .getScaledInstance(170, 230, Image.SCALE_SMOOTH);
                poster.setText("");
                poster.setIcon(new ImageIcon(image));
            } catch (Exception e) {
                // Movie name remains as fallback
            }
        }
        return poster;
    }
    private void styleButton(JButton button, Color color) {
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 13));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}