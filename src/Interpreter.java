
import java.util.HashMap;
import java.util.regex.*;

public class Interpreter {
   private String code;
   private Runtime runtime = new Runtime();

   SimpleStatementType[] simpleStatementTypes=new SimpleStatementType[]{
      new SimpleStatementType("set",(line, runtime)->{
         // System.out.println("Halo");
         Pattern p = Pattern.compile("set ([a-zA-Z]*) = ([0-9]+)");
         Matcher m = p.matcher(line);
         String varName = "";
         double value = 0;
         while(m.find()){
            varName = m.group(1);
            value = Double.parseDouble(m.group(2));
            System.out.println("Variable: " + m.group(1) + " to: " + m.group(2));
         }
         return new SimpleStatement();
      })
   };

   public static void execute(String code){
      Interpreter interpreter = new Interpreter(code);
   }

   public Interpreter(String code){

      this.code = extractCode(code);
      // System.out.println("Interpreter Code: "+ this.code);

      interprete(this.code);
   }

   /**
    * Löscht alles unnötige, wie leere Zeilen
    * @param code Roher user input
    * @return Essenzieller Code
    */
   private String extractCode(String code){
      StringBuilder result = new StringBuilder();
      for (String line : code.split("\n")) {
          if (!line.isBlank()) {
              result.append(line).append("\n");
          }
      }
      return result.toString().trim();
   }

   private void interprete(String code){
      String[] lines = code.split("\n");
      // simpleStatementTypes[0].parseFunction.parse(lines[0]);
      for (String line : lines){
         for (SimpleStatementType type : simpleStatementTypes){
            Pattern typePat = Pattern.compile(type.getType());
            Matcher matcher = typePat.matcher(line);
            if(matcher.find()) type.parseFunction.parse(line);
         }
      }
   }

   // private smth getCommand(String line){
      
   // }
}
