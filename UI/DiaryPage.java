import javax.swing.*;
import java.awt.*;

public class DiaryPage extends JPanel{ 
    Main main;
    
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
    backButton.setBackground(Color.decode("#A64B52"));
    backButton.addActionListener(e->
    {
     main.showPage("home");
    });
    topPanel.add(backButton,BorderLayout.WEST);


    JLabel label = new JLabel("Dairy");
    label.setFont(new Font("Arial",Font.BOLD,30));
    topPanel.add(label, BorderLayout.CENTER);
    panel.add(topPanel,BorderLayout.NORTH);
    


    JPanel monthPanel = createMonth("SEPTEMBER 2026");
    monthPanel.setForeground(Color.decode("#D6A36A"));
    monthPanel.setBackground(Color.decode("#171415"));
    panel.add(monthPanel, BorderLayout.CENTER);

    JButton addmovie = new JButton("Add movie");
    addmovie.setForeground(Color.decode("#D6A36A"));
    addmovie.setBackground(Color.decode("#A64B52"));
    panel.add(addmovie,BorderLayout.SOUTH);

    addmovie.addActionListener(e->{
      JPanel moviePanel = new JPanel(new GridLayout(9,9));

      JLabel nameLabel = new JLabel("Add movie name");
      nameLabel.setForeground(Color.decode("#D6A36A"));
      nameLabel.setBackground(Color.decode("#A64B52"));
      JTextField nameField = new JTextField();

      JLabel ratingLabel = new JLabel("Add Rating");
      ratingLabel.setForeground(Color.decode("#D6A36A"));
      ratingLabel.setBackground(Color.decode("#A64B52"));
      JTextField ratingField = new JTextField();

      JLabel dateLabel = new JLabel("Date Watched");
      dateLabel.setBackground(Color.decode("#A64B52"));
      dateLabel.setForeground(Color.decode("#D6A36A"));
      JTextField dateField = new JTextField();

      moviePanel.add(nameLabel);
      moviePanel.add(nameField);

      moviePanel.add(ratingLabel);
      moviePanel.add(ratingField);

      moviePanel.add(dateLabel);
      moviePanel.add(dateField);

      int result = JOptionPane.showConfirmDialog
      (panel,moviePanel,"Add Movie",JOptionPane.OK_CANCEL_OPTION);

    });
    add(panel,BorderLayout.CENTER);
}

static JPanel createMonth(String monthname)
{
  JPanel monthPanel = new JPanel();
  JLabel monthLabel = new JLabel(monthname);

  monthLabel.setFont(new Font("Ariel",Font.PLAIN,25));
  monthPanel.add(monthLabel);
  return monthPanel;
}


}