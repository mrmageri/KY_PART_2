package config;

public enum RepoMode {
    LOCAL,
    REMOTE;
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