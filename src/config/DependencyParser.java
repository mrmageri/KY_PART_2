package config;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses Debian dependency strings into structured objects.
 */
public final class DependencyParser {
    private static final int GROUP_NAME_INDEX = 1;
    private static final int GROUP_OPERATOR_INDEX = 2;
    private static final int GROUP_VERSION_INDEX = 3;
    private static final int FALLBACK_SPLIT_LIMIT = 2;
    private static final int FIRST_ELEMENT_INDEX = 0;
    private static final int DEPTH_OUTSIDE_PARENS = 0;
    private static final char COMMA_DELIMITER = ',';
    private static final char PIPE_DELIMITER = '|';
    private static final char OPEN_PAREN = '(';
    private static final char CLOSE_PAREN = ')';

    public record Dependency(
            String packageName,
            String operator,
            String version,
            List<List<Dependency>> alternatives
    ) {
        public String display() {
            StringBuilder sb = new StringBuilder(packageName);
            if (operator != null && version != null) {
                sb.append(" (").append(operator).append(" ").append(version).append(")");
            }
            return sb.toString();
        }
    }

    private static final Pattern DEP_RE = Pattern.compile(
            "^([a-zA-Z0-9][a-zA-Z0-9+.-]*)(?:\\s*\\(([<>=]+)\\s*([^)]+)\\))?.*$"
    );

    private DependencyParser() {
    }

    public static List<List<Dependency>> parse(String dependsField) {
        if (dependsField == null || dependsField.isBlank()) {
            return List.of();
        }

        List<List<Dependency>> groups = new ArrayList<>();

        for (String group : splitOutsideParens(dependsField, COMMA_DELIMITER)) {
            List<Dependency> alts = new ArrayList<>();
            for (String alt : splitOutsideParens(group, PIPE_DELIMITER)) {
                String cleaned = alt.replaceAll("\\[[^\\]]*\\]", "").trim();
                Matcher m = DEP_RE.matcher(cleaned);

                if (m.matches()) {
                    String name = m.group(GROUP_NAME_INDEX);
                    String op = m.group(GROUP_OPERATOR_INDEX);
                    String ver = m.group(GROUP_VERSION_INDEX);
                    alts.add(new Dependency(name, op, ver, null));
                } else if (!cleaned.isBlank()) {
                    String[] parts = cleaned.split("[\\s(]", FALLBACK_SPLIT_LIMIT);
                    String name = parts[FIRST_ELEMENT_INDEX];
                    alts.add(new Dependency(name, null, null, null));
                }
            }
            if (!alts.isEmpty()) {
                groups.add(alts);
            }
        }
        return groups;
    }

    private static List<String> splitOutsideParens(String s, char sep) {
        List<String> parts = new ArrayList<>();
        int depth = DEPTH_OUTSIDE_PARENS;
        StringBuilder cur = new StringBuilder();

        for (char c : s.toCharArray()) {
            if (c == OPEN_PAREN) {
                depth++;
            }
            if (c == CLOSE_PAREN) {
                depth--;
            }
            if (c == sep && depth == DEPTH_OUTSIDE_PARENS) {
                parts.add(cur.toString().trim());
                cur.setLength(DEPTH_OUTSIDE_PARENS);
            } else {
                cur.append(c);
            }
        }
        if (!cur.isEmpty()) {
            parts.add(cur.toString().trim());
        }
        return parts;
    }
}