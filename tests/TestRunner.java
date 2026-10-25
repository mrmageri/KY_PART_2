import config.Config;
import config.ConfigException;
import config.ConfigParser;
import config.RepoMode;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Lightweight test runner (no external dependencies).
 */
public final class TestRunner {

    private static int passed;
    private static int failed;

    private TestRunner() {
    }

    /**
     * Runs all tests and exits non-zero if any fail.
     *
     * @param args ignored
     */
    public static void main(String[] args) {
        runAll();
        System.out.println("Passed: " + passed + ", Failed: " + failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void runAll() {
        run("valid full config",       TestRunner::testValidFullConfig);
        run("valid minimal config",    TestRunner::testValidMinimalConfig);
        run("defaults applied",        TestRunner::testDefaults);
        run("missing --package",       TestRunner::testMissingPackage);
        run("missing --repo",          TestRunner::testMissingRepo);
        run("empty --package",         TestRunner::testEmptyPackage);
        run("bad --package format",    TestRunner::testBadPackageFormat);
        run("nonexistent local repo",  TestRunner::testNonexistentLocalRepo);
        run("invalid remote URL",      TestRunner::testInvalidRemoteUrl);
        run("invalid --mode",          TestRunner::testInvalidMode);
        run("invalid --version",       TestRunner::testInvalidVersion);
        run("invalid --output ext",    TestRunner::testInvalidOutput);
        run("invalid --max-depth",     TestRunner::testInvalidDepth);
        run("depth out of range",      TestRunner::testDepthOutOfRange);
        run("invalid --ascii-tree",    TestRunner::testInvalidAscii);
        run("empty --filter",          TestRunner::testEmptyFilter);
        run("unknown option",          TestRunner::testUnknownOption);
        run("missing value",           TestRunner::testMissingValue);
        run("duplicate option",        TestRunner::testDuplicateOption);
    }

    private static void testValidFullConfig() {
        Path tmp = createTempFile();
        Config c = mustParse(
                "--package", "com.example",
                "--repo", tmp.toString(),
                "--mode", "local",
                "--version", "1.0.0",
                "--output", "graph.png",
                "--ascii-tree", "true",
                "--max-depth", "3",
                "--filter", "core");
        assertEquals("com.example", c.getPackageName());
        assertEquals(RepoMode.LOCAL, c.getRepoMode());
        assertEquals("1.0.0", c.getVersion());
        assertEquals("graph.png", c.getOutputFile());
        assertEquals(true, c.isAsciiTree());
        assertEquals(3, c.getMaxDepth());
        assertEquals("core", c.getFilter());
    }

    private static void testValidMinimalConfig() {
        Path tmp = createTempFile();
        Config c = mustParse("--package", "foo", "--repo", tmp.toString());
        assertEquals("foo", c.getPackageName());
        assertEquals(Config.DEFAULT_MAX_DEPTH, c.getMaxDepth());
        assertEquals(Config.DEFAULT_OUTPUT_FILE, c.getOutputFile());
        assertEquals(Config.DEFAULT_VERSION, c.getVersion());
        assertEquals(false, c.isAsciiTree());
        assertEquals("", c.getFilter());
    }

    private static void testDefaults() {
        Path tmp = createTempFile();
        Config c = mustParse("--package=bar", "--repo=" + tmp);
        assertEquals("bar", c.getPackageName());
        assertEquals(RepoMode.LOCAL, c.getRepoMode());
    }

    private static void testMissingPackage() {
        Path tmp = createTempFile();
        expectConfigException("--repo", tmp.toString());
    }

    private static void testMissingRepo() {
        expectConfigException("--package", "foo");
    }

    private static void testEmptyPackage() {
        Path tmp = createTempFile();
        expectConfigException("--package", "", "--repo", tmp.toString());
    }

    private static void testBadPackageFormat() {
        Path tmp = createTempFile();
        expectConfigException("--package", "1bad", "--repo", tmp.toString());
    }

    private static void testNonexistentLocalRepo() {
        expectConfigException("--package", "foo",
                "--repo", "/nonexistent/file-" + System.nanoTime());
    }

    private static void testInvalidRemoteUrl() {
        expectConfigException("--package", "foo",
                "--repo", "not-a-url", "--mode", "remote");
    }

    private static void testInvalidMode() {
        Path tmp = createTempFile();
        expectConfigException("--package", "foo",
                "--repo", tmp.toString(), "--mode", "cloud");
    }

    private static void testInvalidVersion() {
        Path tmp = createTempFile();
        expectConfigException("--package", "foo",
                "--repo", tmp.toString(), "--version", "not a version!");
    }

    private static void testInvalidOutput() {
        Path tmp = createTempFile();
        expectConfigException("--package", "foo",
                "--repo", tmp.toString(), "--output", "graph.txt");
    }

    private static void testInvalidDepth() {
        Path tmp = createTempFile();
        expectConfigException("--package", "foo",
                "--repo", tmp.toString(), "--max-depth", "abc");
    }

    private static void testDepthOutOfRange() {
        Path tmp = createTempFile();
        expectConfigException("--package", "foo",
                "--repo", tmp.toString(), "--max-depth", "0");
        expectConfigException("--package", "foo",
                "--repo", tmp.toString(),
                "--max-depth", String.valueOf(Config.MAX_ALLOWED_DEPTH + 1));
    }

    private static void testInvalidAscii() {
        Path tmp = createTempFile();
        expectConfigException("--package", "foo",
                "--repo", tmp.toString(), "--ascii-tree", "maybe");
    }

    private static void testEmptyFilter() {
        Path tmp = createTempFile();
        expectConfigException("--package", "foo",
                "--repo", tmp.toString(), "--filter", "");
    }

    private static void testUnknownOption() {
        expectConfigException("--package", "foo", "--what", "ever");
    }

    private static void testMissingValue() {
        expectConfigException("--package");
    }

    private static void testDuplicateOption() {
        Path tmp = createTempFile();
        expectConfigException("--package", "a", "--package", "b",
                "--repo", tmp.toString());
    }

    private static void run(String name, ThrowingRunnable test) {
        try {
            test.run();
            passed += 1;
            System.out.println("[PASS] " + name);
        } catch (AssertionError e) {
            failed += 1;
            System.out.println("[FAIL] " + name + " - " + e.getMessage());
        } catch (Exception e) {
            failed += 1;
            System.out.println("[FAIL] " + name + " - " + e);
        }
    }

    private static Config mustParse(String... args) {
        try {
            return ConfigParser.parse(args);
        } catch (ConfigException e) {
            throw new AssertionError("Unexpected ConfigException: "
                    + e.getMessage(), e);
        }
    }

    private static void expectConfigException(String... args) {
        try {
            ConfigParser.parse(args);
        } catch (ConfigException e) {
            return;
        }
        throw new AssertionError(
                "Expected ConfigException, but none was thrown");
    }

    private static void assertEquals(Object expected, Object actual) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError("Expected <" + expected
                    + ">, got <" + actual + ">");
        }
    }

    private static Path createTempFile() {
        try {
            Path p = Files.createTempFile("deptool-test-", ".json");
            p.toFile().deleteOnExit();
            return p;
        } catch (Exception e) {
            throw new AssertionError("Cannot create temp file", e);
        }
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        /**
         * Runs the test body.
         *
         * @throws Exception on unexpected failure
         */
        void run() throws Exception;
    }
}