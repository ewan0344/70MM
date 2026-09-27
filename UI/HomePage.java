import javax.swing.*;
import java.awt.*;

public class HomePage extends JPanel {

    public HomePage(Main main) {

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel sidePanel = new JPanel();
        sidePanel.setBackground(Color.WHITE);
        sidePanel.setPreferredSize(new Dimension(180, 500));
        sidePanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        sidePanel.setLayout(new GridLayout(5, 1));

        JButton profileButton = new JButton("○   Profile");
        profileButton.setHorizontalAlignment(SwingConstants.LEFT);

        JButton diaryButton = new JButton("Diary");
        diaryButton.setBackground(Color.decode("#aed6b0"));
        JButton watchlistButton = new JButton("Watchlist");
        watchlistButton.setBackground(Color.decode("#aed6b0"));
        JButton listButton = new JButton("List");
        listButton.setBackground(Color.decode("#aed6b0"));

        sidePanel.add(profileButton);
        sidePanel.add(diaryButton);
        sidePanel.add(watchlistButton);
        sidePanel.add(listButton);

        JPanel mainArea = new JPanel(new BorderLayout());
        mainArea.setBackground(Color.WHITE);

        JLabel logo = new JLabel("70mm", SwingConstants.CENTER);
        logo.setFont(new Font("Impact", Font.BOLD, 100));

        JPanel center = new JPanel();
        center.setBackground(Color.WHITE);
        center.setLayout(new GridLayout(3, 1, 10, 10));

        JLabel question = new JLabel("What's on your mind?", SwingConstants.CENTER);
        question.setFont(new Font("Arial", Font.BOLD, 30));
        JTextArea thoughtBox = new JTextArea(2,50);
         thoughtBox.setBackground(Color.decode("#d1cfc9"));

        JButton postButton = new JButton("^");

        center.add(question);
        center.add(thoughtBox);
        center.add(postButton);

        mainArea.add(logo, BorderLayout.NORTH);
        mainArea.add(center, BorderLayout.CENTER);

        add(sidePanel, BorderLayout.WEST);
        add(mainArea, BorderLayout.CENTER);

        profileButton.addActionListener(e -> main.showPage("profile"));
        diaryButton.addActionListener(e -> main.showPage("diary"));
        watchlistButton.addActionListener(e -> main.showPage("watchlist"));
        listButton.addActionListener(e -> main.showPage("list"));
    }
}
