import javax.swing.*;
import java.awt.*;

public class ProfilePage extends JPanel {
    public ProfilePage(Main main) {
        setBackground(Color.decode("#171415"));
        setLayout(new BorderLayout());

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Color.decode("#171415"));
        top.setBorder(BorderFactory.createLineBorder(Color.decode("#D6A36A")));
        JButton backButton = new JButton("←");
        backButton.setForeground(Color.decode("#F1E9DD"));
        backButton.setBackground(Color.decode("#A64B52"));

        JLabel title = new JLabel("Profile", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(Color.decode("#F1E9DD"));

        JLabel logo = new JLabel("70mm", SwingConstants.RIGHT);
        logo.setFont(new Font("Impact", Font.BOLD, 58));
        logo.setForeground(Color.decode("#D6A36A"));

        top.add(backButton, BorderLayout.WEST);
        top.add(title, BorderLayout.CENTER);
        top.add(logo,BorderLayout.EAST);

        JPanel info = new JPanel(new GridLayout(3, 1));
        info.setBackground(Color.decode("#171415"));

        JLabel photo = new JLabel("○", SwingConstants.CENTER);
        photo.setFont(new Font("Arial", Font.PLAIN, 55));
        photo.setForeground(Color.decode("#F1E9DD"));

        JLabel username = new JLabel("Username", SwingConstants.CENTER);
        username.setForeground(Color.decode("#F1E9DD"));
        username.setFont(new Font("Arial", Font.BOLD, 20));

        JLabel recentActivity = new JLabel("Recent Activity", SwingConstants.CENTER);
        recentActivity.setForeground(Color.decode("#F1E9DD"));

        info.add(photo);
        info.add(username);
        info.add(recentActivity);

        JPanel options = new JPanel(new GridLayout(3, 1, 10, 10));
        options.setBackground(Color.decode("#171415"));
        options.setBorder(BorderFactory.createEmptyBorder(20, 100, 30, 100));

        JButton diary = new JButton("Diary");
        diary.setBackground(Color.decode("#A64B52"));
        diary.setForeground(Color.decode("#F1E9DD"));

        JButton watchlist = new JButton("Watchlist");
        watchlist.setBackground(Color.decode("#A64B52"));
        watchlist.setForeground(Color.decode("#F1E9DD"));

        JButton list = new JButton("List");
        list.setBackground(Color.decode("#A64B52"));
        list.setForeground(Color.decode("#F1E9DD"));

        options.add(diary);
        options.add(watchlist);
        options.add(list);

        add(top, BorderLayout.NORTH);
        add(info, BorderLayout.CENTER);
        add(options, BorderLayout.SOUTH);

        backButton.addActionListener(e -> main.showPage("home"));
        diary.addActionListener(e -> main.showPage("diary"));
        watchlist.addActionListener(e -> main.showPage("watchlist"));
        list.addActionListener(e -> main.showPage("list"));
    }
}