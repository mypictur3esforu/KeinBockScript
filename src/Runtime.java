import java.util.HashMap;
import java.util.Map;

class Runtime {

    private final Map<String, Double> vars = new HashMap<>();
    private final Map<String, String> functions = new HashMap<>();

    void set(String name, double value) {
        vars.put(name, value);
    }

    double get(String name) {
        if (!vars.containsKey(name))
            throw new RuntimeException("Undefined variable: " + name);
        return vars.get(name);
    }

    void setFunction(String funcName, String code) {
        functions.put(funcName, code);
    }

    String getFunction(String name) {
        if (!functions.containsKey(name))
            throw new RuntimeException("Undefined function: " + name);
        return functions.get(name);
    }
}
