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
