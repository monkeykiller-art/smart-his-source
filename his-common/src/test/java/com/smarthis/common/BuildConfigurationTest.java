package com.smarthis.common;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class BuildConfigurationTest {

    @Test
    void reactorMustContainCurrentDeliveryModules() throws IOException {
        Path root = findProjectRoot();
        String parentPom = Files.readString(root.resolve("pom.xml"));

        for (String module : List.of(
                "his-common", "his-gateway", "his-auth", "his-patient",
                "his-clinical", "his-pharma", "his-operations", "his-migration-tests")) {
            assertTrue(parentPom.contains("<module>" + module + "</module>"),
                    module + " must remain in the basic outpatient reactor");
        }
        for (String removedModule : List.of(
                "his-resource", "his-collaboration", "his-cdss",
                "his-drg", "his-emergency", "his-platform")) {
            assertFalse(parentPom.contains("<module>" + removedModule + "</module>"),
                    removedModule + " must not remain in the basic outpatient reactor");
        }
    }

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
    void parentPomMustPinTestPluginsAndEnforceDependencyHygiene() throws IOException {
        Path root = findProjectRoot();
        String parentPom = Files.readString(root.resolve("pom.xml"));
        String commonPom = Files.readString(root.resolve("his-common/pom.xml"));
        String patientPom = Files.readString(root.resolve("his-patient/pom.xml"));
        String migrationTestsPom = Files.readString(root.resolve("his-migration-tests/pom.xml"));

        assertTrue(parentPom.contains("<maven-compiler-plugin.version>"),
                "Compiler plugin version must be pinned");
        assertTrue(parentPom.contains("<maven-surefire-plugin.version>"),
                "Surefire plugin version must be pinned");
        assertTrue(parentPom.contains("<maven-failsafe-plugin.version>"),
                "Failsafe plugin version must be pinned");
        assertTrue(parentPom.contains("<goal>integration-test</goal>"),
                "Failsafe must participate in the integration-test lifecycle");
        assertTrue(parentPom.contains("<dependencyConvergence/>"),
                "Dependency convergence rule is missing");
        assertTrue(parentPom.contains("<banDuplicateClasses>"),
                "Duplicate class rule is missing");
        assertTrue(parentPom.contains("<bannedDependencies>"),
                "Banned dependency rule is missing");
        assertTrue(parentPom.contains("<artifactId>extra-enforcer-rules</artifactId>"),
                "Extra Enforcer Rules dependency is missing");
        assertTrue(parentPom.contains("<artifactId>hapi-fhir-base</artifactId>"),
                "HAPI FHIR base version must be managed consistently");
        assertTrue(commonPom.contains("<artifactId>jcl-over-slf4j</artifactId>"),
                "Common module must exclude HAPI's duplicate logging bridge");
        assertTrue(patientPom.contains("<artifactId>jcl-over-slf4j</artifactId>"),
                "Patient module must exclude HAPI's duplicate logging bridge");
        assertTrue(patientPom.contains("<artifactId>checker-qual</artifactId>"),
                "Patient module must exclude PostgreSQL's conflicting annotation dependency");
        assertTrue(parentPom.contains("<testcontainers.version>2.0.5</testcontainers.version>"),
                "Testcontainers must remain on the JUnit 4-free 2.x line");
        assertTrue(migrationTestsPom.contains("<artifactId>testcontainers-postgresql</artifactId>"),
                "Migration tests must use the Testcontainers 2.x PostgreSQL module");
    }

    @Test
    void runtimeInfrastructureDependenciesMustBelongToExecutableServices() throws IOException {
        Path root = findProjectRoot();
        String parentPom = Files.readString(root.resolve("pom.xml"));
        int dependenciesStart = parentPom.indexOf("<dependencies>",
                parentPom.indexOf("</dependencyManagement>"));
        int dependenciesEnd = parentPom.indexOf("</dependencies>", dependenciesStart);
        String inheritedDependencies = parentPom.substring(dependenciesStart, dependenciesEnd);
        List<String> runtimeArtifacts = List.of(
                "spring-boot-starter-actuator",
                "spring-cloud-starter-alibaba-nacos-discovery",
                "spring-cloud-starter-alibaba-nacos-config",
                "spring-cloud-starter-bootstrap",
                "micrometer-registry-prometheus");

        for (String artifact : runtimeArtifacts) {
            assertFalse(inheritedDependencies.contains("<artifactId>" + artifact + "</artifactId>"),
                    artifact + " must not be inherited from the parent POM");
        }

        List<String> serviceModules = List.of(
                "his-gateway", "his-auth", "his-patient", "his-clinical",
                "his-operations", "his-pharma");
        for (String module : serviceModules) {
            String modulePom = Files.readString(root.resolve(module).resolve("pom.xml"));
            for (String artifact : runtimeArtifacts) {
                assertTrue(modulePom.contains("<artifactId>" + artifact + "</artifactId>"),
                        module + " must declare " + artifact);
            }
        }
    }

    @Test
    void feignServiceClientsMustIncludeLoadBalancer() throws IOException {
        Path root = findProjectRoot();

        for (String module : List.of("his-patient", "his-clinical")) {
            String modulePom = Files.readString(root.resolve(module).resolve("pom.xml"));
            assertTrue(modulePom.contains("<artifactId>spring-cloud-starter-loadbalancer</artifactId>"),
                    module + " must include Spring Cloud LoadBalancer for name-based Feign clients");
        }
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
        assertTrue(workflow.contains("REQUIRE_MIGRATION_TESTS: \"true\""),
                "Linux CI must require PostgreSQL migration tests");
        assertTrue(Files.isDirectory(root.resolve("his-migration-tests")),
                "PostgreSQL migration test module is missing");
    }

    @Test
    void windowsLocalVerificationMustConfigureThePinnedToolchain() throws IOException {
        Path root = findProjectRoot();
        String script = Files.readString(root.resolve("scripts/verify-local.ps1"));

        assertTrue(script.contains("$env:JAVA_HOME = $JavaHome"),
                "Local verification must configure JAVA_HOME for the test process");
        assertTrue(script.contains("$env:MAVEN_HOME = $MavenHome"),
                "Local verification must configure MAVEN_HOME for the test process");
        assertTrue(script.contains("version \"21\\."),
                "Local verification must reject Java versions other than 21");
        assertTrue(script.contains("$previousErrorActionPreference = $ErrorActionPreference"),
                "Local verification must preserve the caller's PowerShell error policy");
        assertTrue(script.contains("$ErrorActionPreference = \"Continue\""),
                "Windows PowerShell must accept java -version output written to stderr");
        assertTrue(script.contains("$javaExitCode = $LASTEXITCODE"),
                "Local verification must validate the Java process exit code");
        assertTrue(script.contains("$ErrorActionPreference = $previousErrorActionPreference"),
                "Local verification must restore the caller's PowerShell error policy");
        assertTrue(script.contains("clean verify"),
                "Local verification must run the full Maven verification lifecycle");
        assertTrue(script.contains("[switch]$RequireEmptyMavenRepository"),
                "Local verification must support an empty Maven repository check");
        assertTrue(script.contains("Maven repository must be empty"),
                "Local verification must reject a populated repository in clean-cache mode");
        assertTrue(script.contains("& $maven -B -ntp"),
                "Local verification must use non-interactive Maven output");
    }

    @Test
    void windowsLocalStartupMustUseBoundedDatabaseConnectionPools() throws IOException {
        Path root = findProjectRoot();
        String script = Files.readString(root.resolve("scripts/start-local.ps1"));

        assertTrue(script.contains("SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE', '4'"),
                "Local services must use a bounded maximum database pool size");
        assertTrue(script.contains("SPRING_DATASOURCE_HIKARI_MINIMUM_IDLE', '0'"),
                "Local services must not reserve idle database connections at startup");
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
