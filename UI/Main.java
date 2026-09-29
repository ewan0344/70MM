import javax.swing.*;
import java.awt.*;

public class Main extends JFrame {

    CardLayout cardLayout;
    JPanel mainPanel;

    public Main() {

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        mainPanel.add(new LoginPage(this), "login");
        mainPanel.add(new SignupPage(this), "signup");
        mainPanel.add(new HomePage(this), "home");
        mainPanel.add(new ProfilePage(this), "profile");
        mainPanel.add(new DiaryPage(this), "diary");
        mainPanel.add(new WatchlistPage(this), "watchlist");
        mainPanel.add(new ListPage(this), "list");

        add(mainPanel);

        setTitle("70mm");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        showPage("login");
    }

    public void showPage(String pageName) {
        cardLayout.show(mainPanel, pageName);
    }

    public static void main(String[] args) {
        new Main().setVisible(true);
    }
}