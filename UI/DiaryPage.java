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
    monthPanel.setBackground(Color.decode("#171415"));
    panel.add(monthPanel, BorderLayout.CENTER);

    JButton addmovie = new JButton("Add movie");
    panel.add(addmovie,BorderLayout.SOUTH);

    addmovie.addActionListener(e->{
      JPanel moviePanel = new JPanel(new GridLayout(9,9));

      JLabel nameLabel = new JLabel("Add movie name");
      JTextField nameField = new JTextField();

      JLabel ratingLabel = new JLabel("Add Rating");
      JTextField ratingField = new JTextField();

      JLabel dateLabel = new JLabel("Date Watched");
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