import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;

public class Editor {
    private JFrame frame;
    private JPanel menu;
    private JTextArea codeArea;
    private JTextArea output;
    private JTextArea lineNumbers;
    private JSplitPane editorSplitPane, codeSplitPane, outputSplitPane;
    private Color color = new Color(194, 201, 209), fontColor = Color.black;

    public Editor() {
        frame = createFrame();
        menu = createMenu(color, fontColor);
        lineNumbers = createLineNumbers();
        codeArea = createCodeArea();
        outputSplitPane = createLayout();
        frame.add(editorSplitPane, BorderLayout.CENTER);
        frame.setVisible(true);
        updateLineNumbers();
    }

    private JFrame createFrame() {
        JFrame tframe = new JFrame();
        tframe.setLayout(new BorderLayout());
        tframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        tframe.setName("Editor");
        tframe.setMinimumSize(new Dimension(500, 500));
        tframe.setExtendedState(JFrame.MAXIMIZED_BOTH);
        return tframe;
    }

    private JPanel createMenu(Color color, Color fontColor) {
        JPanel panel = new JPanel(new GridLayout(1, 1));
        JButton start = new JButton("Start");
        start.setBackground(color);
        start.setForeground(fontColor);
        start.addActionListener(e -> new Thread(() -> Interpreter.execute(this, getCode())).start());
        panel.add(start);
        return panel;
    }

    private JTextArea createCodeArea() {
        JTextArea ta = new JTextArea();
        ta.setFont(new Font("Arial", Font.PLAIN, 20));
        ta.setBackground(color);
        ta.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateLineNumbers(); }
            public void removeUpdate(DocumentEvent e) { updateLineNumbers(); }
            public void changedUpdate(DocumentEvent e) { updateLineNumbers(); }
        });
        return ta;
    }

    private JTextArea createLineNumbers() {
        JTextArea ta = new JTextArea("1");
        ta.setFont(new Font("Arial", Font.PLAIN, 20));
        ta.setBackground(color);
        ta.setForeground(fontColor);
        ta.setEditable(false);
        ta.setFocusable(false);
        return ta;
    }

    private JScrollPane createOutput(Color color, Color fontColor) {
        output = new JTextArea();
        output.setEditable(false);
        output.setLineWrap(true);
        output.setWrapStyleWord(true);
        output.setBackground(color);
        output.setForeground(fontColor);
        output.setFont(new Font("Monospaced", Font.PLAIN, 16));
        JScrollPane scrollPane = new JScrollPane(output);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        return scrollPane;
    }

    private JSplitPane createLayout() {
        JScrollPane codeScrollPane = new JScrollPane(codeArea);
        JScrollPane lineScrollPane = new JScrollPane(lineNumbers);
        lineScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        lineScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        codeScrollPane.getViewport().addChangeListener(e -> {
            JViewport viewport = (JViewport) e.getSource();
            Rectangle viewRect = viewport.getViewRect();
            lineNumbers.scrollRectToVisible(viewRect);
        });

        codeSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, lineScrollPane, codeScrollPane);
        codeSplitPane.setDividerSize(1);
        codeSplitPane.setDividerLocation(50);
        codeSplitPane.setResizeWeight(0.0);

        JScrollPane outputPane = createOutput(color, fontColor);

        outputSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, codeSplitPane, outputPane);
        outputSplitPane.setDividerLocation(0.7);
        outputSplitPane.setResizeWeight(0.7);

        editorSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, menu, outputSplitPane);
        editorSplitPane.setDividerLocation(0.1);
        editorSplitPane.setResizeWeight(0.1);

        return outputSplitPane;
    }

    public void print(String out) {
        output.append(out + "\n");
        output.setCaretPosition(output.getDocument().getLength());
    }

    private void updateLineNumbers() {
        int lines = codeArea.getLineCount();
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= lines; i++) sb.append(i).append("\n");
        lineNumbers.setText(sb.toString());
    }

    public String getCode() {
        return codeArea.getText();
    }
}
