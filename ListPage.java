import javax.swing.*;
import java.awt.*;

public class ListPage extends JPanel {
    JPanel listPanel;

    public ListPage(Main main) {
        setBackground(Color.WHITE);
        setLayout(new BorderLayout());

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Color.WHITE);
        JButton back = new JButton("←");
        JLabel title = new JLabel("Lists", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        JButton create = new JButton("+ Create List");
        top.add(back, BorderLayout.WEST);
        top.add(title, BorderLayout.CENTER);
        top.add(create, BorderLayout.EAST);

        listPanel = new JPanel();
        listPanel.setBackground(Color.WHITE);
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(listPanel), BorderLayout.CENTER);

        back.addActionListener(e -> main.showPage("home"));
        create.addActionListener(e -> createList());
    }

    private void createList() {
        String listName = JOptionPane.showInputDialog(
            this, "Enter list name:", "Create List",
            JOptionPane.PLAIN_MESSAGE
        );

        if (listName != null && !listName.trim().isEmpty()) {
            JPanel list = new JPanel(new BorderLayout());
            list.setBackground(Color.WHITE);
            list.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            list.setMaximumSize(new Dimension(600, 70));

            JLabel name = new JLabel("  " + listName);
            name.setFont(new Font("Arial", Font.BOLD, 18));
            JButton open = new JButton("Open");

            list.add(name, BorderLayout.CENTER);
            list.add(open, BorderLayout.EAST);

            listPanel.add(list);
            listPanel.add(Box.createVerticalStrut(10));
            listPanel.revalidate();
            listPanel.repaint();

            open.addActionListener(e -> JOptionPane.showMessageDialog(
                this,
                "List: " + listName + "\n\nMovie adding can be connected here later.",
                listName,
                JOptionPane.INFORMATION_MESSAGE
            ));
        }
    }
}
