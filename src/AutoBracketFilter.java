import java.util.Map;
import javax.swing.JTextArea;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

/**
 * Mit KI gemacht
 * "Ich brauche einen Document Filter, der alle Klammern und String Zeichen direkt schließt"
 * Output war die Klasse (ohne Imports)
 */
class AutoBracketFilter extends DocumentFilter {

    private static final Map<String, String> PAIRS = Map.of(
        "{", "}",
        "(", ")",
        "[", "]",
        "\"", "\"",
        "'", "'"
    );

    @Override
    public void replace(FilterBypass fb, int offset, int length,
                        String text, AttributeSet attrs)
            throws BadLocationException {

        // Nur bei Einzelzeichen reagieren
        if (text != null && text.length() == 1 && PAIRS.containsKey(text)) {

            JTextArea ta = (JTextArea) fb.getDocument().getProperty("owner");
            String close = PAIRS.get(text);

            // Einfügen von öffnender + schließender Klammer
            fb.replace(offset, length, text + close, attrs);

            // Cursor zwischen die Klammern setzen
            ta.setCaretPosition(offset + 1);
            return;
        }

        super.replace(fb, offset, length, text, attrs);
    }
}
