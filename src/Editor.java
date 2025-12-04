import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.*;

public class Editor {
   private JFrame frame = new JFrame();
   private JPanel lines, code;
   private JSplitPane splitPane; 
   private Color color = new Color(194, 201, 209), fontColor = Color.black;

    public Editor() {
      // color = Color.CYAN;
      frame = createFrame();
      code = createCodeArea(color, fontColor);
      lines = createLineCounter(color, fontColor);
      
      splitPane = createSplitPane(lines, code);
      frame.add(splitPane, BorderLayout.CENTER);
      frame.setVisible(true);
    }

    private JFrame createFrame(){
      JFrame tframe = new JFrame();

      tframe.setLayout(new BorderLayout());
      tframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      tframe.setName("Editor");
      tframe.setMinimumSize(new Dimension(500, 500));
      tframe.setExtendedState(JFrame.MAXIMIZED_BOTH);
      tframe.setVisible(true);

      return tframe;
    }


    private JSplitPane createSplitPane(JPanel lines, JPanel code){
      JSplitPane tsplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, lines, code);
      tsplitPane.setDividerLocation(0.5);
      tsplitPane.setResizeWeight(0.05);
      return tsplitPane;
    }

    private JPanel createCodeArea(Color color, Color fontColor){
      JPanel panel = new JPanel(new GridLayout(1, 1));
      JTextArea textArea = new JTextArea();
      
      textArea.setFont(new Font("Arial", Font.PLAIN, 20));
      textArea.setBackground(color);
      textArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
    @Override
    public void insertUpdate(javax.swing.event.DocumentEvent e) {
        updateLines();
    }

    @Override
    public void removeUpdate(javax.swing.event.DocumentEvent e) {
        updateLines();
    }

    @Override
    public void changedUpdate(javax.swing.event.DocumentEvent e) {
        updateLines();
    }
    });

      panel.add(textArea);
      return panel;
    }

    private JPanel createLineCounter(Color color, Color fontColor){
      JPanel panel = new JPanel(new GridLayout(1, 1));
      JLabel label = new JLabel("1");

      label.setFont(new Font("Arial", Font.PLAIN, 20));
      label.setHorizontalAlignment(SwingConstants.CENTER);
      label.setVerticalAlignment(SwingConstants.TOP);
      label.setOpaque(true);
      label.setBackground(color);
      label.setForeground(fontColor);

      panel.add(label);
      return panel;
    }


    private void addLinesToLabel(JLabel label, JTextArea ta){
      String input = ta.getText();
      int lines = input.split("\n").length;
      String counter = "<html>";
      for (int i = 1; i <= lines; i++) {
         counter += i+"<br>";
      }
      counter += "</html>";
      label.setText(counter);
    }

    public void updateLines(){
      JLabel label = new JLabel();
      JTextArea ta = new JTextArea();
      for (java.awt.Component component : lines.getComponents()) {
        if (component instanceof JLabel) { // Check if the component is a JLabel
            label = (JLabel) component; // Cast to JLabel
         }
      }
      for (java.awt.Component component : code.getComponents()) {
        if (component instanceof JTextArea) { // Check if the component is a JLabel
            ta = (JTextArea) component; // Cast to JLabel
         }
      }
      addLinesToLabel(label, ta);
   }

}
