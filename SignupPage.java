import javax.swing.*;
import java.awt.*;

public class SignupPage extends JPanel {
    public SignupPage(Main main) {
        setBackground(Color.WHITE);
        setLayout(new GridBagLayout());
        JPanel box = new JPanel();
        box.setBackground(Color.WHITE);
        box.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        box.setLayout(new GridLayout(8, 1, 5, 5));
        box.setPreferredSize(new Dimension(300, 300));
        JLabel title = new JLabel("Sign up", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        JTextField email = new JTextField();
        JTextField username = new JTextField();
        JPasswordField password = new JPasswordField();
        JButton signupButton = new JButton("Sign up");
        JButton loginButton = new JButton("Already have an account? Log in");
        box.add(title); box.add(new JLabel("Email")); box.add(email);
        box.add(new JLabel("Username")); box.add(username);
        box.add(new JLabel("Password")); box.add(password); box.add(signupButton);
        JPanel outside = new JPanel(new BorderLayout());
        outside.setBackground(Color.WHITE);
        outside.add(box, BorderLayout.CENTER);
        outside.add(loginButton, BorderLayout.SOUTH);
        add(outside);
        signupButton.addActionListener(e -> main.showPage("home"));
        loginButton.addActionListener(e -> main.showPage("login"));
    }
}
