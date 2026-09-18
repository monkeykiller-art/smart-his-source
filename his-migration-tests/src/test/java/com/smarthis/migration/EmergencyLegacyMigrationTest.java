package com.smarthis.migration;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;
import org.junit.jupiter.api.Test;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class EmergencyLegacyMigrationTest {

    @Test
    void legacyEmergencySchemaMustUpgradeToCurrentEntities() throws Exception {
        boolean dockerAvailable = DockerClientFactory.instance().isDockerAvailable();
        if ("true".equalsIgnoreCase(System.getenv("REQUIRE_MIGRATION_TESTS"))) {
            assertTrue(dockerAvailable,
                    "Docker is required for PostgreSQL migration tests in this environment");
        }
        assumeTrue(dockerAvailable, "Docker is unavailable; PostgreSQL migration test skipped");

        try (PostgreSQLContainer container = new PostgreSQLContainer("postgres:16-alpine")
                .withDatabaseName("his_emergency_legacy")
                .withUsername("his")
                .withPassword("his_test_password")) {
            container.start();

            Flyway legacyFlyway = Flyway.configure()
                    .dataSource(container.getJdbcUrl(), container.getUsername(), container.getPassword())
                    .locations("classpath:db/legacy-migration")
                    .schemas("his_emergency")
                    .defaultSchema("his_emergency")
                    .createSchemas(true)
                    .load();

            MigrateResult legacyResult = legacyFlyway.migrate();
            assertEquals(1, legacyResult.migrationsExecuted, "Legacy V1 must apply");

            insertLegacyTriageRow(container);
            insertLegacyResuscitationRow(container);
            insertLegacyObservationRow(container);

            Flyway currentFlyway = Flyway.configure()
                    .dataSource(container.getJdbcUrl(), container.getUsername(), container.getPassword())
                    .locations("filesystem:../his-emergency/src/main/resources/db/migration")
                    .schemas("his_emergency")
                    .defaultSchema("his_emergency")
                    .createSchemas(false)
                    .load();

            currentFlyway.repair();
            MigrateResult currentResult = currentFlyway.migrate();
            assertEquals(1, currentResult.migrationsExecuted, "Only V2 should apply on legacy schema");
            assertTrue(currentFlyway.validateWithResult().validationSuccessful,
                    "Flyway validation must succeed after upgrade");

            assertLegacyDataPreserved(container);
            assertCurrentSchemaAcceptsNewRows(container);
        }
    }

    private void insertLegacyTriageRow(PostgreSQLContainer container) throws Exception {
        String sql = """
                INSERT INTO his_emergency.emg_triage
                    (id, triage_no, patient_id, encounter_id, triage_level, triage_time,
                     chief_complaint, vital_signs, triage_nurse_id, triage_nurse_name,
                     target_dept_id, target_dept_name, wait_time_minutes, triage_status)
                VALUES (?, 'JZ-LEGACY-001', 1001, 2001, 2, '2026-09-18 10:00:00',
                        '胸痛', '{"temperature":36.5}', 3001, '王护士',
                        4001, '急诊科', 15, 'WAITING')
                """;
        try (Connection connection = DriverManager.getConnection(
                container.getJdbcUrl(), container.getUsername(), container.getPassword());
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, 10001L);
            assertEquals(1, statement.executeUpdate());
        }
    }

    private void insertLegacyResuscitationRow(PostgreSQLContainer container) throws Exception {
        String sql = """
                INSERT INTO his_emergency.emg_resuscitation
                    (id, resuscitation_no, patient_id, encounter_id, triage_id,
                     start_time, procedures, medications, outcome, resuscitation_status)
                VALUES (?, 'QS-LEGACY-001', 1001, 2001, 10001,
                        '2026-09-18 10:05:00', '心肺复苏', '肾上腺素', 'SUCCESS', 'COMPLETED')
                """;
        try (Connection connection = DriverManager.getConnection(
                container.getJdbcUrl(), container.getUsername(), container.getPassword());
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, 10002L);
            assertEquals(1, statement.executeUpdate());
        }
    }

    private void insertLegacyObservationRow(PostgreSQLContainer container) throws Exception {
        String sql = """
                INSERT INTO his_emergency.emg_observation
                    (id, observation_no, patient_id, encounter_id, triage_id,
                     bed_no, admit_time, actual_discharge_time, observation_diagnosis,
                     treatment_plan, observation_status)
                VALUES (?, 'LG-LEGACY-001', 1001, 2001, 10001,
                        'A01', '2026-09-18 10:10:00', '2026-09-18 18:00:00', '急性心肌梗死',
                        '抗凝治疗', 'DISCHARGED')
                """;
        try (Connection connection = DriverManager.getConnection(
                container.getJdbcUrl(), container.getUsername(), container.getPassword());
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, 10003L);
            assertEquals(1, statement.executeUpdate());
        }
    }

    private void assertLegacyDataPreserved(PostgreSQLContainer container) throws Exception {
        try (Connection connection = DriverManager.getConnection(
                container.getJdbcUrl(), container.getUsername(), container.getPassword())) {
            assertTriagePreserved(connection);
            assertResuscitationPreserved(connection);
            assertObservationPreserved(connection);
        }
    }

    private void assertTriagePreserved(Connection connection) throws Exception {
        String sql = """
                SELECT triage_no, patient_id, triage_level, chief_complaint,
                       triage_nurse_id, triage_status
                FROM his_emergency.emg_triage WHERE id = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, 10001L);
            try (ResultSet rs = statement.executeQuery()) {
                assertTrue(rs.next(), "Legacy triage row must be preserved");
                assertEquals("JZ-LEGACY-001", rs.getString("triage_no"));
                assertEquals(1001L, rs.getLong("patient_id"));
                assertEquals(2, rs.getShort("triage_level"));
                assertEquals("胸痛", rs.getString("chief_complaint"));
                assertEquals(3001L, rs.getLong("triage_nurse_id"));
                assertEquals("WAITING", rs.getString("triage_status"));
            }
        }
    }

    private void assertResuscitationPreserved(Connection connection) throws Exception {
        String sql = """
                SELECT resuscitation_no, patient_id, triage_id, procedures,
                       outcome, resuscitation_status
                FROM his_emergency.emg_resuscitation WHERE id = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, 10002L);
            try (ResultSet rs = statement.executeQuery()) {
                assertTrue(rs.next(), "Legacy resuscitation row must be preserved");
                assertEquals("QS-LEGACY-001", rs.getString("resuscitation_no"));
                assertEquals(1001L, rs.getLong("patient_id"));
                assertEquals(10001L, rs.getLong("triage_id"));
                assertEquals("心肺复苏", rs.getString("procedures"));
                assertEquals("SUCCESS", rs.getString("outcome"));
                assertEquals("COMPLETED", rs.getString("resuscitation_status"));
            }
        }
    }

    private void assertObservationPreserved(Connection connection) throws Exception {
        String sql = """
                SELECT observation_no, patient_id, triage_id, bed_no,
                       discharge_time, diagnosis, observation_status
                FROM his_emergency.emg_observation WHERE id = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, 10003L);
            try (ResultSet rs = statement.executeQuery()) {
                assertTrue(rs.next(), "Legacy observation row must be preserved");
                assertEquals("LG-LEGACY-001", rs.getString("observation_no"));
                assertEquals(1001L, rs.getLong("patient_id"));
                assertEquals(10001L, rs.getLong("triage_id"));
                assertEquals("A01", rs.getString("bed_no"));
                assertEquals("急性心肌梗死", rs.getString("diagnosis"));
                assertEquals("DISCHARGED", rs.getString("observation_status"));
            }
        }
    }

    private void assertCurrentSchemaAcceptsNewRows(PostgreSQLContainer container) throws Exception {
        try (Connection connection = DriverManager.getConnection(
                container.getJdbcUrl(), container.getUsername(), container.getPassword())) {
            insertCurrentTriageRow(connection);
            insertCurrentResuscitationRow(connection);
            insertCurrentObservationRow(connection);
        }
    }

    private void insertCurrentTriageRow(Connection connection) throws Exception {
        String sql = """
                INSERT INTO his_emergency.emg_triage
                    (id, triage_no, patient_id, triage_level, triage_time,
                     chief_complaint, triage_nurse_id, triage_status)
                VALUES (?, 'JZ-CURRENT-001', 1002, 3, '2026-09-18 11:00:00',
                        '腹痛', 3002, 'WAITING')
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, 20001L);
            assertEquals(1, statement.executeUpdate());
        }
    }

    private void insertCurrentResuscitationRow(Connection connection) throws Exception {
        String sql = """
                INSERT INTO his_emergency.emg_resuscitation
                    (id, triage_id, patient_id, start_time, procedures, resuscitation_status)
                VALUES (?, 20001, 1002, '2026-09-18 11:05:00', '清创缝合', 'IN_PROGRESS')
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, 20002L);
            assertEquals(1, statement.executeUpdate());
        }
    }

    private void insertCurrentObservationRow(Connection connection) throws Exception {
        String sql = """
                INSERT INTO his_emergency.emg_observation
                    (id, triage_id, patient_id, bed_no, admit_time,
                     diagnosis, observation_status)
                VALUES (?, 20001, 1002, 'B02', '2026-09-18 11:10:00',
                        '急性阑尾炎', 'ADMITTED')
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, 20003L);
            assertEquals(1, statement.executeUpdate());
        }
    }
}
