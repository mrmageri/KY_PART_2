package config;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Builds a dependency graph using iterative DFS.
 */
public final class GraphBuilder {

    private static final int ROOT_DEPTH = 0;
    private static final int DEPTH_INCREMENT = 1;

    private GraphBuilder() {
    }

    /**
     * Builds a graph of transitive dependencies using iterative DFS.
     *
     * @param index   parsed package index
     * @param root    root package name
     * @param maxDepth maximum analysis depth
     * @param filter  substring to exclude packages
     * @return adjacency list representation of the graph
     */
    public static Map<String, List<String>> build(
            PackageIndex index, String root, int maxDepth, String filter) {

        Map<String, List<String>> graph = new HashMap<>();
        Deque<State> stack = new ArrayDeque<>();
        Set<String> visited = new HashSet<>();

        stack.push(new State(root, ROOT_DEPTH));

        while (!stack.isEmpty()) {
            State current = stack.pop();

            if (shouldSkip(current, maxDepth, filter, visited)) {
                continue;
            }
            visited.add(current.name);

            PackageIndex.PackageEntry entry = index.find(current.name, Config.DEFAULT_VERSION);
            if (entry == null) {
                graph.put(current.name, new ArrayList<>());
                continue;
            }

            List<String> directDeps = extractDependencies(entry, filter);
            graph.put(current.name, directDeps);

            for (String depName : directDeps) {
                if (!visited.contains(depName)) {
                    stack.push(new State(depName, current.depth + DEPTH_INCREMENT));
                }
            }
        }
        return graph;
    }

    private static boolean shouldSkip(State current, int maxDepth, String filter, Set<String> visited) {
        if (current.depth > maxDepth) {
            return true;
        }
        if (filter != null && !filter.isEmpty() && current.name.contains(filter)) {
            return true;
        }
        return visited.contains(current.name);
    }

    private static List<String> extractDependencies(PackageIndex.PackageEntry entry, String filter) {
        List<String> directDeps = new ArrayList<>();
        List<List<DependencyParser.Dependency>> parsedDeps =
                new ArrayList<>(DependencyParser.parse(entry.get("Depends")));
        parsedDeps.addAll(DependencyParser.parse(entry.get("Pre-Depends")));

        for (List<DependencyParser.Dependency> group : parsedDeps) {
            for (DependencyParser.Dependency dep : group) {
                if (filter != null && !filter.isEmpty() && dep.packageName().contains(filter)) {
                    continue;
                }
                directDeps.add(dep.packageName());
            }
        }
        return directDeps;
    }

    private record State(String name, int depth) {
    }
}