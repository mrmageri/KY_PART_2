package config;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Parses command-line arguments into a validated {@link Config}.
 *
 * <p>Each option accepts its value either as {@code --name value} or as
 * {@code --name=value}. Unknown options, duplicated options, missing values
 * and invalid value formats are reported as {@link ConfigException}.</p>
 */
public final class ConfigParser {

    private static final String OPT_PACKAGE = "--package";
    private static final String OPT_REPO    = "--repo";
    private static final String OPT_MODE    = "--mode";
    private static final String OPT_VERSION = "--version";
    private static final String OPT_OUTPUT  = "--output";
    private static final String OPT_ASCII   = "--ascii-tree";
    private static final String OPT_DEPTH   = "--max-depth";
    private static final String OPT_FILTER  = "--filter";

    private static final Set<String> KNOWN_OPTIONS = Set.of(
            OPT_PACKAGE, OPT_REPO, OPT_MODE, OPT_VERSION,
            OPT_OUTPUT, OPT_ASCII, OPT_DEPTH, OPT_FILTER);

    private ConfigParser() {
    }

    /**
     * Parses the given command-line arguments.
     *
     * @param args raw command-line arguments
     * @return validated configuration
     * @throws ConfigException if any argument is invalid
     */
    public static Config parse(String[] args) throws ConfigException {
        Map<String, String> opts = collectOptions(args);
        Config.Builder builder = new Config.Builder();

        applyIfPresent(opts, OPT_PACKAGE, builder, Config.Builder::packageName);
        applyIfPresent(opts, OPT_REPO,    builder, Config.Builder::repository);
        applyIfPresent(opts, OPT_MODE,    builder,
                (b, v) -> b.repoMode(RepoMode.fromString(v)));
        applyIfPresent(opts, OPT_VERSION, builder, Config.Builder::version);
        applyIfPresent(opts, OPT_OUTPUT,  builder, Config.Builder::outputFile);
        applyIfPresent(opts, OPT_ASCII,   builder,
                (b, v) -> b.asciiTree(parseBoolean(v, OPT_ASCII)));
        applyIfPresent(opts, OPT_DEPTH,   builder,
                (b, v) -> b.maxDepth(parseDepth(v)));
        applyIfPresent(opts, OPT_FILTER,  builder, Config.Builder::filter);

        return builder.build();
    }

    private static void applyIfPresent(Map<String, String> opts, String key,
                                       Config.Builder builder, OptionApplier applier)
            throws ConfigException {
        if (opts.containsKey(key)) {
            applier.apply(builder, opts.get(key));
        }
    }

    private static Map<String, String> collectOptions(String[] args)
            throws ConfigException {
        Map<String, String> result = new HashMap<>();
        int i = 0;
        while (i < args.length) {
            String arg = args[i];
            if (!arg.startsWith("--")) {
                throw new ConfigException("Unexpected argument '" + arg
                        + "': options must start with '--'");
            }
            int eq = arg.indexOf('=');
            String key;
            String value;
            if (eq >= 0) {
                key = arg.substring(0, eq);
                value = arg.substring(eq + 1);
                i += 1;
            } else {
                key = arg;
                if (i + 1 >= args.length) {
                    throw new ConfigException(
                            "Missing value for option '" + key + "'");
                }
                value = args[i + 1];
                i += 2;
            }
            if (!KNOWN_OPTIONS.contains(key)) {
                throw new ConfigException("Unknown option '" + key + "'");
            }
            if (result.containsKey(key)) {
                throw new ConfigException(
                        "Option '" + key + "' specified more than once");
            }
            result.put(key, value);
        }
        return result;
    }

    private static boolean parseBoolean(String value, String option)
            throws ConfigException {
        if ("true".equalsIgnoreCase(value)) {
            return true;
        }
        if ("false".equalsIgnoreCase(value)) {
            return false;
        }
        throw new ConfigException("Invalid boolean for '" + option + "': '"
                + value + "' (expected 'true' or 'false')");
    }

    private static int parseDepth(String value) throws ConfigException {
        int depth;
        try {
            depth = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new ConfigException("Invalid max-depth '" + value
                    + "': must be an integer");
        }
        return depth;
    }

    /**
     * Functional interface describing an option-applying callback.
     */
    @FunctionalInterface
    private interface OptionApplier {
        /**
         * Applies the option value to the builder.
         *
         * @param builder config builder
         * @param value   raw option value
         * @throws ConfigException if the value is invalid
         */
        void apply(Config.Builder builder, String value)
                throws ConfigException;
    }
}