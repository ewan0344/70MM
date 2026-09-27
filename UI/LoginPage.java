import javax.swing.*;
import java.awt.*;

public class LoginPage extends JPanel {
    public LoginPage(Main main) {
        setBackground(Color.WHITE);
        setLayout(new GridBagLayout());
        JPanel box = new JPanel();
        box.setBackground(Color.WHITE);
        box.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        box.setLayout(new GridLayout(6, 1, 5, 5));
        box.setPreferredSize(new Dimension(300, 250));
        JLabel title = new JLabel("70mm", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        JTextField username = new JTextField();
        JPasswordField password = new JPasswordField();
        JButton loginButton = new JButton("Log in");
        JButton signupButton = new JButton("Don't have an account? Sign up");
        box.add(title); box.add(new JLabel("Username")); box.add(username);
        box.add(new JLabel("Password")); box.add(password); box.add(loginButton);
        JPanel outside = new JPanel(new BorderLayout());
        outside.setBackground(Color.WHITE);
        outside.add(box, BorderLayout.CENTER);
        outside.add(signupButton, BorderLayout.SOUTH);
        add(outside);
        loginButton.addActionListener(e -> main.showPage("home"));
        signupButton.addActionListener(e -> main.showPage("signup"));
    }
}
