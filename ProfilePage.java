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

        JPanel profileInfo = new JPanel();
        profileInfo.setBackground(Color.WHITE);
        profileInfo.setLayout(new GridLayout(3, 1, 5, 5));

        JLabel profilePhoto = new JLabel("○", SwingConstants.CENTER);
        profilePhoto.setFont(new Font("Arial", Font.PLAIN, 55));

        JLabel username = new JLabel("Username", SwingConstants.CENTER);
        username.setFont(new Font("Arial", Font.BOLD, 20));

        JLabel recentActivity = new JLabel("Recent Activity", SwingConstants.CENTER);

        profileInfo.add(profilePhoto);
        profileInfo.add(username);
        profileInfo.add(recentActivity);

        JPanel options = new JPanel(new GridLayout(3, 1, 10, 10));
        options.setBackground(Color.WHITE);
        options.setBorder(BorderFactory.createEmptyBorder(20, 100, 30, 100));

        JButton diaryButton = new JButton("Diary");
        JButton watchlistButton = new JButton("Watchlist");
        JButton listButton = new JButton("List");

        options.add(diaryButton);
        options.add(watchlistButton);
        options.add(listButton);

        add(top, BorderLayout.NORTH);
        add(profileInfo, BorderLayout.CENTER);
        add(options, BorderLayout.SOUTH);

        backButton.addActionListener(e -> main.showPage("home"));
        diaryButton.addActionListener(e -> main.showPage("diary"));
        watchlistButton.addActionListener(e -> main.showPage("watchlist"));
        listButton.addActionListener(e -> main.showPage("list"));
    }
}
