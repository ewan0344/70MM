import javax.swing.*;
import java.awt.*;

public class LoginPage extends JPanel {
    public LoginPage(Main main) {

        setBackground(Color.decode("#171415"));
        setLayout(new GridBagLayout());

        JPanel box = new JPanel();
        box.setBackground(Color.decode("#242021"));
        box.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        box.setLayout(new GridLayout(6, 1, 5, 5));
        box.setPreferredSize(new Dimension(300, 300));

        JLabel title = new JLabel("70mm", SwingConstants.CENTER);
        title.setFont(new Font("Impact", Font.BOLD, 48));
        title.setForeground(Color.decode("#D6A36A"));

        JLabel usernameLabel = new JLabel("Username");
        usernameLabel.setForeground(Color.decode("#F1E9DD"));


        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setForeground(Color.decode("#F1E9DD"));

        JTextField username = new JTextField();
        JPasswordField password = new JPasswordField();

        JButton loginButton = new JButton("Log in");
        loginButton.setForeground(Color.decode("#F1E9DD"));
        loginButton.setBackground(Color.decode("#A64B52"));

        JButton signupButton = new JButton("Don't have an account? Sign up");
        signupButton.setForeground(Color.decode("#F1E9DD"));
        signupButton.setBackground(Color.decode("#A64B52"));

        box.add(title);
        box.add(usernameLabel);
        box.add(username);
        box.add(passwordLabel);
        box.add(password);
        box.add(loginButton);

        JPanel outside = new JPanel(new BorderLayout());
        outside.setBackground(Color.WHITE);

        outside.add(box, BorderLayout.CENTER);
        outside.add(signupButton, BorderLayout.SOUTH);

        add(outside);

        loginButton.addActionListener(e -> main.showPage("home"));
        signupButton.addActionListener(e -> main.showPage("signup"));
    }
}