import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.sql.*;
import java.util.List;

public class FriendsPage extends JFrame {

    private final Main main;
    private final String username;

    private final Color BG = Color.decode("#171415");
    private final Color CARD = Color.decode("#241E20");
    private final Color GOLD = Color.decode("#D6A36A");
    private final Color RED = Color.decode("#A64B52");
    private final Color TEXT = Color.decode("#F1E9DD");

    private JTextField searchField;
    private DefaultListModel<String> suggestionModel;
    private JList<String> suggestionList;
    private DefaultListModel<String> friendModel;
    private JList<String> friendList;
    private JPanel requestsPanel;
    private JPanel suggestionsPanel;

    public FriendsPage(Main main, String username) {
        this.main = main;
        this.username = username;

        setTitle("70mm - Friends");
        setSize(580, 820);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBackground(BG);
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JLabel title = new JLabel("Friends", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setForeground(GOLD);
        root.add(title, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(BG);

        // Add Friend
        JPanel searchPanel = new JPanel(new BorderLayout(5, 6));
        searchPanel.setBackground(CARD);
        searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel searchLabel = new JLabel("Add Friend");
        styleLabel(searchLabel, 17);

        searchField = new JTextField();
        suggestionModel = new DefaultListModel<>();
        suggestionList = new JList<>(suggestionModel);
        suggestionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JButton addButton = new JButton("Send Friend Request");
        styleButton(addButton);

        JPanel searchTop = new JPanel(new BorderLayout(5, 5));
        searchTop.setBackground(CARD);
        searchTop.add(searchLabel, BorderLayout.NORTH);
        searchTop.add(searchField, BorderLayout.CENTER);

        searchPanel.add(searchTop, BorderLayout.NORTH);
        searchPanel.add(new JScrollPane(suggestionList), BorderLayout.CENTER);
        searchPanel.add(addButton, BorderLayout.SOUTH);
        searchPanel.setPreferredSize(new Dimension(520, 190));
        searchPanel.setMaximumSize(new Dimension(1000, 190));

        // Friend requests
        JPanel requestSection = new JPanel(new BorderLayout(5, 5));
        requestSection.setBackground(CARD);
        requestSection.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel requestTitle = new JLabel("Friend Requests");
        styleLabel(requestTitle, 17);

        requestsPanel = new JPanel();
        requestsPanel.setLayout(new BoxLayout(requestsPanel, BoxLayout.Y_AXIS));
        requestsPanel.setBackground(CARD);

        requestSection.add(requestTitle, BorderLayout.NORTH);
        requestSection.add(new JScrollPane(requestsPanel), BorderLayout.CENTER);
        requestSection.setPreferredSize(new Dimension(520, 150));
        requestSection.setMaximumSize(new Dimension(1000, 190));

        // Movie suggestions
        JPanel movieSuggestionSection = new JPanel(new BorderLayout(5, 5));
        movieSuggestionSection.setBackground(CARD);
        movieSuggestionSection.setBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel movieSuggestionTitle = new JLabel("Movie Suggestions");
        styleLabel(movieSuggestionTitle, 17);

        suggestionsPanel = new JPanel();
        suggestionsPanel.setLayout(new BoxLayout(suggestionsPanel, BoxLayout.Y_AXIS));
        suggestionsPanel.setBackground(CARD);

        movieSuggestionSection.add(movieSuggestionTitle, BorderLayout.NORTH);
        movieSuggestionSection.add(
                new JScrollPane(suggestionsPanel), BorderLayout.CENTER);
        movieSuggestionSection.setPreferredSize(new Dimension(520, 190));
        movieSuggestionSection.setMaximumSize(new Dimension(1000, 230));

        // Accepted friends
        JPanel friendsSection = new JPanel(new BorderLayout(5, 5));
        friendsSection.setBackground(CARD);
        friendsSection.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel friendsTitle = new JLabel("My Friends");
        styleLabel(friendsTitle, 17);

        friendModel = new DefaultListModel<>();
        friendList = new JList<>(friendModel);
        friendList.setFont(new Font("Arial", Font.PLAIN, 16));
        friendList.setForeground(TEXT);
        friendList.setBackground(CARD);
        friendList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        friendsSection.add(friendsTitle, BorderLayout.NORTH);
        friendsSection.add(new JScrollPane(friendList), BorderLayout.CENTER);
        friendsSection.setPreferredSize(new Dimension(520, 140));
        friendsSection.setMaximumSize(new Dimension(1000, 170));

        content.add(searchPanel);
        content.add(Box.createVerticalStrut(10));
        content.add(requestSection);
        content.add(Box.createVerticalStrut(10));
        content.add(movieSuggestionSection);
        content.add(Box.createVerticalStrut(10));
        content.add(friendsSection);

        JScrollPane mainScroll = new JScrollPane(content);
        mainScroll.setBorder(BorderFactory.createEmptyBorder());
        mainScroll.getViewport().setBackground(BG);
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);

        root.add(mainScroll, BorderLayout.CENTER);
        setContentPane(root);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                searchUsers();
            }

            public void removeUpdate(DocumentEvent e) {
                searchUsers();
            }

            public void changedUpdate(DocumentEvent e) {
                searchUsers();
            }
        });

        addButton.addActionListener(e -> sendRequest());

        friendList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selected = friendList.getSelectedValue();

                if (selected != null && !selected.equals("No friends yet")) {
                    new FriendProfilePage(main, selected);
                    friendList.clearSelection();
                }
            }
        });

        loadRequests();
        loadFriends();
        loadMovieSuggestions();

        setVisible(true);
    }

    private void searchUsers() {
        String text = searchField.getText().trim();
        suggestionModel.clear();

        if (text.isEmpty()) {
            return;
        }

        try {
            List<String> users = UserDatabase.searchUsers(username, text);

            for (String user : users) {
                suggestionModel.addElement(user);
            }
        } catch (Exception ex) {
            showError("Could not search users", ex);
        }
    }

    private void sendRequest() {
        String selected = suggestionList.getSelectedValue();

        if (selected == null) {
            JOptionPane.showMessageDialog(
                    this, "Type a username and select a result first.");
            return;
        }

        try {
            boolean sent = UserDatabase.sendFriendRequest(username, selected);

            JOptionPane.showMessageDialog(
                    this,
                    sent
                            ? "Friend request sent to " + selected + "!"
                            : "Could not send request. You may already be friends or have a pending request.");

            searchField.setText("");
            suggestionModel.clear();
            loadFriends();
            loadRequests();

        } catch (Exception ex) {
            showError("Could not send friend request", ex);
        }
    }

    private void loadRequests() {
        requestsPanel.removeAll();

        try {
            List<String[]> requests =
                    UserDatabase.getPendingRequestDetails(username);

            if (requests.isEmpty()) {
                addEmptyLabel(requestsPanel, "No new friend requests");
            }

            for (String[] request : requests) {
                int requestId = Integer.parseInt(request[0]);
                String sender = request[1];

                JPanel row = new JPanel(new BorderLayout(5, 5));
                row.setBackground(CARD);

                JLabel label = new JLabel(sender + " sent you a request");
                label.setForeground(TEXT);

                JButton accept = new JButton("Accept");
                JButton reject = new JButton("Reject");
                styleButton(accept);
                styleButton(reject);

                JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                buttons.setBackground(CARD);
                buttons.add(accept);
                buttons.add(reject);

                row.add(label, BorderLayout.CENTER);
                row.add(buttons, BorderLayout.SOUTH);
                requestsPanel.add(row);

                accept.addActionListener(e -> {
                    try {
                        if (UserDatabase.acceptFriendRequest(username, requestId)) {
                            JOptionPane.showMessageDialog(
                                    this, "You and " + sender + " are now friends!");
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
                        UserDatabase.rejectFriendRequest(username, requestId);
                        loadRequests();
                        searchUsers();

                    } catch (Exception ex) {
                        showError("Could not reject request", ex);
                    }
                });
            }

        } catch (Exception ex) {
            showError("Could not load friend requests", ex);
        }

        requestsPanel.revalidate();
        requestsPanel.repaint();
    }

    private void loadFriends() {
        friendModel.clear();

        try {
            List<String> friends = UserDatabase.getFriends(username);

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

    private void loadMovieSuggestions() {
        suggestionsPanel.removeAll();

        String sql =
                "SELECT s.suggestion_id, s.tmdb_id, u.username, m.title " +
                "FROM mm70_users.movie_suggestions s " +
                "JOIN mm70_users.users u ON u.user_id = s.sender_id " +
                "JOIN movies_new m ON m.tmdb_id = s.tmdb_id " +
                "JOIN mm70_users.users recipient ON recipient.user_id = s.recipient_id " +
                "WHERE recipient.username = ? AND s.status = 'pending' " +
                "ORDER BY s.created_at DESC";

        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                int count = 0;

                while (rs.next()) {
                    int suggestionId = rs.getInt("suggestion_id");
                    int movieId = rs.getInt("tmdb_id");
                    String sender = rs.getString("username");
                    String title = rs.getString("title");

                    JPanel row = new JPanel(new BorderLayout(5, 5));
                    row.setBackground(CARD);
                    row.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

                    JLabel label = new JLabel(sender + " suggests: " + title);
                    label.setForeground(TEXT);

                    JButton details = new JButton("Details");
                    JButton add = new JButton("Add to Watchlist");
                    JButton dismiss = new JButton("Dismiss");

                    styleButton(details);
                    styleButton(add);
                    styleButton(dismiss);

                    JPanel actions = new JPanel(
                            new FlowLayout(FlowLayout.RIGHT, 4, 0));
                    actions.setBackground(CARD);
                    actions.add(details);
                    actions.add(add);
                    actions.add(dismiss);

                    row.add(label, BorderLayout.CENTER);
                    row.add(actions, BorderLayout.SOUTH);
                    suggestionsPanel.add(row);

                    details.addActionListener(e ->
                            main.showMovieDetails(movieId));

                    add.addActionListener(e ->
                            processSuggestion(suggestionId, movieId, title));

                    dismiss.addActionListener(e ->
                            updateSuggestionStatus(suggestionId, "dismissed"));

                    count++;
                }

                if (count == 0) {
                    addEmptyLabel(suggestionsPanel, "No movie suggestions yet");
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            addEmptyLabel(
                    suggestionsPanel,
                    "Could not load suggestions: " + ex.getMessage());
        }

        suggestionsPanel.revalidate();
        suggestionsPanel.repaint();
    }

    /*
     * Checks the recipient's own diary and watchlist before adding.
     * If the movie already exists in either place, the pending suggestion
     * is removed instead of adding a duplicate.
     */
    private void processSuggestion(int suggestionId, int movieId, String title) {
        String checkUser =
                "SELECT user_id FROM mm70_users.users WHERE username = ?";

        String checkDiary =
                "SELECT 1 FROM diary_entries WHERE user_id = ? AND tmdb_id = ? LIMIT 1";

        String checkWatchlist =
                "SELECT 1 FROM watchlist_items_new WHERE user_id = ? AND tmdb_id = ? LIMIT 1";

        String insertWatchlist =
                "INSERT INTO watchlist_items_new (user_id, tmdb_id, status) " +
                "VALUES (?, ?, 'WANTED')";

        try (Connection con = DatabaseManager.getConnection()) {
            con.setAutoCommit(false);

            try {
                int recipientId;

                try (PreparedStatement ps = con.prepareStatement(checkUser)) {
                    ps.setString(1, username);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException(
                                    "Could not find your user account: " + username);
                        }

                        recipientId = rs.getInt("user_id");
                    }
                }

                // Check if the movie is already in this user's diary.
                boolean inDiary;

                try (PreparedStatement ps = con.prepareStatement(checkDiary)) {
                    ps.setInt(1, recipientId);
                    ps.setInt(2, movieId);

                    try (ResultSet rs = ps.executeQuery()) {
                        inDiary = rs.next();
                    }
                }

                if (inDiary) {
                    markSuggestionDismissed(con, suggestionId, recipientId);
                    con.commit();

                    JOptionPane.showMessageDialog(
                            this,
                            "\"" + title + "\" is already in your Diary.\n" +
                            "The suggestion has been removed.");

                    loadMovieSuggestions();
                    return;
                }

                // Check if the movie is already in this user's watchlist.
                boolean inWatchlist;

                try (PreparedStatement ps = con.prepareStatement(checkWatchlist)) {
                    ps.setInt(1, recipientId);
                    ps.setInt(2, movieId);

                    try (ResultSet rs = ps.executeQuery()) {
                        inWatchlist = rs.next();
                    }
                }

                if (inWatchlist) {
                    markSuggestionDismissed(con, suggestionId, recipientId);
                    con.commit();

                    JOptionPane.showMessageDialog(
                            this,
                            "\"" + title + "\" is already in your Watchlist.\n" +
                            "The suggestion has been removed.");

                    loadMovieSuggestions();
                    return;
                }

                // Add the movie to this recipient's watchlist.
                try (PreparedStatement ps = con.prepareStatement(insertWatchlist)) {
                    ps.setInt(1, recipientId);
                    ps.setInt(2, movieId);
                    ps.executeUpdate();
                }

                // Remove it from pending suggestions after successful addition.
                markSuggestionDismissed(con, suggestionId, recipientId);

                con.commit();

                JOptionPane.showMessageDialog(
                        this,
                        "\"" + title + "\" was added to your Watchlist!");

                loadMovieSuggestions();

            } catch (Exception ex) {
                con.rollback();
                throw ex;

            } finally {
                con.setAutoCommit(true);
            }

        } catch (Exception ex) {
            showError("Could not process movie suggestion", ex);
        }
    }

    private void markSuggestionDismissed(
            Connection con, int suggestionId, int recipientId)
            throws SQLException {

        String sql =
                "UPDATE mm70_users.movie_suggestions " +
                "SET status = 'dismissed' " +
                "WHERE suggestion_id = ? AND recipient_id = ? " +
                "AND status = 'pending'";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, suggestionId);
            ps.setInt(2, recipientId);
            ps.executeUpdate();
        }
    }

    private void updateSuggestionStatus(int suggestionId, String status) {
        String sql =
                "UPDATE mm70_users.movie_suggestions s " +
                "JOIN mm70_users.users u ON u.user_id = s.recipient_id " +
                "SET s.status = ? " +
                "WHERE s.suggestion_id = ? " +
                "AND u.username = ? AND s.status = 'pending'";

        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, suggestionId);
            ps.setString(3, username);
            ps.executeUpdate();

            loadMovieSuggestions();

        } catch (Exception ex) {
            showError("Could not update suggestion", ex);
        }
    }

    private void addEmptyLabel(JPanel panel, String text) {
        JLabel label = new JLabel(text);
        label.setForeground(TEXT);
        panel.add(label);
    }

    private void styleLabel(JLabel label, int size) {
        label.setForeground(GOLD);
        label.setFont(new Font("Arial", Font.BOLD, size));
    }

    private void styleButton(JButton button) {
        button.setBackground(RED);
        button.setForeground(TEXT);
        button.setFocusPainted(false);
    }

    private void showError(String message, Exception ex) {
        ex.printStackTrace();

        JOptionPane.showMessageDialog(
                this,
                message + ":\n" + ex.getMessage(),
                "70mm",
                JOptionPane.ERROR_MESSAGE);
    }
}
