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
}
