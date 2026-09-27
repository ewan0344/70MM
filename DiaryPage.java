import javax.swing.*;
import java.awt.*;

public class DiaryPage extends JPanel {
    JPanel moviePanel;

    public DiaryPage(Main main) {
        setBackground(Color.WHITE);
        setLayout(new BorderLayout());

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Color.WHITE);
        JButton back = new JButton("←");
        JLabel title = new JLabel("Diary", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        JButton add = new JButton("+ Add Movie");
        top.add(back, BorderLayout.WEST);
        top.add(title, BorderLayout.CENTER);
        top.add(add, BorderLayout.EAST);

        moviePanel = new JPanel();
        moviePanel.setBackground(Color.WHITE);
        moviePanel.setLayout(new BoxLayout(moviePanel, BoxLayout.Y_AXIS));
        moviePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(moviePanel), BorderLayout.CENTER);

        back.addActionListener(e -> main.showPage("home"));
        add.addActionListener(e -> addMovie());
    }

    private void addMovie() {
        JTextField name = new JTextField();
        JTextArea review = new JTextArea(5, 20);

        JPanel panel = new JPanel(new GridLayout(4, 1, 5, 5));
        panel.add(new JLabel("Movie name:"));
        panel.add(name);
        panel.add(new JLabel("Your review:"));
        panel.add(new JScrollPane(review));

        int result = JOptionPane.showConfirmDialog(
            this, panel, "Add Movie to Diary",
            JOptionPane.OK_CANCEL_OPTION
        );

        if (result == JOptionPane.OK_OPTION && !name.getText().trim().isEmpty()) {
            JPanel movie = new JPanel(new BorderLayout());
            movie.setBackground(Color.WHITE);
            movie.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            movie.setMaximumSize(new Dimension(600, 120));

            JLabel movieName = new JLabel(name.getText());
            movieName.setFont(new Font("Arial", Font.BOLD, 18));

            JTextArea reviewText = new JTextArea(review.getText());
            reviewText.setEditable(false);
            reviewText.setLineWrap(true);
            reviewText.setWrapStyleWord(true);
            reviewText.setBackground(Color.WHITE);

            movie.add(movieName, BorderLayout.NORTH);
            movie.add(reviewText, BorderLayout.CENTER);

            moviePanel.add(movie);
            moviePanel.add(Box.createVerticalStrut(10));
            moviePanel.revalidate();
            moviePanel.repaint();
        }
    }
}
