import javax.swing.*;
import java.awt.*;

public class Main extends JFrame {

    CardLayout cardLayout;
    JPanel mainPanel;

    // Stores the username entered on the login page
    String currentUsername = "Username";

    public Main() {

        DatabaseSetup.initialize();

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        mainPanel.add(new LoginPage(this), "login");
        mainPanel.add(new HomePage(this), "home");
        mainPanel.add(new ProfilePage(this), "profile");
        mainPanel.add(new DiaryPage(this), "diary");
        mainPanel.add(new PosterTestPage(this), "posterTest");
        mainPanel.add(new WatchlistPage(this), "watchlist");
        mainPanel.add(new ListPage(this), "list");

        add(mainPanel);

        setTitle("70mm");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        showPage("login");
    }

    // Save the username entered by the user
    public void setUsername(String username) {
        currentUsername = username;
    }

    public void showPage(String pageName) {

        if ("watchlist".equals(pageName)) {
            mainPanel.remove(getWatchlistPage());

            WatchlistPage watchlist = new WatchlistPage(this);
            mainPanel.add(watchlist, "watchlist");
        }

        cardLayout.show(mainPanel, pageName);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private Component getWatchlistPage() {
        for (Component component : mainPanel.getComponents()) {
            if (component instanceof WatchlistPage) {
                return component;
            }
        }

        return new JPanel();
    }

    public void showMovieDetails(int movieId) {

        // Remove any old movie-details page
        for (Component component : mainPanel.getComponents()) {
            if (component instanceof MovieDetailsPage) {
                mainPanel.remove(component);
            }
        }

        // Create a page for the selected movie
        String pageName = "movieDetails";

        MovieDetailsPage page = new MovieDetailsPage(this, movieId);
        mainPanel.add(page, pageName);

        cardLayout.show(mainPanel, pageName);

        mainPanel.revalidate();
        mainPanel.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Main().setVisible(true);
        });
    }
}