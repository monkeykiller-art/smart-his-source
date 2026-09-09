package com.smarthis.common;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class SqlPortabilityLintTest {

    private static final Pattern VERSIONED_MIGRATION = Pattern.compile("^V([^_]+)__.+\\.sql$");

    private static final List<Pattern> FORBIDDEN = List.of(
            Pattern.compile("(?i)\\bSERIAL\\b"),
            Pattern.compile("(?i)\\bBIGSERIAL\\b"),
            Pattern.compile("(?i)\\bIDENTITY\\b"),
            Pattern.compile("(?i)\\bJSONB\\b"),
            Pattern.compile("(?i)\\bTIMESTAMPTZ\\b"),
            Pattern.compile("(?i)\\bBOOLEAN\\b"),
            Pattern.compile("(?i)\\bILIKE\\b"),
            Pattern.compile("(?i)\\bRETURNING\\b"),
            Pattern.compile("(?i):::"),
            Pattern.compile("(?i)\\bON\\s+CONFLICT\\b"),
            Pattern.compile("(?i)\\bARRAY\\s*\\["),
            Pattern.compile("(?i)\\bGENERATED\\b")
    );

    private static final List<String> FORBIDDEN_NAMES = List.of(
            "user", "order", "level", "type", "value", "comment",
            "size", "number", "desc", "resource", "session", "group",
            "key", "check"
    );

    @Test
    void allMigrationSqlMustBePortable() throws IOException {
        Path root = findProjectRoot();
        List<String> violations = new ArrayList<>();

        Files.walkFileTree(root, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (isMigration(file)) {
                    String content = Files.readString(file);
                    String relativePath = root.relativize(file).toString();
                    checkContent(content, relativePath, violations);
                }
                return FileVisitResult.CONTINUE;
            }
        });

        assertTrue(violations.isEmpty(),
                "SQL portability violations found:\n" + String.join("\n", violations));
    }

    @Test
    void migrationVersionsMustBeUniqueWithinEachModule() throws IOException {
        Path root = findProjectRoot();
        Map<String, Path> migrationsByModuleAndVersion = new HashMap<>();
        List<String> duplicates = new ArrayList<>();
        int[] migrationCount = {0};

        Files.walkFileTree(root, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (!isMigration(file)) {
                    return FileVisitResult.CONTINUE;
                }

                migrationCount[0]++;
                String fileName = file.getFileName().toString();
                var matcher = VERSIONED_MIGRATION.matcher(fileName);
                if (!matcher.matches()) {
                    return FileVisitResult.CONTINUE;
                }

                Path relativePath = root.relativize(file);
                String module = relativePath.getName(0).toString();
                String key = module + ":" + matcher.group(1);
                Path previous = migrationsByModuleAndVersion.putIfAbsent(key, relativePath);
                if (previous != null) {
                    duplicates.add(key + " -> " + previous + ", " + relativePath);
                }
                return FileVisitResult.CONTINUE;
            }
        });

        assertTrue(migrationCount[0] > 0, "No Flyway migration files were discovered");
        assertTrue(duplicates.isEmpty(),
                "Duplicate Flyway migration versions found:\n" + String.join("\n", duplicates));
    }

    private boolean isMigration(Path file) {
        String normalizedPath = file.toString().replace('\\', '/');
        return normalizedPath.contains("/db/migration/") && normalizedPath.endsWith(".sql");
    }

    private void checkContent(String content, String filePath, List<String> violations) {
        String[] lines = content.split("\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.startsWith("--") || line.isEmpty()) continue;

            for (Pattern p : FORBIDDEN) {
                if (p.matcher(line).find()) {
                    violations.add(filePath + ":" + (i + 1) + " forbidden pattern: " + p.pattern() + " in: " + line);
                }
            }
        }
    }

    private Path findProjectRoot() {
        Path current = Paths.get("").toAbsolutePath();
        while (current != null) {
            if (Files.exists(current.resolve("smart-his/pom.xml"))) {
                return current.resolve("smart-his");
            }
            if (Files.exists(current.resolve("pom.xml")) &&
                    Files.exists(current.resolve("his-common"))) {
                return current;
            }
            current = current.getParent();
        }
        fail("Could not find project root");
        return null;
    }
}
