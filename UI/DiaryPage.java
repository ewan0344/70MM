import javax.swing.*;
import java.awt.*;

public class dairy{
    static JFrame frame;
    public static void main(String[] args)
    {
      createWindow();
      createUI();
      frame.setVisible(true);
    }
    
    

 static void createWindow()
 {
    frame = new JFrame("Movie Dairy");
    frame.setSize(600,500);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 }


static void createUI()
{
    JPanel panel = new JPanel(new BorderLayout());
    JLabel label = new JLabel("Dairy");
    label.setFont(new Font("Arial",Font.BOLD,30));
    panel.add(label,BorderLayout.NORTH);
    frame.add(panel);


    JPanel monthPanel = createMonth("SEPTEMBER 2026");
    panel.add(monthPanel, BorderLayout.CENTER);

    JButton addmovie = new JButton("Add movie");

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
      (frame,moviePanel,"Add Movie",JOptionPane.OK_CANCEL_OPTION);

    });



    panel.add(addmovie,BorderLayout.SOUTH);
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