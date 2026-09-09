package com.smarthis.common;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

class BuildConfigurationTest {

    @Test
    void parentPomMustNotUseSystemScopedDependencies() throws IOException {
        Path root = findProjectRoot();
        String parentPom = Files.readString(root.resolve("pom.xml"));

        assertFalse(parentPom.contains("<systemPath>"),
                "Use repository-resolved dependencies instead of systemPath");
        assertFalse(parentPom.contains("<scope>system</scope>"),
                "System-scoped dependencies make builds machine-specific");
    }

    private Path findProjectRoot() {
        Path current = Paths.get("").toAbsolutePath();
        while (current != null) {
            if (Files.exists(current.resolve("pom.xml"))
                    && Files.exists(current.resolve("his-common"))) {
                return current;
            }
            current = current.getParent();
        }
        fail("Could not find project root");
        return null;
    }
}
