
import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class CreateAccountDialog extends JDialog {

    public CreateAccountDialog(Main main) {
        super(main, "Create 70mm Account", true);

        setSize(350, 350);
        setLocationRelativeTo(main);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(Color.decode("#171415"));
        setLayout(new GridBagLayout());

        JPanel box = new JPanel(new GridLayout(6, 1, 8, 8));
        box.setBackground(Color.decode("#242021"));
        box.setBorder(
            BorderFactory.createEmptyBorder(18, 18, 18, 18)
        );
        box.setPreferredSize(new Dimension(300, 290));

        JLabel title = new JLabel(
            "Create Account", SwingConstants.CENTER
        );
        title.setFont(new Font("Impact", Font.BOLD, 28));
        title.setForeground(Color.decode("#D6A36A"));

        JLabel usernameLabel = new JLabel("Choose Username");
        usernameLabel.setForeground(Color.decode("#F1E9DD"));

        JTextField username = new JTextField();

        JLabel passwordLabel = new JLabel("Choose Password");
        passwordLabel.setForeground(Color.decode("#F1E9DD"));

        JPasswordField password = new JPasswordField();

        JButton createButton = new JButton("Create Account");
        createButton.setForeground(Color.decode("#F1E9DD"));
        createButton.setBackground(Color.decode("#A64B52"));

        box.add(title);
        box.add(usernameLabel);
        box.add(username);
        box.add(passwordLabel);
        box.add(password);
        box.add(createButton);

        add(box);

        createButton.addActionListener(e -> {
            String name = username.getText().trim();
            char[] pass = password.getPassword();

            if (name.isEmpty() || pass.length == 0) {
                JOptionPane.showMessageDialog(
                    this, "Enter a username and password."
                );
                Arrays.fill(pass, '\0');
                return;
            }

            if (name.length() < 3 || name.length() > 50) {
                JOptionPane.showMessageDialog(
                    this, "Username must be 3 to 50 characters."
                );
                Arrays.fill(pass, '\0');
                return;
            }

            if (pass.length < 8) {
                JOptionPane.showMessageDialog(
                    this, "Password must be at least 8 characters."
                );
                Arrays.fill(pass, '\0');
                return;
            }

            try {
                UserDatabase.createUser(name, pass);

                JOptionPane.showMessageDialog(
                    this, "Account created! You can now log in."
                );

                dispose();

            } catch (java.sql.SQLIntegrityConstraintViolationException ex) {
                JOptionPane.showMessageDialog(
                    this, "Username already exists. Choose another."
                );

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(
                    this, "Could not create account:\n" + ex.getMessage()
                );

            } finally {
                Arrays.fill(pass, '\0');
                password.setText("");
            }
        });
    }
}
