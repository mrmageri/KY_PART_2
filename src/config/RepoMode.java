package config;

/**
 * Mode of working with a test repository.
 */
public enum RepoMode {

    /** Read dependencies from a local file. */
    LOCAL,

    /** Fetch dependencies from a remote URL. */
    REMOTE;

    /**
     * Parses a repository mode from the given string (case-insensitive).
     *
     * @param value raw value from the command line
     * @return parsed mode
     * @throws ConfigException if the value does not match any mode
     */
    public static RepoMode fromString(String value) throws ConfigException {
        if (value == null) {
            throw new ConfigException("Repository mode must not be null");
        }
        switch (value.toLowerCase(java.util.Locale.ROOT)) {
            case "local":
                return LOCAL;
            case "remote":
                return REMOTE;
            default:
                throw new ConfigException(
                        "Unknown repository mode: '" + value
                                + "'. Expected 'local' or 'remote'");
        }
    }
}