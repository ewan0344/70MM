import javax.swing.*;
import java.awt.*;
import java.util.*;
public class WatchlistPage extends JPanel {
    JPanel watched, wanted;
    ArrayList<Movie> watchedList = new ArrayList<>();
    ArrayList<Movie> wantedList = new ArrayList<>();
    // 70MM theme colors
    Color background = new Color(20, 18, 19);
    Color red = new Color(175, 78, 85);
    Color gold = new Color(225, 170, 100);
    Color darkCard = new Color(35, 31, 32);
    Color white = Color.WHITE;
    class Movie {
        String name;
        Movie(String name) {
            this.name = name;
        }
    }
    public WatchlistPage(Main main) {
        setLayout(new BorderLayout());
        setBackground(background);
        // Starting movies
        watchedList.add(new Movie("Avatar"));
        watchedList.add(new Movie("Interstellar"));
        watchedList.add(new Movie("Inception"));
        wantedList.add(new Movie("Titanic"));
        wantedList.add(new Movie("Joker"));
        wantedList.add(new Movie("Barbie"));
        // Top bar
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(background);
        top.setBorder(
                BorderFactory.createMatteBorder(
                        0, 0, 1, 0, gold
                )
        );
        JButton back = new JButton("←");
        JButton add = new JButton("+ Add Movie");
        JLabel title = new JLabel(
                "My Watchlist",
                SwingConstants.CENTER
        );
        title.setFont(
                new Font("Arial", Font.BOLD, 26)
        );
        title.setForeground(gold);
        style(back, red);
        style(add, red);
        top.add(back, BorderLayout.WEST);
        top.add(title, BorderLayout.CENTER);
        top.add(add, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);
        // Main content
        JPanel content = new JPanel();
        content.setLayout(
                new BoxLayout(content, BoxLayout.Y_AXIS)
        );
        content.setBackground(background);
        content.setBorder(
                BorderFactory.createEmptyBorder(
                        20, 20, 20, 20
                )
        );
        JLabel w1 = heading("✓  Watched Movies");
        watched = grid();
        JLabel w2 = heading("♡  Want to Watch");
        wanted = grid();
        content.add(w1);
        content.add(watched);
        content.add(
                Box.createVerticalStrut(30)
        );
        content.add(w2);
        content.add(wanted);
        add(
                new JScrollPane(content),
                BorderLayout.CENTER
        );
        // Actions
        back.addActionListener(
                e -> main.showPage("home")
        );
        add.addActionListener(
                e -> addMovie()
        );
        refresh();
    }
    JPanel grid() {
        JPanel p = new JPanel(
                new GridLayout(0, 3, 15, 15)
        );
        p.setBackground(background);
        return p;
    }
    JLabel heading(String text) {
        JLabel l = new JLabel(text);
        l.setFont(
                new Font("Arial", Font.BOLD, 21)
        );
        l.setForeground(gold);
        return l;
    }
    void addMovie() {
        String name = JOptionPane.showInputDialog(
                this,
                "Enter movie name:"
        );
        if (name != null &&
                !name.trim().isEmpty()) {
            wantedList.add(
                    new Movie(name.trim())
            );
            refresh();
        }
    }
    void refresh() {
        watched.removeAll();
        wanted.removeAll();
        for (Movie m : watchedList)
            watched.add(card(m, true));
        for (Movie m : wantedList)
            wanted.add(card(m, false));
        watched.revalidate();
        watched.repaint();
        wanted.revalidate();
        wanted.repaint();
    }
    JPanel card(Movie m, boolean isWatched) {
        JPanel card = new JPanel(
                new BorderLayout()
        );
        card.setBackground(darkCard);
        card.setBorder(
                BorderFactory.createLineBorder(
                        new Color(125, 95, 60),
                        1
                )
        );
        // Movie name
        JLabel movieName = new JLabel(
                m.name,
                SwingConstants.CENTER
        );
        movieName.setPreferredSize(
                new Dimension(160, 100)
        );
        movieName.setFont(
                new Font("Arial", Font.BOLD, 16)
        );
        movieName.setForeground(white);
        movieName.setOpaque(true);
        movieName.setBackground(darkCard);
        card.add(
                movieName,
                BorderLayout.CENTER
        );
        // Buttons
        JPanel buttons = new JPanel();
        buttons.setBackground(darkCard);
        if (!isWatched) {

            JButton watch = new JButton(
                    "✓ Watched"
            );
            style(watch, red);
            watch.addActionListener(e -> {
                wantedList.remove(m);
                watchedList.add(m);
                refresh();
            });
            buttons.add(watch);
        }
        JButton remove = new JButton(
                "Remove"
        );
        style(remove, red);
        remove.addActionListener(e -> {
            if (isWatched)
                watchedList.remove(m);
            else
                wantedList.remove(m);
            refresh();
        });
        buttons.add(remove);
        card.add(
                buttons,
                BorderLayout.SOUTH
        );
        return card;
    }
    void style(JButton b, Color c) {
        b.setBackground(c);
        b.setForeground(white);
        b.setFont(
                new Font("Arial", Font.BOLD, 13)
        );
        b.setFocusPainted(false);
    }
}