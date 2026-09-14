package com.smarthis.migration;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;

class OperationsBillingMigrationStructureTest {

    @Test
    void billTransactionsMustHaveMoneyAndIdempotencyConstraints() throws Exception {
        Path migration = findRepositoryRoot()
                .resolve("his-operations/src/main/resources/db/migration/V3__bill_transactions.sql");
        String sql = Files.readString(migration).toLowerCase();

        assertTrue(sql.contains("create table ops_bill_transaction"));
        assertTrue(sql.contains("alter table ops_bill add column void_reason"));
        assertTrue(sql.contains("amount          decimal(18,4)"));
        assertTrue(sql.contains("unique (idempotency_key)"));
        assertTrue(sql.contains("check (amount > 0)"));
        assertTrue(sql.contains("transaction_type in ('payment', 'refund')"));
    }

    private Path findRepositoryRoot() {
        Path current = Paths.get("").toAbsolutePath();
        while (current != null) {
            if (Files.isRegularFile(current.resolve("pom.xml"))
                    && Files.isDirectory(current.resolve("his-operations"))) {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Could not find Smart HIS repository root");
    }
}
