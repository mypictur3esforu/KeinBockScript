import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.*;

public class Editor {
   private JFrame frame = new JFrame();
   private JPanel lines, code, menu;
   private JLabel output;
   private JSplitPane codeSplitPane, editorSplitPane, outputSplitPane;
   private Color color = new Color(194, 201, 209), fontColor = Color.black;

   public Editor() {
      // color = Color.CYAN;
      frame = createFrame();
      code = createCodeArea(color, fontColor);
      lines = createLineCounter(color, fontColor);
      menu = createMenu(color, fontColor);
      output = createOutput(color, fontColor);

      codeSplitPane = createcodeSplitPane(lines, code);
      outputSplitPane = createOutputSplitPane(codeSplitPane, output);
      editorSplitPane = createEditorSplitPane(menu, outputSplitPane);
      // frame.add(codeSplitPane, BorderLayout.CENTER);
      frame.add(editorSplitPane, BorderLayout.CENTER);
      frame.setVisible(true);
   }

   public void print(String out){
      output.setText(output.getText()+"\n"+out);
   }

   private JFrame createFrame() {
      JFrame tframe = new JFrame();

      tframe.setLayout(new BorderLayout());
      tframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      tframe.setName("Editor");
      tframe.setMinimumSize(new Dimension(500, 500));
      tframe.setExtendedState(JFrame.MAXIMIZED_BOTH);
      tframe.setVisible(true);

      return tframe;
   }

   private JSplitPane createEditorSplitPane(JPanel menu, JSplitPane codeSP){
      JSplitPane tsp = new JSplitPane(JSplitPane.VERTICAL_SPLIT, menu, codeSP);
      tsp.setDividerLocation(0.2);
      tsp.setResizeWeight(0.1);
      return tsp;
   }

   private JSplitPane createcodeSplitPane(JPanel lines, JPanel code) {
      JSplitPane tsplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, lines, code);
      tsplitPane.setDividerLocation(0.5);
      tsplitPane.setResizeWeight(0.05);
      return tsplitPane;
   }

   private JSplitPane createOutputSplitPane(JSplitPane input, JLabel output) {
      JSplitPane tsplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, input, output);
      tsplitPane.setDividerLocation(0.8);
      tsplitPane.setResizeWeight(0.9);
      return tsplitPane;
   }
   
   private JPanel createMenu(Color color, Color fontColor) {
      JPanel panel = new JPanel(new GridLayout(1, 1));
      JButton start = new JButton("Start");

      start.setBackground(color);
      start.setForeground(fontColor);
      start.addActionListener((actionEvent) -> {Interpreter.execute(this, getCode());});

      panel.add(start);
      return panel;
   }

   private JLabel createOutput(Color color, Color fontColor){
      JLabel label = new JLabel();

      return label;
   }

   private JPanel createCodeArea(Color color, Color fontColor) {
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

   private JPanel createLineCounter(Color color, Color fontColor) {
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

   private void addLinesToLabel(JLabel label, String code) {
      int lines = code.split("\n").length;
      String counter = "<html>";
      for (int i = 1; i <= lines; i++) {
         counter += i + "<br>";
      }
      counter += "</html>";
      label.setText(counter);
   }

   public void updateLines() {
      JLabel label = getLineLabel();
      String ta = getCode();

      addLinesToLabel(label, ta);
   }

   private JLabel getLineLabel() {
      JLabel label = new JLabel();
      for (java.awt.Component component : lines.getComponents()) {
         if (component instanceof JLabel) {
            label = (JLabel) component;
         }
      }
      return label;
   }

   public String getCode() {
      JTextArea ta = new JTextArea("temp");
      for (java.awt.Component component : code.getComponents()) {
         if (component instanceof JTextArea) {
            ta = (JTextArea) component;
         }
      }
      // System.out.println("getCode: " + ta.getText());
      return ta.getText();
   }
}
