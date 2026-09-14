package com.smarthis.migration;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.output.MigrateResult;
import org.junit.jupiter.api.Test;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class AllSchemasMigrationTest {

    private static final Map<String, String> SERVICE_SCHEMAS = new LinkedHashMap<>();

    static {
        SERVICE_SCHEMAS.put("his-auth", "his_auth");
        SERVICE_SCHEMAS.put("his-patient", "his_patient");
        SERVICE_SCHEMAS.put("his-clinical", "his_clinical");
        SERVICE_SCHEMAS.put("his-resource", "his_resource");
        SERVICE_SCHEMAS.put("his-operations", "his_operations");
        SERVICE_SCHEMAS.put("his-pharma", "his_pharma");
        SERVICE_SCHEMAS.put("his-collaboration", "his_collab");
        SERVICE_SCHEMAS.put("his-cdss", "his_cdss");
        SERVICE_SCHEMAS.put("his-drg", "his_drg");
        SERVICE_SCHEMAS.put("his-emergency", "his_emergency");
        SERVICE_SCHEMAS.put("his-platform", "his_platform");
    }

    private PostgreSQLContainer postgres;

    @Test
    void everyServiceMigrationMustApplyAndValidateOnAnEmptyPostgreSqlDatabase()
            throws Exception {
        boolean dockerAvailable = DockerClientFactory.instance().isDockerAvailable();
        if ("true".equalsIgnoreCase(System.getenv("REQUIRE_MIGRATION_TESTS"))) {
            assertTrue(dockerAvailable,
                    "Docker is required for PostgreSQL migration tests in this environment");
        }
        assumeTrue(dockerAvailable, "Docker is unavailable; PostgreSQL migration test skipped");

        try (PostgreSQLContainer container = new PostgreSQLContainer("postgres:16-alpine")
                .withDatabaseName("his_migration_test")
                .withUsername("his")
                .withPassword("his_test_password")) {
            container.start();
            postgres = container;
            migrateEveryServiceSchema(findRepositoryRoot());
        }
    }

    private void migrateEveryServiceSchema(Path repositoryRoot) throws Exception {
        int discoveredMigrationCount = 0;

        for (Map.Entry<String, String> service : SERVICE_SCHEMAS.entrySet()) {
            Path migrationDirectory = repositoryRoot.resolve(service.getKey())
                    .resolve("src/main/resources/db/migration");
            assertTrue(Files.isDirectory(migrationDirectory),
                    () -> "Migration directory is missing for " + service.getKey());

            int expectedCount = countVersionedMigrations(migrationDirectory);
            assertTrue(expectedCount > 0,
                    () -> "No versioned migrations found for " + service.getKey());
            discoveredMigrationCount += expectedCount;

            Flyway flyway = Flyway.configure()
                    .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                    .locations("filesystem:" + migrationDirectory.toAbsolutePath())
                    .schemas(service.getValue())
                    .defaultSchema(service.getValue())
                    .createSchemas(true)
                    .load();

            MigrateResult result = flyway.migrate();
            assertEquals(expectedCount, result.migrationsExecuted,
                    () -> "Unexpected migration count for " + service.getKey());
            assertTrue(flyway.validateWithResult().validationSuccessful,
                    () -> "Flyway validation failed for " + service.getKey());

            MigrationInfo[] applied = flyway.info().applied();
            long appliedVersionedCount = Arrays.stream(applied)
                    .filter(migration -> migration.getVersion() != null)
                    .count();
            assertEquals(expectedCount, appliedVersionedCount,
                    () -> "Flyway history is incomplete for " + service.getKey());
            assertTrue(countBusinessTables(service.getValue()) > 0,
                    () -> "Migration did not create business tables for " + service.getKey());
            if ("his-auth".equals(service.getKey())) {
                assertAuthRolePermissionSeedData();
            }
        }

        assertEquals(19, discoveredMigrationCount,
                "Every checked-in Flyway migration must be covered by this test");
    }

    private int countBusinessTables(String schema) throws Exception {
        String sql = """
                SELECT count(*)
                FROM information_schema.tables
                WHERE table_schema = ?
                  AND table_type = 'BASE TABLE'
                  AND table_name <> 'flyway_schema_history'
                """;
        try (Connection connection = DriverManager.getConnection(
                postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, schema);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1);
            }
        }
    }

    private void assertAuthRolePermissionSeedData() throws Exception {
        assertEquals(112, queryForInt("""
                SELECT count(*)
                FROM his_auth.auth_role_permission
                WHERE role_id = 1
                """), "The administrator role must receive every seeded permission");
        assertEquals(112, queryForInt("""
                SELECT count(*)
                FROM his_auth.auth_role_permission role_permission
                JOIN his_auth.auth_permission permission
                  ON permission.id = role_permission.permission_id
                WHERE role_permission.role_id = 1
                  AND role_permission.id = permission.id
                """), "Administrator role-permission IDs must not overlap other role ranges");
    }

    private int queryForInt(String sql) throws Exception {
        try (Connection connection = DriverManager.getConnection(
                postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getInt(1);
        }
    }

    private int countVersionedMigrations(Path migrationDirectory) throws IOException {
        try (var files = Files.list(migrationDirectory)) {
            return Math.toIntExact(files
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().matches("V[0-9]+__.+\\.sql"))
                    .count());
        }
    }

    private Path findRepositoryRoot() {
        Path current = Paths.get("").toAbsolutePath();
        while (current != null) {
            if (Files.isRegularFile(current.resolve("pom.xml"))
                    && Files.isDirectory(current.resolve("his-patient"))) {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Could not find Smart HIS repository root");
    }
}
