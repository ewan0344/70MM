
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.io.File;
import java.sql.*;

public class DiaryPage extends JPanel{ 
    Main main;
    JPanel diaryPanel;

    public DiaryPage(Main main)
    {
      this.main=main;
      createUI();
    }

    void createUI()
{
    setLayout(new BorderLayout());
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBackground(Color.decode("#171415"));

    JPanel topPanel = new JPanel(new BorderLayout());
    topPanel.setBackground(Color.decode("#171415"));

    JButton backButton = new JButton("←");
    backButton.setForeground(Color.decode("#D6A36A"));
    backButton.setBackground(Color.decode("#A64B52"));
    backButton.addActionListener(e->
    {
     main.showPage("home");
    });
    topPanel.add(backButton,BorderLayout.WEST);

    JLabel label = new JLabel("Dairy");
    label.setForeground(Color.decode("#D6A36A"));
    label.setFont(new Font("Arial",Font.BOLD,30));
    topPanel.add(label, BorderLayout.CENTER);

    panel.add(topPanel,BorderLayout.NORTH);


    diaryPanel = new JPanel();
    diaryPanel.setLayout(new BoxLayout(diaryPanel,BoxLayout.Y_AXIS));
    diaryPanel.setBackground(Color.decode("#171415"));

    JScrollPane scroll = new JScrollPane(diaryPanel);
    panel.add(scroll,BorderLayout.CENTER);


    JButton addmovie = new JButton("Add movie");
    addmovie.setForeground(Color.decode("#D6A36A"));
    addmovie.setBackground(Color.decode("#A64B52"));
    panel.add(addmovie,BorderLayout.SOUTH);

    addmovie.addActionListener(e->{
      JPanel moviePanel = new JPanel(new GridLayout(4,2,10,10));

      JLabel nameLabel = new JLabel("Add movie name");
      JTextField nameField = new JTextField();

      JLabel ratingLabel = new JLabel("Add Rating (0-10)");
      JTextField ratingField = new JTextField();

      JLabel dateLabel = new JLabel("Date Watched (DD-MM-YYYY)");
      JTextField dateField = new JTextField();

      JLabel review = new JLabel("Add review");
      JTextField reviewField = new JTextField();


      moviePanel.add(nameLabel);
      moviePanel.add(nameField);

      moviePanel.add(ratingLabel);
      moviePanel.add(ratingField);

      moviePanel.add(dateLabel);
      moviePanel.add(dateField);

      moviePanel.add(review);
      moviePanel.add(reviewField);


      int[] movieId = {-1};
      String[] movieName = {""};

      JPopupMenu suggestions = new JPopupMenu();

      nameField.getDocument().addDocumentListener(new DocumentListener()
      {
          public void insertUpdate(DocumentEvent e)
          {
              search();
          }

          public void removeUpdate(DocumentEvent e)
          {
              search();
          }

          public void changedUpdate(DocumentEvent e)
          {
              search();
          }

          void search()
          {
              String text = nameField.getText().trim();

              movieId[0] = -1;
              movieName[0] = "";

              suggestions.setVisible(false);
              suggestions.removeAll();

              if(text.isEmpty())
                  return;

              try
              {
                  Connection con = DatabaseManager.getConnection();

                  String sql = "SELECT tmdb_id,title FROM movies_new WHERE title LIKE ? LIMIT 10";

                  PreparedStatement ps = con.prepareStatement(sql);
                  ps.setString(1,text + "%");

                  ResultSet rs = ps.executeQuery();

                  while(rs.next())
                  {
                      int id = rs.getInt("tmdb_id");
                      String title = rs.getString("title");

                      JMenuItem item = new JMenuItem(title);

                      item.addActionListener(a->
                      {
                          movieId[0] = id;
                          movieName[0] = title;

                          nameField.setText(title);

                          movieId[0] = id;
                          movieName[0] = title;

                          suggestions.setVisible(false);
                      });

                      suggestions.add(item);
                  }

                  rs.close();
                  ps.close();
                  con.close();

                  if(suggestions.getComponentCount()>0 && nameField.isShowing())
                  {
                      suggestions.show(nameField,0,nameField.getHeight());
                  }
              }
              catch(Exception ex)
              {
                  ex.printStackTrace();
              }
          }
      });


      int result = JOptionPane.showConfirmDialog
      (panel,moviePanel,"Add Movie",JOptionPane.OK_CANCEL_OPTION);


      if(result == JOptionPane.OK_OPTION)
      {
          if(movieId[0] == -1)
          {
              JOptionPane.showMessageDialog(panel,"Select a movie from suggestions");
              return;
          }

          String rating = ratingField.getText();
          String date = dateField.getText();
          String reviewText = reviewField.getText();

          if(rating.isEmpty() || date.isEmpty())
          {
              JOptionPane.showMessageDialog(panel,"Enter rating and date");
              return;
          }

          try
          {
              double value = Double.parseDouble(rating);

              if(value < 0 || value > 10)
              {
                  JOptionPane.showMessageDialog(panel,"Rating must be between 0 and 10");
                  return;
              }
          }
          catch(NumberFormatException ex)
          {
              JOptionPane.showMessageDialog(panel,"Enter a valid rating");
              return;
          }

          addToDiary(movieId[0],movieName[0],rating,date,reviewText);
      }

    });

    add(panel,BorderLayout.CENTER);
}


void addToDiary(int id,String title,String rating,String date,String review)
{
    JPanel movie = new JPanel(new BorderLayout(10,10));
    movie.setBackground(Color.decode("#241E20"));
    movie.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
    movie.setMaximumSize(new Dimension(650,180));

    JLabel poster = new JLabel();
    poster.setPreferredSize(new Dimension(100,150));

    File file = new File("posters/" + id + ".jpg");

    if(file.exists())
    {
        ImageIcon icon = new ImageIcon(file.getAbsolutePath());

        Image image = icon.getImage().getScaledInstance(100,150,Image.SCALE_SMOOTH);

        poster.setIcon(new ImageIcon(image));
    }
    else
    {
        poster.setText("No Poster");
        poster.setForeground(Color.WHITE);
    }

    JPanel details = new JPanel(new GridLayout(4,1));
    details.setBackground(Color.decode("#241E20"));

    JLabel movieTitle = new JLabel(title);
    movieTitle.setForeground(Color.decode("#D6A36A"));
    movieTitle.setFont(new Font("Arial",Font.BOLD,20));

    JLabel movieRating = new JLabel("Rating: " + rating + "/10");
    movieRating.setForeground(Color.WHITE);

    JLabel movieDate = new JLabel("Watched: " + date);
    movieDate.setForeground(Color.WHITE);

    JLabel movieReview = new JLabel("Review: " + review);
    movieReview.setForeground(Color.WHITE);

    details.add(movieTitle);
    details.add(movieRating);
    details.add(movieDate);
    details.add(movieReview);

    movie.add(poster,BorderLayout.WEST);
    movie.add(details,BorderLayout.CENTER);

    diaryPanel.add(movie);
    diaryPanel.add(Box.createVerticalStrut(10));

    diaryPanel.revalidate();
    diaryPanel.repaint();
}

}
