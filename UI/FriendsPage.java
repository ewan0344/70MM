
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.util.List;

public class FriendsPage extends JFrame {

    String username;

    Color BG = Color.decode("#171415");
    Color CARD = Color.decode("#241E20");
    Color GOLD = Color.decode("#D6A36A");
    Color RED = Color.decode("#A64B52");
    Color TEXT = Color.decode("#F1E9DD");

    JTextField searchField;

    DefaultListModel<String> suggestionModel;
    JList<String> suggestionList;

    DefaultListModel<String> friendModel;
    JList<String> friendList;

    JPanel requestsPanel;

    public FriendsPage(String username) {

        this.username = username;

        setTitle("70mm - Friends");
        setSize(550, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(BG);
        mainPanel.setBorder(
            BorderFactory.createEmptyBorder(12, 12, 12, 12)
        );

        JLabel title = new JLabel("Friends", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setForeground(GOLD);

        mainPanel.add(title, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(BG);

        // Search section
        JPanel searchPanel = new JPanel(new BorderLayout(5, 6));
        searchPanel.setBackground(CARD);
        searchPanel.setBorder(
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        );

        JLabel searchLabel = new JLabel("Add Friend");
        styleLabel(searchLabel, 17);

        searchField = new JTextField();

        suggestionModel = new DefaultListModel<>();
        suggestionList = new JList<>(suggestionModel);
        suggestionList.setSelectionMode(
            ListSelectionModel.SINGLE_SELECTION
        );

        JButton addButton = new JButton("Send Friend Request");
        styleButton(addButton);

        JPanel searchTop = new JPanel(new BorderLayout(5, 5));
        searchTop.setBackground(CARD);
        searchTop.add(searchLabel, BorderLayout.NORTH);
        searchTop.add(searchField, BorderLayout.CENTER);

        searchPanel.add(searchTop, BorderLayout.NORTH);
        searchPanel.add(
            new JScrollPane(suggestionList), BorderLayout.CENTER
        );
        searchPanel.add(addButton, BorderLayout.SOUTH);

        searchPanel.setPreferredSize(new Dimension(500, 200));
        searchPanel.setMaximumSize(new Dimension(1000, 200));

        // Friend requests section
        JPanel requestSection = new JPanel(new BorderLayout(5, 5));
        requestSection.setBackground(CARD);
        requestSection.setBorder(
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        );

        JLabel requestTitle = new JLabel("Friend Requests");
        styleLabel(requestTitle, 17);

        requestsPanel = new JPanel();
        requestsPanel.setLayout(
            new BoxLayout(requestsPanel, BoxLayout.Y_AXIS)
        );
        requestsPanel.setBackground(CARD);

        JScrollPane requestScroll = new JScrollPane(requestsPanel);
        requestScroll.setPreferredSize(new Dimension(480, 150));

        requestSection.add(requestTitle, BorderLayout.NORTH);
        requestSection.add(requestScroll, BorderLayout.CENTER);

        // My Friends section
        JPanel friendsSection = new JPanel(new BorderLayout(5, 5));
        friendsSection.setBackground(CARD);
        friendsSection.setBorder(
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        );

        JLabel friendsTitle = new JLabel("My Friends");
        styleLabel(friendsTitle, 17);

        friendModel = new DefaultListModel<>();
        friendList = new JList<>(friendModel);
        friendList.setFont(new Font("Arial", Font.PLAIN, 16));
        friendList.setForeground(TEXT);
        friendList.setBackground(CARD);
        friendList.setSelectionMode(
            ListSelectionModel.SINGLE_SELECTION
        );
        friendList.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Click a friend to open their profile.
        friendList.addMouseListener(
            new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(
                        java.awt.event.MouseEvent e) {

                    int index = friendList.locationToIndex(e.getPoint());

                    if (index < 0) {
                        return;
                    }

                    Rectangle bounds =
                        friendList.getCellBounds(index, index);

                    if (bounds == null ||
                        !bounds.contains(e.getPoint())) {
                        return;
                    }

                    String selected =
                        friendList.getModel().getElementAt(index);

                    if (selected != null &&
                        !selected.equals("No friends yet")) {

                        new FriendProfilePage(selected);
                    }
                }
            }
        );

        friendsSection.add(friendsTitle, BorderLayout.NORTH);
        friendsSection.add(
            new JScrollPane(friendList), BorderLayout.CENTER
        );

        friendsSection.setPreferredSize(new Dimension(500, 160));
        friendsSection.setMaximumSize(new Dimension(1000, 200));

        content.add(searchPanel);
        content.add(Box.createVerticalStrut(10));
        content.add(requestSection);
        content.add(Box.createVerticalStrut(10));
        content.add(friendsSection);

        mainPanel.add(new JScrollPane(content), BorderLayout.CENTER);

        setContentPane(mainPanel);

        // Search automatically as the user types.
        searchField.getDocument().addDocumentListener(
            new DocumentListener() {

                public void insertUpdate(DocumentEvent e) {
                    searchUsers();
                }

                public void removeUpdate(DocumentEvent e) {
                    searchUsers();
                }

                public void changedUpdate(DocumentEvent e) {
                    searchUsers();
                }
            }
        );

        addButton.addActionListener(e -> sendRequest());

        loadRequests();
        loadFriends();

        setVisible(true);
    }


    // Search registered usernames.
    void searchUsers() {

        String text = searchField.getText().trim();

        suggestionModel.clear();

        if (text.isEmpty()) {
            return;
        }

        try {

            List<String> users =
                UserDatabase.searchUsers(username, text);

            for (String user : users) {
                suggestionModel.addElement(user);
            }

        } catch (Exception ex) {
            showError("Could not search users", ex);
        }
    }


    // Send a friend request.
    void sendRequest() {

        String selected = suggestionList.getSelectedValue();

        if (selected == null) {
            JOptionPane.showMessageDialog(
                this,
                "Type a username and select a result first."
            );
            return;
        }

        try {

            boolean sent =
                UserDatabase.sendFriendRequest(username, selected);

            if (sent) {

                JOptionPane.showMessageDialog(
                    this,
                    "Friend request sent to " + selected + "!"
                );

            } else {

                JOptionPane.showMessageDialog(
                    this,
                    "Could not send request. You may already be friends " +
                    "or have a pending request."
                );
            }

            searchField.setText("");
            suggestionModel.clear();

            loadFriends();
            loadRequests();

        } catch (Exception ex) {
            showError("Could not send friend request", ex);
        }
    }


    // Show incoming friend requests.
    void loadRequests() {

        requestsPanel.removeAll();

        try {

            List<String[]> requests =
                UserDatabase.getPendingRequestDetails(username);

            if (requests.isEmpty()) {

                JLabel empty =
                    new JLabel("No new friend requests");

                empty.setForeground(TEXT);
                requestsPanel.add(empty);
            }

            for (String[] request : requests) {

                int requestId =
                    Integer.parseInt(request[0]);

                String sender = request[1];

                JPanel row = new JPanel(new BorderLayout(5, 5));
                row.setBackground(CARD);
                row.setBorder(
                    BorderFactory.createEmptyBorder(5, 0, 5, 0)
                );

                JLabel name =
                    new JLabel(sender + " sent you a request");

                name.setForeground(TEXT);

                JButton accept = new JButton("Accept");
                JButton reject = new JButton("Reject");

                styleButton(accept);
                styleButton(reject);

                JPanel buttons =
                    new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));

                buttons.setBackground(CARD);
                buttons.add(accept);
                buttons.add(reject);

                row.add(name, BorderLayout.CENTER);
                row.add(buttons, BorderLayout.SOUTH);

                accept.addActionListener(e -> {

                    try {

                        boolean accepted =
                            UserDatabase.acceptFriendRequest(
                                username, requestId
                            );

                        if (accepted) {
                            JOptionPane.showMessageDialog(
                                this,
                                "You and " + sender + " are now friends!"
                            );
                        }

                        loadRequests();
                        loadFriends();
                        searchUsers();

                    } catch (Exception ex) {
                        showError("Could not accept request", ex);
                    }
                });

                reject.addActionListener(e -> {

                    try {

                        UserDatabase.rejectFriendRequest(
                            username, requestId
                        );

                        loadRequests();
                        searchUsers();

                    } catch (Exception ex) {
                        showError("Could not reject request", ex);
                    }
                });

                requestsPanel.add(row);
                requestsPanel.add(Box.createVerticalStrut(5));
            }

        } catch (Exception ex) {
            showError("Could not load friend requests", ex);
        }

        requestsPanel.revalidate();
        requestsPanel.repaint();
    }


    // Load accepted friends.
    void loadFriends() {

        friendModel.clear();

        try {

            List<String> friends =
                UserDatabase.getFriends(username);

            if (friends.isEmpty()) {

                friendModel.addElement("No friends yet");

            } else {

                for (String friend : friends) {
                    friendModel.addElement(friend);
                }
            }

        } catch (Exception ex) {
            showError("Could not load friends", ex);
        }
    }


    void styleLabel(JLabel label, int size) {

        label.setForeground(GOLD);
        label.setFont(new Font("Arial", Font.BOLD, size));
    }


    void styleButton(JButton button) {

        button.setBackground(RED);
        button.setForeground(TEXT);
        button.setFocusPainted(false);
    }


    void showError(String message, Exception ex) {

        ex.printStackTrace();

        JOptionPane.showMessageDialog(
            this,
            message + ": " + ex.getMessage()
        );
    }
}
