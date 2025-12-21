import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;

public class Editor {
    private JFrame frame;
    private JPanel menu;
    private JTextArea codeArea;
    private JTextArea output, lineNumbers;
    private JTextField inputField, programmNameField;

    private JSplitPane editorSplitPane, codeSplitPane, outputSplitPane;
    private Color color = new Color(133, 140, 138), fontColor = Color.white;
    private Color cAreaC = new Color(100, 100, 100);
    // Konsol Farben
    private Color consoleColor = new Color(83, 90, 88), consoleInputC = new Color(30, 30, 40);
    private Interpreter interpreter;

    public Editor() {
        frame = createFrame();
        // menu = createMenu(color, fontColor);
        menu = createTopBar(color, fontColor);
        lineNumbers = createLineNumbers();
        codeArea = createCodeArea(cAreaC, Color.white);
        createLayout();

        frame.add(editorSplitPane, BorderLayout.CENTER);
        frame.setVisible(true);
        updateLineNumbers();
    }

    private JFrame createFrame() {
        JFrame tframe = new JFrame("Editor");
        tframe.setLayout(new BorderLayout());
        tframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        tframe.setMinimumSize(new Dimension(500, 500));
        tframe.setExtendedState(JFrame.MAXIMIZED_BOTH);
        return tframe;
    }

        private JPanel createTopBar(Color color, Color fontColor) {
        JPanel topBar = new JPanel(new GridLayout(1, 2));

        topBar.add(createMenu(color, fontColor));
        topBar.add(createProgrammNameBar(color, fontColor));

        return topBar;
    }

    private JPanel createMenu(Color color, Color fontColor) {
        JPanel panel = new JPanel(new GridLayout(1, 3, 10, 0));

        JButton start = new JButton("Start");
        JButton save = new JButton("Speichern");
        JButton load = new JButton("Laden");

        for (JButton b : new JButton[] { start, save, load }) {
            b.setBackground(color);
            b.setForeground(fontColor);
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        // start.addActionListener(e -> new Thread(() -> interpreter = Interpreter.execute(this, getCode())).start());
        start.addActionListener(e -> interpreter = Interpreter.execute(this, getCode()));

        save.addActionListener(e -> {
            Storing.saveProgramm(getProgrammName(), getCode());
        });

        load.addActionListener(e -> {
            // TODO: Laden
        });

        panel.add(start);
        panel.add(save);
        panel.add(load);

        return panel;
    }

    private JPanel createProgrammNameBar(Color color, Color fontColor) {
        JPanel panel = new JPanel(new BorderLayout());

        JLabel label = new JLabel(" Programmname: ");
        label.setForeground(fontColor);

        programmNameField = new JTextField();
        programmNameField.setFont(new Font("Arial", Font.PLAIN, 16));
        programmNameField.setBackground(color);
        programmNameField.setForeground(fontColor);

        panel.add(label, BorderLayout.WEST);
        panel.add(programmNameField, BorderLayout.CENTER);

        return panel;
    }

    private JTextArea createCodeArea(Color color, Color fontColor) {
        JTextArea ta = new JTextArea();
        ta.setFont(new Font("Arial", Font.PLAIN, 20));
        ta.setBackground(color);
        ta.setForeground(fontColor);
        ta.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                updateLineNumbers();
            }

            public void removeUpdate(DocumentEvent e) {
                updateLineNumbers();
            }

            public void changedUpdate(DocumentEvent e) {
                updateLineNumbers();
            }
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

    private JScrollPane createConsole(Color color, Color fontColor) {
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

    private JPanel createConsoleInput(Color color, Color fontColor) {
        JPanel panel = new JPanel(new BorderLayout());

        JScrollPane outputPane = createConsole(consoleColor, fontColor);

        inputField = new JTextField();
        inputField.setFont(new Font("Monospaced", Font.PLAIN, 16));
        inputField.setBackground(color);
        inputField.setForeground(fontColor);

        inputField.addActionListener(e -> {
            String input = inputField.getText();
            inputField.setText("");

            print("> " + input);
            interpreter.input(input);
        });

        panel.add(outputPane, BorderLayout.CENTER);
        panel.add(inputField, BorderLayout.SOUTH);

        return panel;
    }

    private void createLayout() {
        JScrollPane codeScrollPane = new JScrollPane(codeArea);
        JScrollPane lineScrollPane = new JScrollPane(lineNumbers);
        lineScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        lineScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        codeScrollPane.getViewport().addChangeListener(e -> {
            JViewport viewport = (JViewport) e.getSource();
            lineNumbers.scrollRectToVisible(viewport.getViewRect());
        });

        codeSplitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                lineScrollPane,
                codeScrollPane);
        codeSplitPane.setDividerSize(1);
        codeSplitPane.setDividerLocation(50);

        JPanel console = createConsoleInput(consoleInputC, fontColor);

        outputSplitPane = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                codeSplitPane,
                console);
        outputSplitPane.setResizeWeight(0.8);

        editorSplitPane = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                menu,
                outputSplitPane);
        editorSplitPane.setResizeWeight(0.01);
    }

    public void print(String out) {
        output.append(out + "\n");
        output.setCaretPosition(output.getDocument().getLength());
    }

    private void updateLineNumbers() {
        int lines = codeArea.getLineCount();
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= lines; i++)
            sb.append(i).append("\n");
        lineNumbers.setText(sb.toString());
    }

    public String getCode() {
        return codeArea.getText();
    }

    public String getProgrammName() {
        return programmNameField.getText();
    }
}
