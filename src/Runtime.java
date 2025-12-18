import java.util.HashMap;

class Runtime {
    private final HashMap<String, Double> vars = new HashMap<>();

    void set(String name, double value) {
        vars.put(name, value);
    }

    double get(String name) {
        if (!vars.containsKey(name))
            throw new RuntimeException("Undefined variable: " + name);
        return vars.get(name);
    }
}
