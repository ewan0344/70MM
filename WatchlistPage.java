import javax.swing.*;
import java.awt.*;

public class WatchlistPage extends JPanel {
    public WatchlistPage(Main main) {
        setBackground(Color.WHITE);
        setLayout(new BorderLayout());

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Color.WHITE);

        JButton backButton = new JButton("←");
        JLabel title = new JLabel("Watchlist", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));

        top.add(backButton, BorderLayout.WEST);
        top.add(title, BorderLayout.CENTER);

        JPanel movies = new JPanel(new GridLayout(2, 3, 20, 20));
        movies.setBackground(Color.WHITE);
        movies.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        movies.add(new JButton("Movie 1"));
        movies.add(new JButton("Movie 2"));
        movies.add(new JButton("Movie 3"));
        movies.add(new JButton("Movie 4"));
        movies.add(new JButton("Movie 5"));
        movies.add(new JButton("Movie 6"));

        add(top, BorderLayout.NORTH);
        add(movies, BorderLayout.CENTER);

        backButton.addActionListener(e -> main.showPage("home"));
    }
}
