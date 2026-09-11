package com.smarthis.migration;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CollaborationMigrationStructureTest {

    @Test
    void collaborationMigrationMustCreateCoreBusinessTables() throws Exception {
        Path migration = findRepositoryRoot()
                .resolve("his-collaboration/src/main/resources/db/migration/V1__init.sql");
        String sql = Files.readString(migration).toLowerCase();

        List.of(
                "collab_consultation",
                "collab_referral",
                "collab_mdt",
                "collab_care_plan"
        ).forEach(table -> assertTrue(sql.contains("create table " + table),
                () -> "Missing collaboration table: " + table));
    }

    private Path findRepositoryRoot() {
        Path current = Paths.get("").toAbsolutePath();
        while (current != null) {
            if (Files.isRegularFile(current.resolve("pom.xml"))
                    && Files.isDirectory(current.resolve("his-collaboration"))) {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Could not find Smart HIS repository root");
    }
}
