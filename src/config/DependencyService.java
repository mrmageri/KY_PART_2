package config;

import java.util.List;

/**
 * Service class to fetch direct dependencies from a repository.
 */
public final class DependencyService {

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
}