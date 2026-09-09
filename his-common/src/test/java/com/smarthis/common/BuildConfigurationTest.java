package com.smarthis.common;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

    @Test
    void repositoryMustPinAndEnforceTheBuildToolchain() throws IOException {
        Path root = findProjectRoot();
        String parentPom = Files.readString(root.resolve("pom.xml"));
        String wrapperProperties = Files.readString(
                root.resolve(".mvn/wrapper/maven-wrapper.properties"));

        assertTrue(Files.isRegularFile(root.resolve("mvnw")), "Unix Maven wrapper is missing");
        assertTrue(Files.isRegularFile(root.resolve("mvnw.cmd")), "Windows Maven wrapper is missing");
        assertTrue(wrapperProperties.contains("apache-maven-3.9.16-bin.zip"),
                "Maven wrapper must pin Maven 3.9.16");
        assertTrue(wrapperProperties.contains("distributionSha256Sum="),
                "Maven distribution checksum is required");
        assertTrue(parentPom.contains("<requireJavaVersion>"),
                "Parent POM must enforce the Java version");
        assertTrue(parentPom.contains("<requireMavenVersion>"),
                "Parent POM must enforce the Maven version");
    }

    @Test
    void continuousIntegrationMustVerifyLinuxAndWindows() throws IOException {
        Path root = findProjectRoot();
        String workflow = Files.readString(root.resolve(".github/workflows/verify.yml"));

        assertTrue(workflow.contains("runs-on: ubuntu-latest"), "Linux verification job is missing");
        assertTrue(workflow.contains("runs-on: windows-latest"), "Windows verification job is missing");
        assertTrue(workflow.contains("./mvnw -B clean verify"), "Linux clean verify command is missing");
        assertTrue(workflow.contains(".\\mvnw.cmd -B clean verify"),
                "Windows clean verify command is missing");
        assertTrue(workflow.contains("actions/dependency-review-action@v4"),
                "Dependency review job is missing");
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
