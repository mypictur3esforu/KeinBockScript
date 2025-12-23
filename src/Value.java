
import java.util.ArrayList;


public class Value {
   private ValueType vt;
   private String string;
   private double number;
   private boolean bool;
   private ArrayList<Value> array;

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

   Value(ArrayList<Value> array) {
      vt = ValueType.ARRAY;
      this.array = array;
   }

  @Override
    public String toString() {
        return switch (vt) {
            case NUMBER -> Double.toString(number);
            case STRING -> string;
            case BOOLEAN -> Boolean.toString(bool);
            case ARRAY -> array.toString();
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
   public Value getValueFromArray(int index) {
      //  if (vt != ValueType.BOOLEAN) throw new RuntimeException(vt + " can not be used as " + ValueType.BOOLEAN);
      return array.get(index);
    }
}