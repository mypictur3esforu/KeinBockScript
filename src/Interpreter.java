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
                        // System.out.println("Set " + varName + " = " + value);
                    }),
            new SimpleStatementType(
                    "add",
                    "([a-zA-Z]+) \\+= ([0-9]+)",
                    (matcher, runtime) -> {
                        String varName = matcher.group(1);
                        double add = Double.parseDouble(matcher.group(2));
                        double varValue = runtime.get(varName);

                        runtime.set(varName, varValue + add);
                    }),
            new SimpleStatementType(
                    "sub",
                    "([a-zA-Z]+) -= ([0-9]+)",
                    (matcher, runtime) -> {
                        String varName = matcher.group(1);
                        double sub = Double.parseDouble(matcher.group(2));
                        double varValue = runtime.get(varName);

                        runtime.set(varName, varValue - sub);
                    }),
            new SimpleStatementType(
                    "print",
                    "print\\((\'([^,+]*)'(,|\\+)) ([a-zA-Z]*)\\)",
                    (matcher, runtime) -> {
                        String varName = matcher.group(4);
                        double varValue = runtime.get(varName);
                        print(matcher.group(2) + " " + varValue);

                    }),
    };

    public static void execute(Editor editor, String code) {
        Interpreter i = new Interpreter(editor, code);
        System.out.println(i.getRuntime().get("x"));
    }

    public void print(String output) {
        editor.print(output);
    }

    public Interpreter(Editor editor, String code) {
        this.runtime = new Runtime();
        this.editor = editor;
        this.code = extractCode(code);
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
        System.out.println(lines.length);

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            boolean matched = false;

            Pattern oBracket = Pattern.compile(".*\\{.*", Pattern.MULTILINE);
            Matcher oBracketM = oBracket.matcher(line);
            if (oBracketM.matches()) {
                int end = findClosebracket(lines, i);
                if (end == -1)
                    throw new RuntimeException("Unclosed bracket " + line);
                String area = "";
                for (int z = i + 1; z < end; z++) {
                    area += lines[z] + "\n";
                }
                if (checkIfCondition(lines[i]))
                    interpret(area);
                i = end;
                continue;
            }

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
        return -1;
    }

    private boolean checkIfCondition(String ifStatement) {
        if (ifStatement.equals("{"))
            return true;
        Pattern ifPattern = Pattern.compile("if ([a-zA-Z]+) (==|!=|>=|<=|>|<) ([0-9]+) \\{");
        Matcher mif = ifPattern.matcher(ifStatement);
        if (!mif.matches())
            throw new RuntimeException("Wrong if statement:");

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
}
