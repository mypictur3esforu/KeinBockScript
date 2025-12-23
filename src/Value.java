
public class Value {
   private ValueType vt;
   private String string;
   private double number;
   private boolean bool;

   Value(String string) {
      vt = ValueType.STRING;
      this.string = string;
   }

   Value(double number) {
      vt = ValueType.NUMBER;
      this.number = number;
   }

   Value(boolean bool) {
      vt = ValueType.BOOLEAN;
      this.bool = bool;
   }

  @Override
    public String toString() {
        return switch (vt) {
            case NUMBER -> Double.toString(number);
            case STRING -> string;
            case BOOLEAN -> Boolean.toString(bool);
        };
    }

    public ValueType getValueType(){ return vt;}
    
    public double getNumber() {
      if (vt != ValueType.NUMBER) throw new RuntimeException(vt + " can not be used as " + ValueType.NUMBER);
      return number;
   }
   
   public String getString() {
       if (vt != ValueType.STRING) throw new RuntimeException(vt + " can not be used as " + ValueType.STRING);
       return string;
      }
      
   public boolean getBoolean() {
       if (vt != ValueType.BOOLEAN) throw new RuntimeException(vt + " can not be used as " + ValueType.BOOLEAN);
      return bool;
    }
}