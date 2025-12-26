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
            String value = matcher.group(2);
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
        "num ("+varRegex+") = ("+varRegex+")",
        (matcher, runtime) -> {
            String var1 = matcher.group(1);
            String var2 = matcher.group(2);
            runtime.equalize(var1, var2);
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
        "add",
        "("+varRegex+") \\+= ("+stringReg+")",
        (matcher, runtime) -> {
            String varName = matcher.group(1);
            if (!isString(runtime, varName)) throw new RuntimeException("!String += String");
            String add = (matcher.group(2));
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
                else if (v1.getValueType() == ValueType.STRING) runtime.set(a, v2.getString() + v1.getString());
                
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
                Value v = new Value(value);
                vList.add(v);
            }
            runtime.set(varName, new Value(vList));
        }),
        new SimpleStatementType(
            "number array",
        "num\\[\\] ("+varRegex+") ?= ?\\[(("+numReg+" ?,)*"+numReg+")\\]",
        // "(num|string|boolean)[] "+varRegex+" ?= ?new (num|string|boolean)([ ?"+numReg+" ?]|[]{"+numReg+"|"+stringReg+"|"+boolReg+"})",
        (matcher, runtime) -> {
            String varName = matcher.group(1);
            String arrVs = matcher.group(2);
            String[] values = arrVs.split("\n");
            ArrayList<Value> vList = new ArrayList();
            for (String value : values){
                Value v = new Value(Double.parseDouble(value));
                vList.add(v);
            }
            runtime.set(varName, new Value(vList));
        }),
        new SimpleStatementType(
        "boolean array",
        "boolean\\[\\] ("+varRegex+") ?= ?\\[(("+boolReg+" ?,)*"+boolReg+")\\]",
        // "(num|string|boolean)[] "+varRegex+" ?= ?new (num|string|boolean)([ ?"+numReg+" ?]|[]{"+numReg+"|"+stringReg+"|"+boolReg+"})",
        (matcher, runtime) -> {
            String varName = matcher.group(1);
            String arrVs = matcher.group(2);
            String[] values = arrVs.split("\n");
            ArrayList<Value> vList = new ArrayList();
            for (String value : values){
                Value v = new Value(Boolean.parseBoolean((value)));
                vList.add(v);
            }
            runtime.set(varName, new Value(vList));
        }),
        new SimpleStatementType("arrayValue", "("+varRegex+") ?= ?("+varRegex+")\\[("+numReg+"|"+varRegex+")\\]", (matcher, runtime)->{
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
        new SimpleStatementType("array split", "string ("+varRegex+") ?= ?("+varRegex+")\\.split\\(("+stringReg+")\\)", (matcher, runtime) ->{
            String arrName = matcher.group(1);
            String varName = matcher.group(2);
            String regex = matcher.group(3);

            ArrayList<Value> parts = new ArrayList<>();
            for (String part : runtime.get(varName).getString().split(regex)){
                parts.add(new Value(part));
            }
            runtime.set(arrName, new Value(parts));
        }),
        new SimpleStatementType("array length", "num ("+varRegex+") ?= ?("+varRegex+")\\.length", (matcher, runtime)->{
            String varName = matcher.group(1);
            String arrName = matcher.group(2);
            runtime.set(varName, runtime.get(arrName).getArray().size());
        })
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
