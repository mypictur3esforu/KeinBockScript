import java.util.regex.Matcher;

@FunctionalInterface
interface StatementExecutor {
    void execute(Matcher matcher, Runtime runtime);
}
