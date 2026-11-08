import config.*;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A simple test runner for the application without external dependencies.
 */
public final class TestRunner {

    private static int passed;
    private static int failed;

    private static final int EXIT_FAILURE = 1;
    private static final int INCREMENT = 1;
    private static final int ZERO = 0;
    private static final int FIRST_INDEX = 0;
    private static final int SECOND_INDEX = 1;

    private static final int EXPECTED_GROUPS_SIZE = 2;
    private static final int EXPECTED_ALTS_SIZE = 2;
    private static final int EXPECTED_DEPTH = 3;

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
        if (failed > ZERO) {
            System.exit(EXIT_FAILURE);
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
        run("dependency parsing",      TestRunner::testDependencyParsing);
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
        assertEquals(EXPECTED_DEPTH, c.getMaxDepth());
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
                "--max-depth", String.valueOf(Config.MAX_ALLOWED_DEPTH + INCREMENT));
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

    private static void testDependencyParsing() {
        var groups = DependencyParser.parse("libc6 (>= 2.31), libssl1.1 | libssl3");
        assertEquals(EXPECTED_GROUPS_SIZE, groups.size());

        assertEquals("libc6", groups.get(FIRST_INDEX).get(FIRST_INDEX).packageName());
        assertEquals(">=", groups.get(FIRST_INDEX).get(FIRST_INDEX).operator());
        assertEquals("2.31", groups.get(FIRST_INDEX).get(FIRST_INDEX).version());

        assertEquals(EXPECTED_ALTS_SIZE, groups.get(SECOND_INDEX).size());
        assertEquals("libssl1.1", groups.get(SECOND_INDEX).get(FIRST_INDEX).packageName());
        assertEquals("libssl3", groups.get(SECOND_INDEX).get(SECOND_INDEX).packageName());
    }

    private static void run(String name, ThrowingRunnable test) {
        try {
            test.run();
            passed += INCREMENT;
            System.out.println("[PASS] " + name);
        } catch (AssertionError e) {
            failed += INCREMENT;
            System.out.println("[FAIL] " + name + " - " + e.getMessage());
        } catch (Exception e) {
            failed += INCREMENT;
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
        void run() throws Exception;
    }
}