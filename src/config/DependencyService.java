package config;

import java.util.List;
import java.util.Map;

/**
 * Service class to fetch dependencies and build graphs.
 */
public final class DependencyService {

    /**
     * Fetches direct dependencies from a repository.
     *
     * @param repoLocation repository path or URL
     * @param packageName  target package
     * @param version      target version
     * @return list of direct dependencies
     * @throws ConfigException if fetching or parsing fails
     */
    public static List<List<DependencyParser.Dependency>> fetchDirectDependencies(
            String repoLocation, String packageName, String version) throws ConfigException {

        String content = PackageFetcher.fetch(repoLocation);
        PackageIndex index = PackageIndex.parse(content);

        PackageIndex.PackageEntry pkg = index.find(packageName, version);
        if (pkg == null) {
            throw new ConfigException("Package '" + packageName + "' version '" + version + "' not found in index");
        }

        List<List<DependencyParser.Dependency>> deps = new java.util.ArrayList<>();
        String depends = pkg.get("Depends");
        String preDepends = pkg.get("Pre-Depends");

        if (!depends.isBlank()) {
            deps.addAll(DependencyParser.parse(depends));
        }
        if (!preDepends.isBlank()) {
            deps.addAll(DependencyParser.parse(preDepends));
        }
        return deps;
    }

    /**
     * Builds a transitive dependency graph.
     *
     * @param config validated configuration
     * @return adjacency list representation of the graph
     * @throws ConfigException if fetching or parsing fails
     */
    public static Map<String, List<String>> buildGraph(Config config) throws ConfigException {
        String content = PackageFetcher.fetch(config.getRepository());
        PackageIndex index = PackageIndex.parse(content);
        return GraphBuilder.build(index, config.getPackageName(), config.getMaxDepth(), config.getFilter());
    }
}