import java.io.*;

public class Storing {
   
   public static void saveProgramm(String name, String programm){
      boolean fileCreated = createFile(name);
      if(!fileCreated) System.out.println("Programm will be overwritten");
      overWriteProgramm(name, programm);
   }
   
   private static boolean createFile(String pName){
      try {
         File create = new File(getFileDirectory(pName));
         create.createNewFile();
         return true;
      } catch (Exception e) {
         System.out.println("File could not be created");
         // File notCreated = 
         return false;
      }
   }

   private static boolean overWriteProgramm(String name, String code){
      try {
          FileWriter overwriter = new FileWriter(getFileDirectory(name));
          overwriter.write(code);
          overwriter.close();
      } catch (Exception e) {
      }
      return false;
   }

   private static String getFileDirectory(String name){
      return "programms/"+name+".txt";
   }
}
