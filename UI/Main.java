
import javax.swing.*;
import java.awt.*;

public class Main extends JFrame {

    CardLayout cardLayout;
    JPanel mainPanel;

    String currentUsername = "Username";

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

    // Store the logged-in username.
    public void setUsername(String username) {
        currentUsername = username;
    }

    public String getUsername() {
        return currentUsername;
    }

    // Navigate between application pages.
    public void showPage(String pageName) {

        if ("watchlist".equals(pageName)) {
            Component oldPage = getPageComponent("watchlist");

            if (oldPage != null) {
                mainPanel.remove(oldPage);
            }

            mainPanel.add(new WatchlistPage(this), "watchlist");
        }

        if ("favourites".equals(pageName)) {
            Component oldPage = getPageComponent("favourites");

            if (oldPage != null) {
                mainPanel.remove(oldPage);
            }

            mainPanel.add(new FavouritesPage(this), "favourites");
        }

        if ("admin".equals(pageName)) {
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

    // Find an existing page by its type.
    private Component getPageComponent(String pageName) {
        for (Component component : mainPanel.getComponents()) {

            if ("watchlist".equals(pageName)
                    && component instanceof WatchlistPage) {
                return component;
            }

            if ("favourites".equals(pageName)
                    && component instanceof FavouritesPage) {
                return component;
            }
        }

        return null;
    }

    // Open a movie's details page.
    public void showMovieDetails(int movieId) {

        for (Component component : mainPanel.getComponents()) {
            if (component instanceof MovieDetailsPage) {
                mainPanel.remove(component);
                break;
            }
        }

        String pageName = "movieDetails";

        MovieDetailsPage page = new MovieDetailsPage(this, movieId);
        mainPanel.add(page, pageName);

        cardLayout.show(mainPanel, pageName);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    // Open the Friends window.
    public void openFriendsWindow() {
        try {
            UserDatabase.setup();
            new FriendsPage(currentUsername);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                this,
                "Could not open Friends: " + ex.getMessage()
            );
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Main app = new Main();
            app.setVisible(true);
        });
    }
}
