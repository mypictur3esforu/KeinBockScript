import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class SimpleStatementType {
    private final String name;
    private final Pattern pattern;
    final StatementExecutor executor;
    
    SimpleStatementType(String name, String regex, StatementExecutor executor) {
        this.name = name;
        this.pattern = Pattern.compile(regex);
        this.executor = executor;
    }
    
    String getName(){return name;}
    
    Pattern getPattern() {
        return pattern;
    }

    private static String stripQuotes(String s) {
        if (s.startsWith("'") && s.endsWith("'")) {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }
    
    public static SimpleStatementType[] getSimpleStatements(){
        final String varRegex = "[a-zA-Z]+", numReg = "[0-9]+", stringReg = "'[^']*'", boolReg = "true|false", termReg = "";
        SimpleStatementType[] simpleStatementTypes = {
        new SimpleStatementType(
        "set",
        "num ("+varRegex+") = ("+numReg+")",
        (matcher, runtime) -> {
            String varName = matcher.group(1);
            double value = Double.parseDouble(matcher.group(2));
            runtime.set(varName, value);
        }),
        new SimpleStatementType(
        "set",
        "string ("+varRegex+") = ("+stringReg+")",
        (matcher, runtime) -> {
            String varName = matcher.group(1);
            String value = stripQuotes(matcher.group(2));
            runtime.set(varName, value);
        }),
        new SimpleStatementType(
        "set",
        "boolean ("+varRegex+") = ("+boolReg+")",
        (matcher, runtime) -> {
            String varName = matcher.group(1);
            boolean value = Boolean.parseBoolean(matcher.group(2));
            runtime.set(varName, value);
        }),
        new SimpleStatementType(
        "setVar",
        "("+varRegex+") = ("+varRegex+")",
        (matcher, runtime) -> {
            String var1 = matcher.group(1);
            String var2 = matcher.group(2);
            runtime.set(var1, runtime.get(var2));
        }),
        new SimpleStatementType(
        "add",
        "("+varRegex+") \\+= ("+numReg+")",
        (matcher, runtime) -> {
            String varName = matcher.group(1);
            if (!isNumber(runtime, varName)) throw new RuntimeException("!Num += Number");
            double add = Double.parseDouble(matcher.group(2));
            runtime.set(varName, runtime.get(varName).getNumber() + add);
        }),
        new SimpleStatementType(
        "add String",
        "("+varRegex+") \\+= ("+stringReg+")",
        (matcher, runtime) -> {
            String varName = matcher.group(1);
            if (!isString(runtime, varName)) throw new RuntimeException("!String += String");
            String add = stripQuotes(matcher.group(2));
            String temp = runtime.get(varName).getString() + add;
            runtime.set(varName, runtime.get(varName).getString() + add);
        }),
        new SimpleStatementType(
            "addVar",
            "("+varRegex+") \\+= ("+varRegex+")",
            (matcher, runtime) -> {
                String a = matcher.group(1);
                String b = matcher.group(2);
                Value v1 = runtime.get(a);
                Value v2 = runtime.get(b);
                if (v1.getValueType() == ValueType.BOOLEAN || v1.getValueType() != v2.getValueType()) throw new RuntimeException("Incompatible datatypes");
                else if (v1.getValueType() == ValueType.NUMBER) runtime.set(a, v2.getNumber() + v1.getNumber());
                else if (v1.getValueType() == ValueType.STRING) runtime.set(a, v1.getString() + v2.getString());
                
        }),
        new SimpleStatementType(
        "sub",
        "("+varRegex+") -= ("+numReg+")",
        (matcher, runtime) -> {
            String varName = matcher.group(1);
            if (!isNumber(runtime, varName)) throw new RuntimeException("Not a number var");
            double sub = Double.parseDouble(matcher.group(2));
            runtime.set(varName, runtime.get(varName).getNumber() - sub);
        }),
        new SimpleStatementType(
        "mul",
        "("+varRegex+") \\*= ("+numReg+")",
        (matcher, runtime) -> {
            String varName = matcher.group(1);
            if (!isNumber(runtime, varName)) throw new RuntimeException("Not a number var");
            double mul = Double.parseDouble(matcher.group(2));
            runtime.set(varName, runtime.get(varName).getNumber() * mul);
        }),
        new SimpleStatementType(
        "div",
        "("+varRegex+") /= ("+numReg+")",
        (matcher, runtime) -> {
            String varName = matcher.group(1);
            if (!isNumber(runtime, varName)) throw new RuntimeException("Not a number var");
            double div = Double.parseDouble(matcher.group(2));
            runtime.set(varName, runtime.get(varName).getNumber() / div);
        }),
        new SimpleStatementType(
        "mod",
        "("+varRegex+") %= ("+numReg+")",
        (matcher, runtime) -> {
            String varName = matcher.group(1);
            if (!isNumber(runtime, varName)) throw new RuntimeException("Not a number var");
            double mod = Double.parseDouble(matcher.group(2));
            runtime.set(varName, runtime.get(varName).getNumber() % mod);
        }),
        new SimpleStatementType(
        "inc",
        "("+varRegex+")\\+\\+",
        (matcher, runtime) -> {
            String varName = matcher.group(1);
            if (!isNumber(runtime, varName)) throw new RuntimeException("Not a number var");
            runtime.set(varName, runtime.get(varName).getNumber() + 1);
        }),
        new SimpleStatementType(
        "dec",
        "("+varRegex+")--",
        (matcher, runtime) -> {
            String varName = matcher.group(1);
            if (!isNumber(runtime, varName)) throw new RuntimeException("Not a number var");
            runtime.set(varName, runtime.get(varName).getNumber() - 1);
        }),
        new SimpleStatementType(
        "string array",
        "string\\[\\] ("+varRegex+") ?= ?\\[ ?(("+stringReg+", ?)*"+stringReg+")\\]",
        // "(num|string|boolean)[] "+varRegex+" ?= ?new (num|string|boolean)([ ?"+numReg+" ?]|[]{"+numReg+"|"+stringReg+"|"+boolReg+"})",
        (matcher, runtime) -> {
            String varName = matcher.group(1);
            String arrVs = matcher.group(2);
            String[] values = arrVs.split(",");
            ArrayList<Value> vList = new ArrayList();
            for (String value : values){
                Value v = new Value(stripQuotes(value));
                vList.add(v);
            }
            runtime.set(varName, new Value(vList));
        }),
        new SimpleStatementType(
            "number array",
        "num\\[\\] ("+varRegex+") ?= ?\\[(("+numReg+" ?,)*"+numReg+")?\\]",
        // "(num|string|boolean)[] "+varRegex+" ?= ?new (num|string|boolean)([ ?"+numReg+" ?]|[]{"+numReg+"|"+stringReg+"|"+boolReg+"})",
        (matcher, runtime) -> {
            String varName = matcher.group(1);
            String arrVs = matcher.group(2);
            ArrayList<Value> vList = new ArrayList();
            if (arrVs != null){
                String[] values = arrVs.split(",");
                for (String value : values){
                    Value v = new Value(Double.parseDouble(value));
                    vList.add(v);
                }}
            runtime.set(varName, new Value(vList));
        }),
        new SimpleStatementType(
        "boolean array",
        "boolean\\[\\] ("+varRegex+") ?= ?\\[(("+boolReg+" ?,)*"+boolReg+")\\]",
        // "(num|string|boolean)[] "+varRegex+" ?= ?new (num|string|boolean)([ ?"+numReg+" ?]|[]{"+numReg+"|"+stringReg+"|"+boolReg+"})",
        (matcher, runtime) -> {
            String varName = matcher.group(1);
            String arrVs = matcher.group(2);
            String[] values = arrVs.split(",");
            ArrayList<Value> vList = new ArrayList();
            for (String value : values){
                Value v = new Value(Boolean.parseBoolean((value)));
                vList.add(v);
            }
            runtime.set(varName, new Value(vList));
        }),
        // var1 = var2[index]; var 1 muss definiert sein, damit Variable Typ klar ist (bisschen faule Lösung) 
        new SimpleStatementType("array Value by index", "("+varRegex+") ?= ?("+varRegex+")\\[("+numReg+"|"+varRegex+")\\]", (matcher, runtime)->{
            String varName = matcher.group(1);
            String varName2 = matcher.group(2);
            String indexer = matcher.group(3);
            Pattern numPattern = Pattern.compile(numReg);
            Matcher numPM = numPattern.matcher(indexer);
            int index;
            if (numPM.matches()) index = Integer.parseInt(indexer);
            else index = (int) runtime.get(indexer).getNumber();
            Value v = runtime.get(varName2).getValueFromArray(index);
            Value varV = runtime.get(varName);
            if (varV.getValueType() == ValueType.STRING)runtime.set(varName, v.getString());
            if (varV.getValueType() == ValueType.BOOLEAN)runtime.set(varName, v.getBoolean());
            if (varV.getValueType() == ValueType.NUMBER)runtime.set(varName, v.getNumber());
        }),
        new SimpleStatementType("array split", "string\\[\\] ("+varRegex+") ?= ?("+varRegex+")\\.split\\(("+stringReg+")\\)", (matcher, runtime) ->{
            String arrName = matcher.group(1);
            String varName = matcher.group(2);
            String regex = stripQuotes(matcher.group(3));

            ArrayList<Value> parts = new ArrayList<>();
            for (String part : runtime.get(varName).getString().split(regex)){
                parts.add(new Value(part));
            }
            runtime.set(arrName, new Value(parts));
        }),
        new SimpleStatementType("array length", "num ("+varRegex+") ?= ?("+varRegex+")\\.length", (matcher, runtime)->{
            String varName = matcher.group(1);
            String arrName = matcher.group(2);
            runtime.set(varName, new Value(runtime.get(arrName).getArray().size()));
        }),
        new SimpleStatementType(
    "array add last",
    "("+varRegex+")\\.add\\(("+numReg+"|"+stringReg+"|"+boolReg+"|"+varRegex+")\\)",
    (matcher, runtime) -> {
        String arrName = matcher.group(1);
        Value arr = runtime.get(arrName);
        String g2 = matcher.group(2);
        if (Pattern.matches(varRegex, g2)) arr.getArray().add(runtime.get(g2));
        else if (Pattern.matches(stringReg, g2)) arr.getArray().add(new Value(stripQuotes(g2)));
        else if (Pattern.matches(numReg, g2)) arr.getArray().add(new Value(Double.parseDouble(g2)));
        else if (Pattern.matches(boolReg, g2)) arr.getArray().add(new Value(Boolean.parseBoolean(g2)));
        //     switch(arr.getValueType()){
        //         case ValueType.STRING -> { arr.getArray().add(new Value(stripQuotes(g2)));}
        //         case ValueType.NUMBER -> { arr.getArray().add(new Value(Double.parseDouble(g2)));}
        //         case ValueType.BOOLEAN -> { arr.getArray().add(new Value(Boolean.parseBoolean(g2)));}
        //         case ValueType.ARRAY -> { arr.getArray().add(runtime.get(g2));}
        // }
    }),
    new SimpleStatementType(
    "array add index",
    "("+varRegex+")\\.add\\(("+numReg+"|"+varRegex+"), ?("+numReg+"|"+stringReg+"|"+boolReg+"|"+varRegex+")\\)",
    (matcher, runtime) -> {
        String arrName = matcher.group(1);
        String indexRaw = matcher.group(2);
        String value = stripQuotes(matcher.group(3));

        Value arr = runtime.get(arrName);

        int index = indexRaw.matches(numReg)
                ? Integer.parseInt(indexRaw)
                : (int) runtime.get(indexRaw).getNumber();

        arr.getArray().add(index, new Value(value));
    }),
    new SimpleStatementType(
        "array contains element",
        "boolean ("+varRegex+") ?= ("+varRegex+")\\.contains\\(("+numReg+"|"+stringReg+"|"+boolReg+"|"+varRegex+")\\)",
        (matcher, runtime) -> {
            String var1 = matcher.group(1);
            String arrName = matcher.group(2);
            Value arr = runtime.get(arrName);
            String g3 = matcher.group(3);
            boolean con = false;
            // Funktioniert nur mit Variablen, weil bei Werten new Value() niemals ein vorhandes Objekt sein kann
            if (Pattern.matches(varRegex, g3)) con = arr.getArray().contains(runtime.get(g3));
            // else if (Pattern.matches(stringReg, g3)) con =  arr.getArray().contains(new Value(stripQuotes(g3)));
            // else if (Pattern.matches(numReg, g3)) con = arr.getArray().contains(new Value(Double.parseDouble(g3)));
            // else if (Pattern.matches(boolReg, g3)) con = arr.getArray().contains(new Value(Boolean.parseBoolean(g3)));
            runtime.set(var1, con);
        }),
    new SimpleStatementType("random num", "num ("+varRegex+") ?= ?random\\(("+numReg+"|"+varRegex+"),("+numReg+"|"+varRegex+")\\)",
        (matcher,runtime) ->{
            String varName = matcher.group(1);
            double min = Pattern.matches(varRegex, matcher.group(2)) ? runtime.get(matcher.group(2)).getNumber() : Double.parseDouble(matcher.group(2));
            double max = Pattern.matches(varRegex, matcher.group(3)) ? runtime.get(matcher.group(3)).getNumber() : Double.parseDouble(matcher.group(3));
            // double max = Double.parseDouble(matcher.group(3));
            double rnum = min<max ? min -1 : null;
            while(min > rnum) rnum = Math.round(Math.random()*max);
            runtime.set(varName, rnum);
        }),
        new SimpleStatementType(
    "varMathOp",
    "("+varRegex+") ?(\\+=|-=|\\*=|/=|%=) ?("+varRegex+")",
    (matcher, runtime) -> {
        String var1 = matcher.group(1);
        String operand = matcher.group(2);
        String var2 = matcher.group(3);

        double a = runtime.get(var1).getNumber();
        double b = runtime.get(var2).getNumber();

        switch (operand) {
            case "+=" -> runtime.set(var1, a + b);
            case "-=" -> runtime.set(var1, a - b);
            case "*=" -> runtime.set(var1, a * b);
            case "/=" -> runtime.set(var1, a / b);
            case "%=" -> runtime.set(var1, a % b);
        }}),

    };
    return simpleStatementTypes;
    }

    private static boolean isNumber(Runtime runtime, String varName){
        Value value = runtime.get(varName);
        return value.getValueType() == ValueType.NUMBER;}
    private static boolean isString(Runtime runtime, String varName){
        Value value = runtime.get(varName);
        return value.getValueType() == ValueType.STRING;}
    private static boolean isBoolean(Runtime runtime, String varName){
        Value value = runtime.get(varName);
        return value.getValueType() == ValueType.BOOLEAN;}
}
