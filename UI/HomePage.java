import javax.swing.*;
import java.awt.*;
import java.util.List;

public class HomePage extends JPanel {

    private final Main main;
    private JTextArea thoughtBox;
    private JButton postButton;

    public HomePage(Main main) {
        this.main = main;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel sidePanel = new JPanel();
        sidePanel.setBackground(Color.decode("#D6A36A"));
        sidePanel.setPreferredSize(new Dimension(180, 500));
        sidePanel.setBorder(BorderFactory.createLineBorder(
                Color.decode("#D6A36A")));
        sidePanel.setLayout(new GridLayout(4, 1));

        JButton profileButton = createSideButton("Profile");
        JButton diaryButton = createSideButton("Diary");
        JButton watchlistButton = createSideButton("Watchlist");
        JButton listButton = createSideButton("List");

        sidePanel.add(profileButton);
        sidePanel.add(diaryButton);
        sidePanel.add(watchlistButton);
        sidePanel.add(listButton);

        JPanel mainArea = new JPanel(new BorderLayout());
        mainArea.setBackground(Color.decode("#171415"));

        JLabel logo = new JLabel("70mm", SwingConstants.CENTER);
        logo.setFont(new Font("Impact", Font.BOLD, 100));
        logo.setForeground(Color.decode("#D6A36A"));

        JPanel center = new JPanel(new GridLayout(3, 1, 10, 10));
        center.setBackground(Color.decode("#171415"));
        center.setBorder(BorderFactory.createEmptyBorder(
                20, 15, 20, 15));

        JLabel question = new JLabel(
                "What's on your mind?", SwingConstants.CENTER);
        question.setForeground(Color.WHITE);
        question.setFont(new Font("Arial", Font.BOLD, 30));

        thoughtBox = new JTextArea(2, 50);
        thoughtBox.setLineWrap(true);
        thoughtBox.setWrapStyleWord(true);
        thoughtBox.setBorder(BorderFactory.createLineBorder(
                Color.decode("#D6A36A")));
        thoughtBox.setBackground(Color.decode("#242021"));
        thoughtBox.setForeground(Color.WHITE);
        thoughtBox.setCaretColor(Color.WHITE);
        thoughtBox.setFont(new Font("Arial", Font.PLAIN, 18));

        JScrollPane thoughtScroll = new JScrollPane(thoughtBox);
        thoughtScroll.setBorder(BorderFactory.createLineBorder(
                Color.decode("#D6A36A")));

        postButton = new JButton("SUBMIT");
        postButton.setForeground(Color.WHITE);
        postButton.setBackground(Color.decode("#A64B52"));

        center.add(question);
        center.add(thoughtScroll);
        center.add(postButton);

        mainArea.add(logo, BorderLayout.NORTH);
        mainArea.add(center, BorderLayout.CENTER);

        add(sidePanel, BorderLayout.WEST);
        add(mainArea, BorderLayout.CENTER);

        profileButton.addActionListener(
                e -> main.showPage("profile"));
        diaryButton.addActionListener(
                e -> main.showPage("diary"));
        watchlistButton.addActionListener(
                e -> main.showPage("watchlist"));
        listButton.addActionListener(
                e -> main.showPage("list"));

        postButton.addActionListener(e -> submitThought());
    }

    private JButton createSideButton(String text) {
        JButton button = new JButton(text);
        button.setForeground(Color.WHITE);
        button.setBackground(Color.decode("#A64B52"));
        return button;
    }

    private void submitThought() {
        String input = thoughtBox.getText().trim();

        if (input.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Tell us what's on your mind first!",
                    "70mm",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        postButton.setEnabled(false);
        postButton.setText("FINDING MOVIES...");

        new SwingWorker<List<RecommendedMovie>, Void>() {

            @Override
            protected List<RecommendedMovie> doInBackground()
                    throws Exception {
                RecommendationEngine engine =
                        new RecommendationEngine();

                return engine.recommend(input);
            }

            @Override
            protected void done() {
                postButton.setEnabled(true);
                postButton.setText("SUBMIT");

                try {
                    List<RecommendedMovie> movies = get();

                    if (movies.isEmpty()) {
                        JOptionPane.showMessageDialog(
                                HomePage.this,
                                "No matching movies were found. "
                                + "Check whether your movies_new table "
                                + "contains suitable movies.",
                                "70mm",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                        return;
                    }

                    Window owner =
                            SwingUtilities.getWindowAncestor(
                                    HomePage.this);

                    RecommendationPopup popup =
                            new RecommendationPopup(main,owner, movies);

                    popup.setVisible(true);

                } catch (Exception ex) {
                    Throwable cause = ex.getCause();

                    String message = cause != null
                            ? cause.getMessage()
                            : ex.getMessage();

                    JOptionPane.showMessageDialog(
                            HomePage.this,
                            "Could not load recommendations.\n"
                            + message,
                            "Recommendation Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        }.execute();
    }
}