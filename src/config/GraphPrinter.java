package config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Utility class for printing and saving dependency graphs.
 */
public final class GraphPrinter {

    private static final String TREE_BRANCH = "├── ";
    private static final String TREE_LAST_BRANCH = "└── ";
    private static final String TREE_PIPE = "│   ";
    private static final String TREE_SPACE = "    ";
    private static final String DOT_HEADER = "digraph G {\n";
    private static final String DOT_FOOTER = "}\n";
    private static final String DOT_ARROW = " -> ";
    private static final String DOT_END = ";\n";
    private static final String DOT_QUOTE = "\"";

    private GraphPrinter() {
    }

    /**
     * Prints the graph as an ASCII tree.
     *
     * @param graph adjacency list
     * @param root  starting node
     */
    public static void printAsciiTree(Map<String, List<String>> graph, String root) {
        System.out.println(root);
        printTreeRecursive(graph, root, "", new HashSet<>());
    }

    private static void printTreeRecursive(Map<String, List<String>> graph, String node,
                                           String prefix, Set<String> path) {
        path.add(node);
        List<String> children = graph.getOrDefault(node, List.of());

        for (int i = 0; i < children.size(); i++) {
            String child = children.get(i);
            boolean isLast = (i == children.size() - 1);
            System.out.print(prefix + (isLast ? TREE_LAST_BRANCH : TREE_BRANCH));

            if (path.contains(child)) {
                System.out.println(child + " (cycle detected)");
            } else {
                System.out.println(child);
                printTreeRecursive(graph, child, prefix + (isLast ? TREE_SPACE : TREE_PIPE), path);
            }
        }
        path.remove(node);
    }

    /**
     * Saves the graph in DOT format.
     *
     * @param graph      adjacency list
     * @param outputFile output file path
     * @throws ConfigException if writing fails
     */
    public static void saveDot(Map<String, List<String>> graph, String outputFile) throws ConfigException {
        StringBuilder sb = new StringBuilder();
        sb.append(DOT_HEADER);

        for (Map.Entry<String, List<String>> entry : graph.entrySet()) {
            String node = entry.getKey();
            List<String> deps = entry.getValue();

            if (deps.isEmpty()) {
                sb.append("  ").append(DOT_QUOTE).append(node).append(DOT_QUOTE).append(DOT_END);
            } else {
                for (String dep : deps) {
                    sb.append("  ").append(DOT_QUOTE).append(node).append(DOT_QUOTE)
                            .append(DOT_ARROW)
                            .append(DOT_QUOTE).append(dep).append(DOT_QUOTE).append(DOT_END);
                }
            }
        }
        sb.append(DOT_FOOTER);

        try {
            Files.writeString(Path.of(outputFile), sb.toString());
            System.out.println("\n[Graph saved to " + outputFile + "]");
        } catch (IOException e) {
            throw new ConfigException("Failed to save graph to file: " + outputFile, e);
        }
    }
}