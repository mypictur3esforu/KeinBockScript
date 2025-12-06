public class SimpleStatementType {
   ParseFunction parseFunction;
   private String type;
   //RunFunction run;

    public SimpleStatementType(String type, ParseFunction pf) {
      this.type=type;
      parseFunction=pf;
    }


    public String getType(){
      return type;
    }
   

}
