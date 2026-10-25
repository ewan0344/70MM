
import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class LoginPage extends JPanel {

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "42";

    public LoginPage(Main main) {

        setOpaque(false);
        setLayout(new GridBagLayout());

        JPanel box = new JPanel();
        box.setBackground(Color.decode("#242021"));
        box.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        box.setLayout(new GridLayout(7, 1, 5, 5));
        box.setPreferredSize(new Dimension(300, 340));

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

        JButton createButton = new JButton("Create Account");
        createButton.setForeground(Color.decode("#F1E9DD"));
        createButton.setBackground(Color.decode("#A64B52"));

        box.add(title);
        box.add(usernameLabel);
        box.add(username);
        box.add(passwordLabel);
        box.add(password);
        box.add(loginButton);
        box.add(createButton);

        JPanel outside = new JPanel(new BorderLayout());
        outside.setOpaque(false);
        outside.add(box, BorderLayout.CENTER);
        add(outside);

        createButton.addActionListener(e ->
            new CreateAccountDialog(main).setVisible(true)
        );

        loginButton.addActionListener(e -> {

            String name = username.getText().trim();
            char[] pass = password.getPassword();

            try {
                if (name.isEmpty() || pass.length == 0) {
                    JOptionPane.showMessageDialog(
                        this, "Enter your username and password."
                    );
                    return;
                }

                // Admin login is checked before normal-user login.
                if (name.equalsIgnoreCase(ADMIN_USERNAME)) {

                    String enteredPassword = new String(pass);

                    if (ADMIN_PASSWORD.equals(enteredPassword)) {
                        main.setUsername(ADMIN_USERNAME);
                        main.showPage("admin");
                    } else {
                        JOptionPane.showMessageDialog(
                            this, "Incorrect admin password."
                        );
                    }

                    return;
                }

                // Normal users continue using the database.
                if (UserDatabase.login(name, pass)) {
                    main.setUsername(name);
                    main.showPage("home");
                } else {
                    JOptionPane.showMessageDialog(
                        this, "Incorrect username or password."
                    );
                }

            } catch (Exception ex) {
                ex.printStackTrace();

                JOptionPane.showMessageDialog(
                    this,
                    "Login failed. Check your MySQL connection.\n"
                    + ex.getMessage()
                );

            } finally {
                Arrays.fill(pass, '\0');
                password.setText("");
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        ImageIcon img = new ImageIcon("UI/bgreel.png");

        g.drawImage(
            img.getImage(),
            0, 0, getWidth(), getHeight(), this
        );
    }
}
