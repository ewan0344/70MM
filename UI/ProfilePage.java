import javax.swing.*;
import java.awt.*;

public class ProfilePage extends JPanel {
    public ProfilePage(Main main) {
        setBackground(Color.WHITE);
        setLayout(new BorderLayout());

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Color.WHITE);
        JButton backButton = new JButton("←");
        JLabel title = new JLabel("Profile", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        top.add(backButton, BorderLayout.WEST);
        top.add(title, BorderLayout.CENTER);

        JPanel info = new JPanel(new GridLayout(3, 1));
        info.setBackground(Color.WHITE);
        JLabel photo = new JLabel("○", SwingConstants.CENTER);
        photo.setFont(new Font("Arial", Font.PLAIN, 55));
        JLabel username = new JLabel("Username", SwingConstants.CENTER);
        username.setFont(new Font("Arial", Font.BOLD, 20));
        info.add(photo); info.add(username);
        info.add(new JLabel("Recent Activity", SwingConstants.CENTER));

        JPanel options = new JPanel(new GridLayout(3, 1, 10, 10));
        options.setBackground(Color.WHITE);
        options.setBorder(BorderFactory.createEmptyBorder(20, 100, 30, 100));
        JButton diary = new JButton("Diary");
        JButton watchlist = new JButton("Watchlist");
        JButton list = new JButton("List");
        options.add(diary); options.add(watchlist); options.add(list);

        add(top, BorderLayout.NORTH);
        add(info, BorderLayout.CENTER);
        add(options, BorderLayout.SOUTH);

        backButton.addActionListener(e -> main.showPage("home"));
        diary.addActionListener(e -> main.showPage("diary"));
        watchlist.addActionListener(e -> main.showPage("watchlist"));
        list.addActionListener(e -> main.showPage("list"));
    }
}
