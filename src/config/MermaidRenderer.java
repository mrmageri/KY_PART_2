package config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public final class MermaidRenderer {

    private static final String HEADER = "graph TD\n";
    private static final String ARROW = " --> ";
    private static final String NODE_OPEN = "[";
    private static final String NODE_CLOSE = "]";
    private static final String LINE_END = "\n";
    private static final String INDENT = "    ";
    private static final String SAFE_PREFIX = "n_";
    private static final String UNSAFE_PATTERN = "[^A-Za-z0-9_]";
    private static final String SAFE_REPLACEMENT = "_";

    private static final String QUOTE = "\"";
    private static final String PLAIN_PATTERN = "[A-Za-z0-9_]+";

    private static String label(String name) {
        if (name.matches(PLAIN_PATTERN)) {
            return NODE_OPEN + name + NODE_CLOSE;
        }
        return NODE_OPEN + QUOTE + name + QUOTE + NODE_CLOSE;
    }

    private MermaidRenderer() {
    }

    public static String render(Map<String, List<String>> graph) {
        StringBuilder sb = new StringBuilder(HEADER);
        for (Map.Entry<String, List<String>> entry : graph.entrySet()) {
            String from = safeId(entry.getKey());
            sb.append(INDENT).append(from).append(label(entry.getKey()))
                    .append(LINE_END);
            for (String dep : entry.getValue()) {
                sb.append(INDENT).append(from).append(ARROW)
                        .append(safeId(dep)).append(label(dep))
                        .append(LINE_END);
            }
        }
        return sb.toString();
    }

    public static void save(Map<String, List<String>> graph, String outputFile)
            throws ConfigException {
        try {
            Files.writeString(Path.of(outputFile), render(graph));
            System.out.println("\n[Mermaid saved to " + outputFile + "]");
        } catch (IOException e) {
            throw new ConfigException(
                    "Failed to save Mermaid to file: " + outputFile, e);
        }
    }

    private static String safeId(String name) {
        String safe = name.replaceAll(UNSAFE_PATTERN, SAFE_REPLACEMENT);
        return SAFE_PREFIX + safe + "_" + Integer.toHexString(name.hashCode());
    }
}