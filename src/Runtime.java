import java.util.HashMap;
import java.util.Map;

class Runtime {

    private final Map<String, Value> vars = new HashMap<>();
    private final Map<String, String> functions = new HashMap<>();

    public void set(String name, Value value) {
        vars.put(name, value);
    }

    /**
     * Setzt var1 = var2
     * @param name Variable 1
     * @param name2 Variable 2
     */
    void equalize(String name, String name2) {
        Value v1 = get(name), v2 = get(name2);
        if (v1.getValueType() == v2.getValueType()) v1 = v2;
        vars.put(name, v1);
    }
    void set(String name, String value) {
        Value v = new Value(value);
        vars.put(name, v);
    }
    void set(String name, boolean value) {
        Value v = new Value(value);
        vars.put(name, v);
    }
    void set(String name, double value) {
        Value v = new Value(value);
        vars.put(name, v);
    }

    Value get(String name) {
        if (!vars.containsKey(name))
            throw new RuntimeException("Undefined variable: " + name);
        return vars.get(name);
    }

    // Was wenn man gleiche Funktion mehrfach defniert?
    void setFunction(String funcName, String code) {
        functions.put(funcName, code);
    }

    String getFunction(String name) {
        if (!functions.containsKey(name))
            throw new RuntimeException("Undefined function: " + name);
        return functions.get(name);
    }

    public String getString(String name) {
        return get(name).getString();
    }

    public double getNumber(String name) {
        return get(name).getNumber();
    }

    public boolean getBoolean(String name) {
        return get(name).getBoolean();
    }
}
