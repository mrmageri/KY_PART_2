import config.*;

import java.util.List;

public final class Main {

    private static final String OPT_HELP_LONG  = "--help";
    private static final String OPT_HELP_SHORT = "-h";
    private static final int EXIT_FAILURE = 1;

    /**
     * Main method.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        if (containsHelp(args)) {
            printHelp();
            return;
        }
        try {
            Config config = ConfigParser.parse(args);
            System.out.println(config.toKeyValueString());
            System.out.println();
            System.out.println("=== Direct dependencies of " + config.getPackageName()
                    + " (version: " + config.getVersion() + ") ===");

            List<List<DependencyParser.Dependency>> deps =
                    DependencyService.fetchDirectDependencies(
                            config.getRepository(),
                            config.getPackageName(),
                            config.getVersion());

            if (deps.isEmpty()) {
                System.out.println("(no direct dependencies)");
            } else {
                for (List<DependencyParser.Dependency> group : deps) {
                    String line = group.stream()
                            .map(DependencyParser.Dependency::display)
                            .collect(java.util.stream.Collectors.joining(" | "));
                    System.out.println("  " + line);
                }
            }
        } catch (ConfigException e) {
            System.err.println("Configuration error: " + e.getMessage());
            System.err.println("Try '--help' for usage information.");
            System.exit(EXIT_FAILURE);
        }
    }

    private static boolean containsHelp(String[] args) {
        for (String arg : args) {
            if (OPT_HELP_LONG.equals(arg) || OPT_HELP_SHORT.equals(arg)) {
                return true;
            }
        }
        return false;
    }

    private static void printHelp() {
        System.out.println("Usage: deptool [OPTIONS]");
        System.out.println();
        System.out.println("Dependency graph visualization tool (Stage 2).");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  --package <name>     Package name (required)");
        System.out.println("  --repo <location>    Repository URL or path (required)");
        System.out.println("  --mode <local|remote> Repo mode (default: local)");
        System.out.println("  --version <value>    Package version (default: latest)");
        System.out.println("  --output <file>      Image file (default: dependency-graph.png)");
        System.out.println("  --ascii-tree <bool>  ASCII tree mode (default: false)");
        System.out.println("  --max-depth <n>      Analysis depth, 1..100 (default: 5)");
        System.out.println("  --filter <substr>    Package name substring filter");
        System.out.println("  -h, --help           Show this help");
    }
}