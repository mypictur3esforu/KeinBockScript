import java.awt.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class ProgramLoader {

    /**
     * Lädt alle Programm Name und gibt sie zurück
     * @return Programm Namen als String[]
     * @throws IOException
     */
    private static String[] loadProgrammNames() {
        Path dir = Paths.get("./programms");
        List<String> names = new ArrayList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path file : stream) {
                if (Files.isRegularFile(file)) {
                    names.add(file.getFileName().toString());
                }
            }
        }catch (IOException e){
            throw new RuntimeException("Programms are not lodable");
        }

        return names.toArray(new String[0]);
    }


    /**
     * Lässt den User ein Programm aussuchen
     */
    public static void getDesiredProgramm(Editor editor){
        String[] programms = loadProgrammNames();
        chooseProgramm(programms, editor);
    }

    private static void chooseProgramm(String[] programs, Editor editor){
        JFrame frame = new JFrame();
        JPanel panel = new JPanel(new GridLayout((int) (programs.length * 0.6), (int) (programs.length * 0.4)));
        for (String programm : programs){
            JButton b = new JButton(programm);
            b.setBackground(new Color(123, 179, 167));
            b.addActionListener(e -> {
                programmChosen(programm, editor);
                frame.dispose();
            });
            panel.add(b);
        }
        frame.setVisible(true);
        frame.setExtendedState(Frame.MAXIMIZED_BOTH);
        frame.add(panel);
    }

    private static void programmChosen(String programm, Editor editor){
        editor.loadProgramm(programm, getCode(programm));
    }

    private static String getCode(String name){
        try{
            String code = Files.readString(Path.of("programms/"+name));
            return code;
        }catch (Exception e){System.out.println("File not readable"); return null;}
    }
}
