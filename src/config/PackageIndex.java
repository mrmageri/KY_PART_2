package config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a parsed Ubuntu Packages index.
 */
public final class PackageIndex {

    private static final int MIN_VALID_COLON_INDEX = 1;
    private static final int VALUE_OFFSET = 1;
    private static final int KEY_START_INDEX = 0;

    public record PackageEntry(Map<String, String> fields) {
        public String get(String key) {
            return fields.getOrDefault(key, "");
        }
    }

    private final List<PackageEntry> entries;

    public PackageIndex(List<PackageEntry> entries) {
        this.entries = entries;
    }

    public List<PackageEntry> getEntries() {
        return entries;
    }

    public static PackageIndex parse(String content) {
        List<PackageEntry> result = new ArrayList<>();
        Map<String, String> fields = new HashMap<>();

        for (String line : content.split("\\R")) {
            if (line.isBlank()) {
                if (fields.containsKey("Package")) {
                    result.add(new PackageEntry(fields));
                    fields = new HashMap<>();
                }
                continue;
            }

            int colon = line.indexOf(':');
            if (colon >= MIN_VALID_COLON_INDEX) {
                String key = line.substring(KEY_START_INDEX, colon).trim();
                String value = line.substring(colon + VALUE_OFFSET).trim();

                if ("Package".equals(key) && fields.containsKey("Package")) {
                    result.add(new PackageEntry(fields));
                    fields = new HashMap<>();
                }
                fields.put(key, value);
            }
        }
        if (fields.containsKey("Package")) {
            result.add(new PackageEntry(fields));
        }
        return new PackageIndex(result);
    }

    public PackageEntry find(String packageName, String version) {
        for (PackageEntry e : entries) {
            if (e.get("Package").equals(packageName)) {
                if (version == null
                        || Config.DEFAULT_VERSION.equals(version)
                        || e.get("Version").equals(version)) {
                    return e;
                }
            }
        }
        return null;
    }
}