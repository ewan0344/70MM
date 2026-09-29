import javax.swing.*;
import java.awt.*;

public class HomePage extends JPanel {

    public HomePage(Main main) {

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel sidePanel = new JPanel();
        sidePanel.setBackground(Color.decode("#D6A36A"));
        sidePanel.setPreferredSize(new Dimension(180, 500));
        sidePanel.setBorder(BorderFactory.createLineBorder(Color.decode("#D6A36A")));
        sidePanel.setLayout(new GridLayout(4, 1));

        JButton profileButton = new JButton("Profile");
        profileButton.setForeground(Color.WHITE);
        
        profileButton.setBackground(Color.decode("#A64B52"));
        JButton diaryButton = new JButton("Diary");
        diaryButton.setForeground(Color.WHITE); 
        diaryButton.setBackground(Color.decode("#A64B52"));
        JButton watchlistButton = new JButton("Watchlist");
        watchlistButton.setBackground(Color.decode("#A64B52"));
        watchlistButton.setForeground(Color.WHITE); 
        JButton listButton = new JButton("List");
        listButton.setBackground(Color.decode("#A64B52"));
        listButton.setForeground(Color.WHITE); 

        sidePanel.add(profileButton);
        sidePanel.add(diaryButton);
        sidePanel.add(watchlistButton);
        sidePanel.add(listButton);

        JPanel mainArea = new JPanel(new BorderLayout());
        mainArea.setBackground(Color.decode("#171415"));

        JLabel logo = new JLabel("70mm", SwingConstants.CENTER);
        logo.setFont(new Font("Impact", Font.BOLD, 100));
        logo.setForeground(Color.decode("#D6A36A"));

        JPanel center = new JPanel();
        center.setBackground(Color.decode("#171415"));
        center.setLayout(new GridLayout(3, 1, 10, 10));

        JLabel question = new JLabel("What's on your mind?", SwingConstants.CENTER);
        question.setForeground(Color.WHITE);
        question.setFont(new Font("Arial", Font.BOLD, 30));
        JTextArea thoughtBox = new JTextArea(2,50);
        thoughtBox.setBorder(BorderFactory.createLineBorder(Color.decode("#D6A36A")));
        thoughtBox.setBackground(Color.decode("#242021"));
        thoughtBox.setForeground(Color.WHITE);
        JButton postButton = new JButton("SUBMIT");
        postButton.setForeground(Color.WHITE); 
        postButton.setBackground(Color.decode("#A64B52"));


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
