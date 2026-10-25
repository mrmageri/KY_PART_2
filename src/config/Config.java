package config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

public final class Config {

    public static final int DEFAULT_MAX_DEPTH = 5;

    public static final int MAX_ALLOWED_DEPTH = 100;

    public static final int MIN_ALLOWED_DEPTH = 1;

    public static final String DEFAULT_OUTPUT_FILE = "dependency-graph.png";

    public static final String DEFAULT_VERSION = "latest";

    private static final Pattern PACKAGE_PATTERN =
            Pattern.compile("^[A-Za-z][A-Za-z0-9_.\\-]*$");
    private static final Pattern VERSION_PATTERN =
            Pattern.compile("^(latest|\\d+(\\.\\d+)*(-[A-Za-z0-9.\\-]+)?)$");
    private static final Pattern URL_PATTERN =
            Pattern.compile("^https?://\\S+$");
    private static final Pattern OUTPUT_PATTERN =
            Pattern.compile("^[\\w.\\-]+\\.(png|svg|dot|pdf)$",
                    Pattern.CASE_INSENSITIVE);

    private final String packageName;
    private final String repository;
    private final RepoMode repoMode;
    private final String version;
    private final String outputFile;
    private final boolean asciiTree;
    private final int maxDepth;
    private final String filter;

    private Config(Builder builder) {
        this.packageName = builder.packageName;
        this.repository = builder.repository;
        this.repoMode = builder.repoMode;
        this.version = builder.version;
        this.outputFile = builder.outputFile;
        this.asciiTree = builder.asciiTree;
        this.maxDepth = builder.maxDepth;
        this.filter = builder.filter;
    }

    /**
     * Returns the analyzed package name.
     *
     * @return package name
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * Returns the repository URL or local file path.
     *
     * @return repository location
     */
    public String getRepository() {
        return repository;
    }

    /**
     * Returns the repository access mode.
     *
     * @return repository mode
     */
    public RepoMode getRepoMode() {
        return repoMode;
    }

    /**
     * Returns the target package version.
     *
     * @return version string
     */
    public String getVersion() {
        return version;
    }

    /**
     * Returns the output image file name.
     *
     * @return output file name
     */
    public String getOutputFile() {
        return outputFile;
    }

    /**
     * Tells whether dependencies should be printed as an ASCII tree.
     *
     * @return {@code true} if ASCII tree mode is enabled
     */
    public boolean isAsciiTree() {
        return asciiTree;
    }

    /**
     * Returns the maximum dependency-analysis depth.
     *
     * @return maximum depth
     */
    public int getMaxDepth() {
        return maxDepth;
    }

    /**
     * Returns the substring used to filter packages.
     *
     * @return filter substring, possibly empty
     */
    public String getFilter() {
        return filter;
    }

    /**
     * Formats the configuration as a multi-line {@code key: value} string.
     *
     * @return formatted configuration
     */
    public String toKeyValueString() {
        String nl = System.lineSeparator();
        return "package-name: " + packageName + nl
                + "repository: " + repository + nl
                + "repo-mode: " + repoMode.name().toLowerCase() + nl
                + "version: " + version + nl
                + "output-file: " + outputFile + nl
                + "ascii-tree: " + asciiTree + nl
                + "max-depth: " + maxDepth + nl
                + "filter: " + filter;
    }

    /**
     * Builder for {@link Config}. All validation happens in the setter
     * methods or in {@link #build()} (for cross-field checks).
     */
    public static final class Builder {

        private String packageName;
        private String repository;
        private RepoMode repoMode = RepoMode.LOCAL;
        private String version = DEFAULT_VERSION;
        private String outputFile = DEFAULT_OUTPUT_FILE;
        private boolean asciiTree;
        private int maxDepth = DEFAULT_MAX_DEPTH;
        private String filter = "";

        /**
         * Sets the analyzed package name.
         *
         * @param value package name
         * @return this builder
         * @throws ConfigException if the value is empty or invalid
         */
        public Builder packageName(String value) throws ConfigException {
            if (value == null || value.isEmpty()) {
                throw new ConfigException("Package name must not be empty");
            }
            if (!PACKAGE_PATTERN.matcher(value).matches()) {
                throw new ConfigException("Invalid package name '" + value
                        + "': must start with a letter and contain only "
                        + "letters, digits, '.', '_' or '-'");
            }
            this.packageName = value;
            return this;
        }

        /**
         * Sets the repository URL or local file path.
         *
         * @param value repository location
         * @return this builder
         * @throws ConfigException if the value is empty
         */
        public Builder repository(String value) throws ConfigException {
            if (value == null || value.isEmpty()) {
                throw new ConfigException("Repository must not be empty");
            }
            this.repository = value;
            return this;
        }

        /**
         * Sets the repository access mode.
         *
         * @param value repository mode
         * @return this builder
         * @throws ConfigException if the value is {@code null}
         */
        public Builder repoMode(RepoMode value) throws ConfigException {
            if (value == null) {
                throw new ConfigException("Repository mode must not be null");
            }
            this.repoMode = value;
            return this;
        }

        /**
         * Sets the target package version.
         *
         * @param value version string
         * @return this builder
         * @throws ConfigException if the value has an invalid format
         */
        public Builder version(String value) throws ConfigException {
            if (value == null || !VERSION_PATTERN.matcher(value).matches()) {
                throw new ConfigException("Invalid version '" + value
                        + "': expected 'latest' or a semver-like value");
            }
            this.version = value;
            return this;
        }

        /**
         * Sets the output image file name.
         *
         * @param value output file name
         * @return this builder
         * @throws ConfigException if the value has an invalid extension
         */
        public Builder outputFile(String value) throws ConfigException {
            if (value == null || !OUTPUT_PATTERN.matcher(value).matches()) {
                throw new ConfigException("Invalid output file '" + value
                        + "': expected a name ending with .png, .svg, .dot or .pdf");
            }
            this.outputFile = value;
            return this;
        }

        /**
         * Enables or disables ASCII tree output.
         *
         * @param value {@code true} to enable ASCII tree mode
         * @return this builder
         */
        public Builder asciiTree(boolean value) {
            this.asciiTree = value;
            return this;
        }

        /**
         * Sets the maximum dependency-analysis depth.
         *
         * @param value maximum depth
         * @return this builder
         * @throws ConfigException if the depth is outside 1..100
         */
        public Builder maxDepth(int value) throws ConfigException {
            if (value < MIN_ALLOWED_DEPTH || value > MAX_ALLOWED_DEPTH) {
                throw new ConfigException("Invalid max-depth " + value
                        + ": must be between " + MIN_ALLOWED_DEPTH
                        + " and " + MAX_ALLOWED_DEPTH);
            }
            this.maxDepth = value;
            return this;
        }

        /**
         * Sets the substring used to filter package names.
         *
         * @param value filter substring
         * @return this builder
         * @throws ConfigException if the value is empty
         */
        public Builder filter(String value) throws ConfigException {
            if (value == null || value.isEmpty()) {
                throw new ConfigException("Filter must not be empty; "
                        + "omit the option to disable filtering");
            }
            this.filter = value;
            return this;
        }

        /**
         * Builds the configuration after cross-field validation.
         *
         * @return validated configuration
         * @throws ConfigException if a required field is missing or
         *     a cross-field check fails
         */
        public Config build() throws ConfigException {
            if (packageName == null) {
                throw new ConfigException("Package name is required (--package)");
            }
            if (repository == null) {
                throw new ConfigException("Repository is required (--repo)");
            }
            validateRepository();
            return new Config(this);
        }

        private void validateRepository() throws ConfigException {
            if (repoMode == RepoMode.LOCAL) {
                Path path = Paths.get(repository);
                if (!Files.exists(path)) {
                    throw new ConfigException("Repository file does not exist: "
                            + repository);
                }
                if (!Files.isRegularFile(path)) {
                    throw new ConfigException("Repository path is not a "
                            + "regular file: " + repository);
                }
            } else {
                if (!URL_PATTERN.matcher(repository).matches()) {
                    throw new ConfigException("Invalid repository URL '"
                            + repository + "': expected http:// or https://");
                }
            }
        }
    }
}