
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
        currentUsername = username;
    }

    public String getUsername() {
        return currentUsername;
    }

    public void showPage(String pageName) {

        if ("watchlist".equals(pageName)) {
            Component oldPage = getPageComponent("watchlist");

            if (oldPage != null) {
                mainPanel.remove(oldPage);
            }

            mainPanel.add(new WatchlistPage(this), "watchlist");
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

    private Component getPageComponent(String pageName) {
        for (Component component : mainPanel.getComponents()) {
            if (component instanceof WatchlistPage) {
                return component;
            }
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

        String pageName = "movieDetails";

        MovieDetailsPage page = new MovieDetailsPage(this, movieId);
        mainPanel.add(page, pageName);

        cardLayout.show(mainPanel, pageName);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Main app = new Main();
            app.setVisible(true);
        });
    }
}
