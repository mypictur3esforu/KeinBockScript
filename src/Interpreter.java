public class Interpreter {
   private String code;

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
   }

   private smth getCommand(String line){
      
   }
}
