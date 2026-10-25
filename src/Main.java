import config.Config;
import config.ConfigException;
import config.ConfigParser;


public final class Main {

    private static final String OPT_HELP_LONG  = "--help";
    private static final String OPT_HELP_SHORT = "-h";

    private Main() {
    }

    /**
     * Runs the tool.
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
        } catch (ConfigException e) {
            System.err.println("Configuration error: " + e.getMessage());
            System.err.println("Try '--help' for usage information.");
            System.exit(1);
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
        System.out.println("Dependency graph visualization tool (Stage 1).");
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