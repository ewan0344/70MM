import javax.swing.*;
import java.awt.*;

public class ListPage extends JPanel {
    public ListPage(Main main) {
        setBackground(Color.WHITE);
        setLayout(new BorderLayout());

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Color.WHITE);

        JButton backButton = new JButton("←");
        JLabel title = new JLabel("Lists", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));

        top.add(backButton, BorderLayout.WEST);
        top.add(title, BorderLayout.CENTER);

        JPanel listPanel = new JPanel();
        listPanel.setBackground(Color.WHITE);
        listPanel.setLayout(new GridLayout(4, 1, 10, 10));
        listPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JLabel listName = new JLabel("List name");
        JLabel movieList = new JLabel("☐  M  A  L  A  Y  A  L  A  M");
        JLabel createLabel = new JLabel("Create new list");
        JTextField newList = new JTextField();

        listPanel.add(listName);
        listPanel.add(movieList);
        listPanel.add(createLabel);
        listPanel.add(newList);

        add(top, BorderLayout.NORTH);
        add(listPanel, BorderLayout.CENTER);

        backButton.addActionListener(e -> main.showPage("home"));
    }
}
