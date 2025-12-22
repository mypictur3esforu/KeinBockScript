
import java.util.regex.*;

public class Interpreter {

    private final String code;
    private final Runtime runtime;
    private Editor editor;
    private int holdLine = -1;
    private int[] emptyLineOffset;
    private String userInput;

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
         )//,
        //  new SimpleStatementType("userInput", ,
        // (matcher, runtime) -> {
        //     String funcName = matcher.group(1);
        //     // Hier anhalten
        // }
        // )
    };

    public static Interpreter execute(Editor editor, String code) {
        Interpreter i = new Interpreter(editor, code);
        return i;
        // System.out.println(i.getRuntime().get("x"));
    }

    public void print(String output) {
        editor.print(output);
    }

    public Interpreter(Editor editor, String code) {
        this.runtime = new Runtime();
        this.editor = editor;
        this.code = extractCode(code);
        saveFunctions(this.code);
        interpret(this.code);
    }

    public Runtime getRuntime() {
        return runtime;
    }

    public void input(String input){
        if (holdLine == -1) return;
        userInput = input;
        interpret(code);
    }

    private String extractCode(String code) {
        emptyLineOffset = new int[code.trim().split("\n").length -1];
        StringBuilder result = new StringBuilder();
        int emptyLines = 0, i = 0;
        for (String line : code.split("\n")) {
            if (!line.isBlank()) {
                result.append(line.trim()).append("\n");
                emptyLineOffset[i] = emptyLines;
                i++;
            }else{
                emptyLines++;}
        }
        return result.toString().trim();
    }

    private synchronized void interpret(String code) {
        String[] lines = code.split("\n");
        // String[] lines = this.code.split("\n");

        Pattern oBracket = Pattern.compile(".*\\{.*", Pattern.MULTILINE);
        for (int i = 0; i < lines.length; i++) {
            boolean matched = false;
            if (i < holdLine) continue;

            String[] promptVar = checkPrompt(lines[i]);
            if( promptVar != null) {
                if (holdLine == -1){
                    print(promptVar[1]);
                    holdLine = i;
                    break;
                } else{
                    holdLine = -1;
                    runtime.set(promptVar[0], Double.parseDouble(userInput));
                    continue;
                }
            }
            // Wenn irgendwas mit Klammer kommt (if, functions)
            Matcher oBracketM = oBracket.matcher(lines[i]);
            if (oBracketM.matches()) {
                int end = findClosebracket(lines, i);
                String area = linesToCode(lines, i, end);

                // Schauen was mit Klammer gemacht werden soll
                if (checkIfCondition(lines[i])) interpret(area);
                i = end;
                continue;
            }

            // Einfach onliner Statements
            for (SimpleStatementType type : simpleStatementTypes) {
                Matcher matcher = type.getPattern().matcher(lines[i]);
                if (matcher.matches()) {
                    type.executor.execute(matcher, runtime);
                    matched = true;
                    break;
                }
            }

            if (!matched) {
                // throw new RuntimeException("Unknown statement: " + lines[i]);
                throwException(i, "Unknown Statement", lines[i]);
            }
        }
    }

    private String linesToCode(String[] lines, int start, int end) {
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
            if (oBracketM.matches()) {
                level++;
            }
            if (cBracketM.matches()) {
                level--;
            }
            if (level == 0) {
                return i;
            }
        }
        // throw new RuntimeException("Unclosed bracket " + lines[openingIndex]);
        throwException(openingIndex, "Unclosed Bracket", lines[openingIndex]);
        return -1; // Wird niemals ausgeführt
    }

    /**
     * Überprüft, ob die Ziele ein prompt Befehl ist.
     * @param line Zeile
     * @return line != prompt Befehl -> null; line == prompt -> [varName, PrintBefehl]
     */
    private String[] checkPrompt(String line){
        Pattern promptPattern = Pattern.compile("([a-zA-Z]) ?= ?prompt ?\\( ?'([a-zA-Z ]+)' ?\\)");
        Matcher pm = promptPattern.matcher(line);
        if (pm.matches()) return new String[]{pm.group(1), pm.group(2)};
        else return null;
    }

    private boolean checkIfCondition(String ifStatement) {
        // if (ifStatement.equals("{"))
        //     return true;
        Pattern ifPattern = Pattern.compile("if ?\\( ?([a-zA-Z]+) ?(==|!=|>=|<=|>|<) ?([0-9]+) ?\\) ?\\{");
        Matcher mif = ifPattern.matcher(ifStatement);
        if (!mif.matches()) {
            return false;
        }
        // throw new RuntimeException("Wrong if statement:");

        String varName = mif.group(1);
        String operator = mif.group(2);
        double compareValue = Double.parseDouble(mif.group(3));

        double varValue = runtime.get(varName);
        boolean condition = switch (operator) {
            case "==" ->
                varValue == compareValue;
            case "!=" ->
                varValue != compareValue;
            case ">" ->
                varValue > compareValue;
            case "<" ->
                varValue < compareValue;
            case ">=" ->
                varValue >= compareValue;
            case "<=" ->
                varValue <= compareValue;
            default ->
                false;
        };
        return condition;
    }

    // private boolean checkFunction(String line){
    //     Pattern funcPattern = Pattern.compile("func [a-zA-Z][a-zA-Z0-9]* ?\\{");
    //     Matcher mfunc = funcPattern.matcher(line);
    //     return mfunc.matches();
    // }
    private void saveFunctions(String code) {
        String[] lines = code.split("\n");
        Pattern function = Pattern.compile("func ([a-zA-Z][a-zA-Z0-9]*)\\(\\) ?\\{");
        for (int i = 0; i < lines.length; i++) {
            Matcher mfunc = function.matcher(lines[i]);
            if (!mfunc.matches()) {
                continue;
            }

            int closeBracket = findClosebracket(lines, i);
            runtime.setFunction(mfunc.group(1), linesToCode(lines, i, closeBracket));
        }
    }

    private void throwException(int lineNumber, String error, String line) throws RuntimeException{
        lineNumber += emptyLineOffset[lineNumber] + 1;
        editor.setErrorLine(lineNumber);
        throw new RuntimeException("Line " + lineNumber + ": "+error+": "+ line);
    }

}
