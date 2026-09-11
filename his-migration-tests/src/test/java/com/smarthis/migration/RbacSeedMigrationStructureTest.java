package com.smarthis.migration;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RbacSeedMigrationStructureTest {

    @Test
    void administratorRolePermissionIdsMustUseTheirOwnRange() throws Exception {
        Path migration = findRepositoryRoot()
                .resolve("his-auth/src/main/resources/db/migration/V2__seed_rbac.sql");
        String sql = Files.readString(migration).replace("\r\n", "\n");

        assertTrue(sql.contains("SELECT gs, 1, gs, 'system', CURRENT_TIMESTAMP\n"
                        + "FROM generate_series(10001, 10112) AS gs;"),
                "Administrator role-permission IDs must use 10001-10112");
        assertFalse(sql.contains("SELECT 10000 + gs, 1, gs"),
                "Administrator IDs must not overlap the role 2 range starting at 20001");
    }

    private Path findRepositoryRoot() {
        Path current = Paths.get("").toAbsolutePath();
        while (current != null) {
            if (Files.isRegularFile(current.resolve("pom.xml"))
                    && Files.isDirectory(current.resolve("his-auth"))) {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Could not find Smart HIS repository root");
    }
}
