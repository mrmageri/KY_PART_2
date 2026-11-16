package config;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class DependencyLoadOrder {

    private static final int START_INDEX = 0;
    private static final int INDEX_INCREMENT = 1;

    private DependencyLoadOrder() {
    }

    public static List<String> compute(Map<String, List<String>> graph, String root) {
        List<String> order = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Set<String> active = new HashSet<>();
        Deque<Frame> stack = new ArrayDeque<>();

        stack.push(new Frame(root, START_INDEX));
        active.add(root);

        while (!stack.isEmpty()) {
            Frame frame = stack.peek();
            List<String> deps = graph.getOrDefault(frame.node(), List.of());

            if (frame.index() < deps.size()) {
                String dep = deps.get(frame.index());
                stack.pop();
                stack.push(new Frame(frame.node(), frame.index() + INDEX_INCREMENT));

                if (visited.contains(dep)) {
                    continue;
                }
                if (active.contains(dep)) {
                    continue;
                }

                stack.push(new Frame(dep, START_INDEX));
                active.add(dep);
            } else {
                stack.pop();
                active.remove(frame.node());
                if (visited.add(frame.node())) {
                    order.add(frame.node());
                }
            }
        }

        return order;
    }

    public static void print(List<String> order) {
        for (String name : order) {
            System.out.println(name);
        }
    }

    private record Frame(String node, int index) {
    }
}