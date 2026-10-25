import javax.swing.*;
import java.awt.*;

public class Main extends JFrame {

    private final CardLayout cardLayout;
    private final JPanel mainPanel;
    private String currentUsername = "Username";

    public Main() {
        DatabaseSetup.initialize();

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        mainPanel.add(new LoginPage(this), "login");
        mainPanel.add(new HomePage(this), "home");
        mainPanel.add(new ProfilePage(this), "profile");
        mainPanel.add(new DiaryPage(this), "diary");
        mainPanel.add(new FavouritesPage(this), "favourites");
        mainPanel.add(new PosterTestPage(this), "posterTest");
        mainPanel.add(new WatchlistPage(this), "watchlist");
        mainPanel.add(new ListPage(this), "list");
        mainPanel.add(new AdminDashboard(this), "admin");

        add(mainPanel);
        setTitle("70mm");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        showPage("login");
    }

    public void setUsername(String username) {
        if (username != null && !username.trim().isEmpty()) {
            currentUsername = username.trim();
        }
    }

    public String getUsername() {
        return currentUsername;
    }

    public void showPage(String pageName) {
        if ("watchlist".equals(pageName)) {
            replacePage("watchlist", new WatchlistPage(this));
        } else if ("favourites".equals(pageName)) {
            replacePage("favourites", new FavouritesPage(this));
        } else if ("admin".equals(pageName)) {
            for (Component component : mainPanel.getComponents()) {
                if (component instanceof AdminDashboard) {
                    ((AdminDashboard) component).refreshStats();
                    break;
                }
            }
        }

        cardLayout.show(mainPanel, pageName);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private void replacePage(String pageName, Component newPage) {
        Component oldPage = getPageComponent(pageName);
        if (oldPage != null) mainPanel.remove(oldPage);
        mainPanel.add(newPage, pageName);
    }

    private Component getPageComponent(String pageName) {
        for (Component component : mainPanel.getComponents()) {
            if ("watchlist".equals(pageName) && component instanceof WatchlistPage) return component;
            if ("favourites".equals(pageName) && component instanceof FavouritesPage) return component;
        }
        return null;
    }

    public void showMovieDetails(int movieId) {
        for (Component component : mainPanel.getComponents()) {
            if (component instanceof MovieDetailsPage) {
                mainPanel.remove(component);
                break;
            }
        }

        mainPanel.add(new MovieDetailsPage(this, movieId), "movieDetails");
        cardLayout.show(mainPanel, "movieDetails");
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    public void openFriendsWindow() {
        if (currentUsername == null || currentUsername.trim().isEmpty()
                || "Username".equals(currentUsername)) {
            JOptionPane.showMessageDialog(this, "Please log in before opening Friends.");
            return;
        }

        try {
            UserDatabase.setup();
            new FriendsPage(this, currentUsername);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Could not open Friends:\n" + ex.getMessage(),
                    "Friends Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Main app = new Main();
            app.setVisible(true);
        });
    }
}
