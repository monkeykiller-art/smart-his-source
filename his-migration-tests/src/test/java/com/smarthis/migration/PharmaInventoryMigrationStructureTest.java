package com.smarthis.migration;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PharmaInventoryMigrationStructureTest {
    @Test
    void inventoryMigrationEnforcesNonNegativeAndTraceableStock() throws IOException {
        Path migration = findRepositoryRoot().resolve("his-pharma/src/main/resources/db/migration/V4__inventory.sql");
        String sql = Files.readString(migration).toLowerCase();
        assertTrue(sql.contains("check (available_quantity >= 0)"));
        assertTrue(sql.contains("available_quantity + locked_quantity <= quantity"));
        assertTrue(sql.contains("create table pha_inventory_transaction"));
        assertTrue(sql.contains("reference_type"));
        assertTrue(sql.contains("reference_id"));
    }

    private Path findRepositoryRoot() {
        Path current = Paths.get("").toAbsolutePath();
        while (current != null) {
            if (Files.isRegularFile(current.resolve("pom.xml")) && Files.isDirectory(current.resolve("his-pharma"))) return current;
            current = current.getParent();
        }
        throw new IllegalStateException("Could not find Smart HIS repository root");
    }
}
