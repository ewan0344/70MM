import javax.swing.*;
import java.awt.*;

public class DiaryPage extends JPanel {
    public DiaryPage(Main main) {
        setBackground(Color.WHITE);
        setLayout(new BorderLayout());

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Color.WHITE);

        JButton backButton = new JButton("←");
        JLabel title = new JLabel("Diary", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));

        top.add(backButton, BorderLayout.WEST);
        top.add(title, BorderLayout.CENTER);

        JPanel monthPanel = new JPanel();
        monthPanel.setBackground(Color.WHITE);

        JLabel monthLabel = new JLabel("Month");
        JComboBox<String> month = new JComboBox<>(new String[] {
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        });
        JLabel yearLabel = new JLabel("Year");
        JTextField year = new JTextField("2026", 5);

        monthPanel.add(monthLabel);
        monthPanel.add(month);
        monthPanel.add(yearLabel);
        monthPanel.add(year);

        JPanel movies = new JPanel(new GridLayout(5, 2, 10, 10));
        movies.setBackground(Color.WHITE);
        movies.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        movies.add(new JLabel("Film name"));
        movies.add(new JLabel("Date / Rating"));
        movies.add(new JLabel("Movie 1"));
        movies.add(new JLabel("27 Sep    ★★★★"));
        movies.add(new JLabel("Movie 2"));
        movies.add(new JLabel("25 Sep    ★★★"));
        movies.add(new JLabel("Movie 3"));
        movies.add(new JLabel("20 Sep    ★★★★★"));
        movies.add(new JLabel("Movie 4"));
        movies.add(new JLabel("18 Sep    ★★★★"));

        add(top, BorderLayout.NORTH);
        add(monthPanel, BorderLayout.CENTER);
        add(movies, BorderLayout.SOUTH);

        backButton.addActionListener(e -> main.showPage("home"));
    }
}
