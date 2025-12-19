import java.util.regex.*;

public class Interpreter {

    private final String code;
    private final Runtime runtime;
    private Editor editor;

SimpleStatementType[] simpleStatementTypes = {
        new SimpleStatementType(
                "set",
                "set ([a-zA-Z]+) = ([0-9]+)",
                (matcher, runtime) -> {
                    String varName = matcher.group(1);
                    double value = Double.parseDouble(matcher.group(2));
                    runtime.set(varName, value);
                }),

        new SimpleStatementType(
                "setVar",
                "set ([a-zA-Z]+) = ([a-zA-Z]+)",
                (matcher, runtime) -> {
                    String target = matcher.group(1);
                    String source = matcher.group(2);
                    runtime.set(target, runtime.get(source));
                }),

        new SimpleStatementType(
                "add",
                "([a-zA-Z]+) \\+= ([0-9]+)",
                (matcher, runtime) -> {
                    String varName = matcher.group(1);
                    double add = Double.parseDouble(matcher.group(2));
                    runtime.set(varName, runtime.get(varName) + add);
                }),

        new SimpleStatementType(
                "addVar",
                "([a-zA-Z]+) \\+= ([a-zA-Z]+)",
                (matcher, runtime) -> {
                    String a = matcher.group(1);
                    String b = matcher.group(2);
                    runtime.set(a, runtime.get(a) + runtime.get(b));
                }),

        new SimpleStatementType(
                "sub",
                "([a-zA-Z]+) -= ([0-9]+)",
                (matcher, runtime) -> {
                    String varName = matcher.group(1);
                    double sub = Double.parseDouble(matcher.group(2));
                    runtime.set(varName, runtime.get(varName) - sub);
                }),

        new SimpleStatementType(
                "mul",
                "([a-zA-Z]+) \\*= ([0-9]+)",
                (matcher, runtime) -> {
                    String varName = matcher.group(1);
                    double mul = Double.parseDouble(matcher.group(2));
                    runtime.set(varName, runtime.get(varName) * mul);
                }),

        new SimpleStatementType(
                "div",
                "([a-zA-Z]+) /= ([0-9]+)",
                (matcher, runtime) -> {
                    String varName = matcher.group(1);
                    double div = Double.parseDouble(matcher.group(2));
                    runtime.set(varName, runtime.get(varName) / div);
                }),

        new SimpleStatementType(
                "mod",
                "([a-zA-Z]+) %= ([0-9]+)",
                (matcher, runtime) -> {
                    String varName = matcher.group(1);
                    double mod = Double.parseDouble(matcher.group(2));
                    runtime.set(varName, runtime.get(varName) % mod);
                }),

        new SimpleStatementType(
                "inc",
                "([a-zA-Z]+)\\+\\+",
                (matcher, runtime) -> {
                    String varName = matcher.group(1);
                    runtime.set(varName, runtime.get(varName) + 1);
                }),

        new SimpleStatementType(
                "dec",
                "([a-zA-Z]+)--",
                (matcher, runtime) -> {
                    String varName = matcher.group(1);
                    runtime.set(varName, runtime.get(varName) - 1);
                }),

        new SimpleStatementType(
                "printVar",
                "print\\(([a-zA-Z]+)\\)",
                (matcher, runtime) -> {
                    String varName = matcher.group(1);
                    print(String.valueOf(runtime.get(varName)));
                }),

        new SimpleStatementType(
                "printString",
                "print\\('([^']*)'\\)",
                (matcher, runtime) -> {
                    print(matcher.group(1));
                }),

        new SimpleStatementType(
                "printStringVar",
                "print\\('([^']*)' \\+ ([a-zA-Z]+)\\)",
                (matcher, runtime) -> {
                    print(matcher.group(1) + runtime.get(matcher.group(2)));
                }),
        new SimpleStatementType("functionCall", "([a-zA-Z][a-zA-Z0-9]*)\\( ?\\)", 
            (matcher, runtime) -> {
                String funcName = matcher.group(1);
                interpret(runtime.getFunction(funcName));
            }
        )
};

    public static void execute(Editor editor, String code) {
        Interpreter i = new Interpreter(editor, code);
        // System.out.println(i.getRuntime().get("x"));
    }

    public void print(String output) {
        editor.print(output);
    }

    public Interpreter(Editor editor, String code) {
        this.runtime = new Runtime();
        this.editor = editor;
        this.code = extractCode(code);
        saveFunctions(code);
        interpret(this.code);
    }

    public Runtime getRuntime() {
        return runtime;
    }

    private String extractCode(String code) {
        StringBuilder result = new StringBuilder();
        for (String line : code.split("\n")) {
            if (!line.isBlank()) {
                result.append(line.trim()).append("\n");
            }
        }
        return result.toString().trim();
    }
    
    private void interpret(String code) {
        String[] lines = code.split("\n");
        // System.out.println(lines.length);
        
        Pattern oBracket = Pattern.compile(".*\\{.*", Pattern.MULTILINE);
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            boolean matched = false;

            // Wenn irgendwas mit Klammer kommt (if, functions)
            Matcher oBracketM = oBracket.matcher(line);
            if (oBracketM.matches()) {
                int end = findClosebracket(lines, i);
                String area = linesToCode(lines, i, end);
                
                // Schauen was mit Klammer gemacht werden soll
                if (checkIfCondition(lines[i]))interpret(area);
                i = end;
                continue;
            }
            
            // Einfach onliner Statements
            for (SimpleStatementType type : simpleStatementTypes) {
                Matcher matcher = type.getPattern().matcher(line);
                if (matcher.matches()) {
                    type.executor.execute(matcher, runtime);
                    matched = true;
                    break;
                }
            }
            
            if (!matched) {
                throw new RuntimeException("Unknown statement: " + line);
            }
        }
    }
    
    private String linesToCode(String[] lines, int start, int end){
        String area = "";
        for (int i = start + 1; i < end; i++) {
            area += lines[i] + "\n";
        }
        return area;
    }
    
    private int findClosebracket(String[] lines, int openingIndex) {
        int level = 0;
        Pattern cBracket = Pattern.compile(".*\\}", Pattern.MULTILINE);
        Pattern oBracket = Pattern.compile(".*\\{.*", Pattern.MULTILINE);
        for (int i = openingIndex; i < lines.length; i++) {
            Matcher oBracketM = oBracket.matcher(lines[i]);
            Matcher cBracketM = cBracket.matcher(lines[i]);
            if (oBracketM.matches())
                level++;
            if (cBracketM.matches())
                level--;
            if (level == 0)
                return i;
        }
        throw new RuntimeException("Unclosed bracket " + lines[openingIndex]);
        // return -1;
    }

    private boolean checkIfCondition(String ifStatement) {
        // if (ifStatement.equals("{"))
        //     return true;
        Pattern ifPattern = Pattern.compile("if ?\\( ?([a-zA-Z]+) ?(==|!=|>=|<=|>|<) ?([0-9]+) ?\\) ?\\{");
        Matcher mif = ifPattern.matcher(ifStatement);
        if (!mif.matches())
            return false;
            // throw new RuntimeException("Wrong if statement:");

        String varName = mif.group(1);
        String operator = mif.group(2);
        double compareValue = Double.parseDouble(mif.group(3));

        double varValue = runtime.get(varName);
        boolean condition = switch (operator) {
            case "==" -> varValue == compareValue;
            case "!=" -> varValue != compareValue;
            case ">" -> varValue > compareValue;
            case "<" -> varValue < compareValue;
            case ">=" -> varValue >= compareValue;
            case "<=" -> varValue <= compareValue;
            default -> false;
        };
        return condition;
    }

    // private boolean checkFunction(String line){
    //     Pattern funcPattern = Pattern.compile("func [a-zA-Z][a-zA-Z0-9]* ?\\{");
    //     Matcher mfunc = funcPattern.matcher(line);
    //     return mfunc.matches();
    // }

    private void saveFunctions(String code){
        String[] lines = code.split("\n");
        Pattern function = Pattern.compile("func ([a-zA-Z][a-zA-Z0-9]*)\\(\\) ?\\{");
        for (int i = 0; i < lines.length; i++) {
            Matcher mfunc = function.matcher(lines[i]);
            if (!mfunc.matches()) continue;

            int closeBracket = findClosebracket(lines, i);
            runtime.setFunction(mfunc.group(1), linesToCode(lines, i, closeBracket));
        }
    }
}
