
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;

public class LineMover extends AbstractAction {
   JTextArea ta;
   boolean up;

   /**
    * Lässt den User mit alt + ↑/↓ Code Zeile verschieben
    * 
    * @param ta Text Area, bei der es ermöglicht werden soll
    * @param up Code nach oben verschieben? Bei false nach unten
    */
   public LineMover(JTextArea ta, boolean up) {
      this.ta = ta;
      this.up = up;
   }

   @Override
   public void actionPerformed(ActionEvent e) {
      int pos = ta.getCaretPosition();
      int line;
      try {
         line = ta.getLineOfOffset(pos);
      } catch (BadLocationException err) {
         line = -1;
      }

      if ((up && line == 0) || (!up && line >= ta.getLineCount() - 1)) return;

      String code = ta.getText();
      code = code.replaceAll("\n", "\n&");
      String[] lines = code.split("\n");
      String curline = lines[line];
      int nIndex = up ? line-1 : line+1;
      lines[line] = lines[nIndex];
      lines[nIndex] = curline;
      
      String res = "";
      for (int i = 0; i < lines.length; i++){
         res+=lines[i];
         if (i < lines.length - 1) res +="\n";
      }
      res = res.replaceAll("&", "");
      ta.setText(res);
      int caretEndPos;
      try {
         caretEndPos = ta.getLineEndOffset(nIndex-1);
      } catch (Exception evr) {
         // caretEndPos = res.length();
         caretEndPos = 0;
      }
      ta.setCaretPosition(caretEndPos);
   }

}
