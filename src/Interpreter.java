
import java.util.regex.*;

public class Interpreter {

    private final String code;
    private final Runtime runtime;
    private Editor editor;
    private int holdLine = -1, holdDebugLine = -1;
    private int[] emptyLineOffset;
    private boolean debugger = false;
    private String userInput;
    private SimpleStatementType[] simpleStatementTypes = getSimpleStatementTypes();

    private final String varRegex = "[a-zA-Z]+", numReg = "[0-9]+", stringReg = "'[^']*'", boolReg = "true|false", termReg = "";


   private SimpleStatementType[] getSimpleStatementTypes() {

    String varRegex = "[a-zA-Z]+";

    SimpleStatementType[] base =
            SimpleStatementType.getSimpleStatements();

    SimpleStatementType[] result =
            new SimpleStatementType[base.length + 4];

    System.arraycopy(base, 0, result, 0, base.length);

    result[base.length] = new SimpleStatementType(
        "printVar",
        "print\\(("+varRegex+")\\)",
        (matcher, runtime) -> {
            String varName = matcher.group(1);
            print(runtime.get(varName).toString());
        }
    );

    result[base.length + 1] = new SimpleStatementType(
        "printString",
        "print\\('([^']*)'\\)",
        (matcher, runtime) -> {
            print(matcher.group(1));
        }
    );

    result[base.length + 2] = new SimpleStatementType(
        "printStringVar",
        "print\\('([^']*)' \\+ ("+varRegex+")\\)",
        (matcher, runtime) -> {
            print(matcher.group(1) + runtime.get(matcher.group(2)));
        }
    );

    result[base.length + 3] = new SimpleStatementType(
        "functionCall",
        "([a-zA-Z][a-zA-Z0-9]*)\\( ?\\)",
        (matcher, runtime) -> {
            interpret(runtime.getFunction(matcher.group(1)));
        }
    );

    return result;
}

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

    public boolean debugg(){
        debugger = !debugger;
        return debugger;
    }

    public void input(String input){
        if (holdLine == -1) return;
        userInput = input;
        try {
            interpret(code);
        } catch (RuntimeException e) {
            // int line = Integer.parseInt(e.getMessage());
            // throwException(0, "Unknown Statement", code);
            print(e.getMessage() );
        }
    }

    private String extractCode(String code) {
        emptyLineOffset = new int[code.trim().split("\n").length];
        StringBuilder result = new StringBuilder();
        int emptyLines = 0, i = 0;
        for (String line : code.split("\n")) {
            if (!line.isBlank() && !line.startsWith("//")) {
                result.append(line.trim()).append("\n");
                emptyLineOffset[i] = emptyLines;
                i++;
            }else{
                emptyLines++;}
        }
        if (result.isEmpty()) throw new RuntimeException("Empty Programm");
        return result.toString().trim();
    }

    private void interpret(String code) throws RuntimeException {
        // int stackTrace = Thread.currentThread().getStackTrace().length;
        String[] lines = code.split("\n");
        // String[] lines = this.code.split("\n");

        
        Pattern oBracket = Pattern.compile(".*\\{.*", Pattern.MULTILINE);
        for (int i = 0; i < lines.length; i++) {
            boolean matched = false;
            if (i < holdLine || i < holdDebugLine) continue;
            
            String[] promptVar = checkPrompt(lines[i]);
            if( promptVar != null) {
                if (holdLine == -1){
                    print(promptVar[1]);
                    holdLine = i;
                    break;
                } else{
                    holdLine = -1;
                    if(promptVar[2].equals("num")) runtime.set(promptVar[0], Double.parseDouble(userInput));
                    else if(promptVar[2].equals("string")) runtime.set(promptVar[0], (userInput));
                    continue;
                }
            }
            // if (debugger){
            //     if (holdDebugLine == -1) holdDebugLine = 0;
            //     holdLine++;
            //     break;
            // }
            // Wenn irgendwas mit { Klammer kommt (if, functions, while)
            Matcher oBracketM = oBracket.matcher(lines[i]);
            if (oBracketM.matches()) {
                int end = findClosebracket(lines, i);
                String area = linesToCode(lines, i, end);

                // Schauen was mit Klammer gemacht werden soll
                if (checkIfCondition(lines[i])) interpret(area);
                else{
                    String fakeIf = lines[i].replaceFirst("while", "if");
                    while (checkIfCondition(fakeIf)) interpret(area);
                }
                i = end;
                continue;
            }

            // Einfache oneliner Statements
            for (SimpleStatementType type : simpleStatementTypes) {
                Matcher matcher = type.getPattern().matcher(lines[i]);
                if (matcher.matches()) {
                    try {
                        type.executor.execute(matcher, runtime);
                    } catch (Exception e) {
                        throwException(i, "Unknown Statement "+e.getMessage()+" in function", lines[i]);
                    }
                    matched = true;
                    break;
                }
            }

            if (!matched) {
                throw new RuntimeException(lines[i] +" (line "+i+")");
                // throw new RuntimeException("Unknown statement: " + lines[i]);
                // if (stackTrace > 32) runtime.get
                // throwException(i, "Unknown Statement", lines[i]);
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
     * @return line != prompt Befehl -> null; line == prompt -> [varName, PrintBefehl, datatype]
     */
    private String[] checkPrompt(String line){
        Pattern promptPattern = Pattern.compile("(string|num) ([a-zA-Z]+) ?= ?prompt ?\\( ?'([^']+)' ?\\)");
        Matcher pm = promptPattern.matcher(line);
        if (pm.matches()) return new String[]{pm.group(2), pm.group(3), pm.group(1)};
        else return null;
    }

    private boolean checkIfCondition(String ifStatement) {
        Pattern boolIf = Pattern.compile("if ?\\( ?(!?)("+varRegex+" ?)\\) ?\\{");
        Matcher moolif = boolIf.matcher(ifStatement);
        if (moolif.matches()){
            Value value = runtime.get(moolif.group(2));
            if (isBoolean(runtime, moolif.group(2))){
                if (moolif.group(1).equals("!")) return !value.getBoolean();
                else return value.getBoolean();
            }
        }

        Pattern ifPattern = Pattern.compile("if ?\\( ?("+varRegex+") ?(==|!=|>=|<=|>|<) ?("+numReg+"|"+stringReg+"|"+varRegex+") ?\\) ?\\{");
        Matcher mif = ifPattern.matcher(ifStatement);
        if (!mif.matches()) {
            return false;
        }
        // throw new RuntimeException("Wrong if statement:");

        String varName = mif.group(1);
        String operator = mif.group(2);
        String compareV = mif.group(3);

        if (Pattern.matches(varRegex, compareV)) compareV = runtime.get(compareV).toString();

        Value value = runtime.get(varName);
        if (isString(runtime, varName) && operator.equals("==")) return value.getString().equals(compareV);

        double varValue = value.getNumber();
        double compareValue = Double.parseDouble(compareV);
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
        lineNumber = emptyLineOffset[lineNumber] + lineNumber + 1;
        editor.setErrorLine(lineNumber);
        // lineNumber++;
        throw new RuntimeException("Line " + lineNumber + ": "+error+": "+ line);
    }

    // Bin mir bewusst, dass es sich doppelt, aber halte das hier für angemessener und vor allem angenehmer
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
