package com.smarthis.migration;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CoreSchemaMigrationStructureTest {

    @Test
    void cdssMigrationMustCreateCoreBusinessTables() throws Exception {
        assertCreatesTables("his-cdss", List.of(
                "cdss_knowledge_rule",
                "cdss_decision_request",
                "cdss_decision_result",
                "cdss_alert"));
    }

    @Test
    void platformMigrationMustCreateCoreBusinessTables() throws Exception {
        assertCreatesTables("his-platform", List.of(
                "plt_master_data",
                "plt_data_mapping",
                "plt_exchange_record",
                "plt_quality_result",
                "plt_audit_log"));
    }

    private void assertCreatesTables(String module, List<String> tables) throws Exception {
        Path migration = findRepositoryRoot()
                .resolve(module + "/src/main/resources/db/migration/V1__init.sql");
        String sql = Files.readString(migration).toLowerCase();
        tables.forEach(table -> assertTrue(sql.contains("create table " + table),
                () -> "Missing " + module + " table: " + table));
    }

    private Path findRepositoryRoot() {
        Path current = Paths.get("").toAbsolutePath();
        while (current != null) {
            if (Files.isRegularFile(current.resolve("pom.xml"))
                    && Files.isDirectory(current.resolve("his-cdss"))) {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Could not find Smart HIS repository root");
    }
}
