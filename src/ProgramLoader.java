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
            // JPanel p = new JPanel(new GridLayout(1, 2));
            JButton b = new JButton(programm);
            JButton del = new JButton("Delete");
            del.setBackground(Color.RED);
            del.addActionListener(e ->{deleteProgramm(programm); chooseProgramm(loadProgrammNames(), editor); frame.dispose();});
            b.setBackground(new Color(123, 179, 167));
            b.addActionListener(e -> {
                programmChosen(programm, editor);
                frame.dispose();
            });
            b.setMinimumSize(new Dimension(500, 100));
            JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, b, del);
            sp.setDividerSize(5);
            sp.setDividerLocation(0.9);
            sp.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
            panel.add(sp);
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

    private static void deleteProgramm(String name){
        try {
            Files.move(Paths.get(("programms/"+name)), Paths.get("trashbin/"+name), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
        }
    }
}
