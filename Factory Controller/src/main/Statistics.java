package main;

import java.util.*;

public class Statistics {

    public enum Type {
        BlocksReceived("Blocks received"),
        BlocksSent("Blocks sent"),
        BlocksPushed("Blocks pushed"),
        TransformationsOperated("Transformations operated"),
        TotalTimeInOperation("Total time in operation (ms)");
        String id;

        Type(String id) {
            this.id = id;
        }
    }

    private final Map<String, Map<Type, Map<Object, Integer>>> mapStats = new HashMap<>();
    private final Map<String, Map<Type, Integer>> intStats = new HashMap<>();

    public void inc(String id, Type stat) {
        this.inc(id, stat, 1);
    }

    public void inc(String id, Type stat, Object obj) {
        this.inc(id, stat, obj, 1);
    }

    public void inc(String id, Type stat, int inc) {
        Map<Type, Integer> m = intStats.getOrDefault(id, new HashMap<>());
        m.put(stat, m.getOrDefault(stat, 0) + inc);
        intStats.put(id, m);
    }

    public void inc(String id, Type stat, Object obj, int inc) {
        Map<Type, Map<Object, Integer>> m1 = mapStats.getOrDefault(id, new HashMap<>());
        Map<Object, Integer> m2 = m1.getOrDefault(stat, new HashMap<>());
        m2.put(obj, m2.getOrDefault(obj, 0) + inc);
        m1.put(stat, m2);
        mapStats.put(id, m1);
    }

    public String processCmd(String cmd) {
        Map<Type, Integer> intm = intStats.get(cmd);
        Map<Type, Map<Object, Integer>> mapm = mapStats.get(cmd);

        if (intm != null || mapm != null) {
            StringBuilder sb = new StringBuilder();

            sb.append("Info for item ").append(cmd).append("\n");

            if (intm != null) {
                for (Map.Entry<Type, Integer> entry : intm.entrySet()) {
                    sb.append(" - ").append(entry.getKey().id).append(": ").append(entry.getValue()).append("\n");
                }
            }

            if (mapm != null) {
                for (Map.Entry<Type, Map<Object, Integer>> e1 : mapm.entrySet()) {
                    int total = e1.getValue().entrySet().stream().map((e2) -> e2.getValue()).reduce(0, Integer::sum);

                    sb.append(" - ").append(e1.getKey().id).append(": ").append(total).append("\n");

                    e1.getValue().entrySet().stream().forEach((e2) -> {
                        sb.append("\t").append(e2.getKey().toString()).append(": ").append(e2.getValue()).append("\n");
                    });
                }
            }

            return sb.toString();
        }

        return "No info for item " + cmd;
    }

}
