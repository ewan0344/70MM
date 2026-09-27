import javax.swing.*;
import java.awt.*;

public class WatchlistPage extends JPanel {
    JPanel moviePanel;

    public WatchlistPage(Main main) {
        setBackground(Color.WHITE);
        setLayout(new BorderLayout());

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Color.WHITE);
        JButton back = new JButton("←");
        JLabel title = new JLabel("Watchlist", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        JButton add = new JButton("+ Add Movie");
        top.add(back, BorderLayout.WEST);
        top.add(title, BorderLayout.CENTER);
        top.add(add, BorderLayout.EAST);

        moviePanel = new JPanel(new GridLayout(0, 3, 15, 15));
        moviePanel.setBackground(Color.WHITE);
        moviePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(moviePanel), BorderLayout.CENTER);

        back.addActionListener(e -> main.showPage("home"));
        add.addActionListener(e -> addMovie());
    }

    private void addMovie() {
        String movieName = JOptionPane.showInputDialog(
            this, "Enter movie name:", "Add Movie",
            JOptionPane.PLAIN_MESSAGE
        );

        if (movieName != null && !movieName.trim().isEmpty()) {
            JButton movie = new JButton(movieName);
            movie.setPreferredSize(new Dimension(150, 120));
            moviePanel.add(movie);
            moviePanel.revalidate();
            moviePanel.repaint();
        }
    }
}
