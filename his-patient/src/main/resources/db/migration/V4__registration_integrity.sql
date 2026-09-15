-- M1 registration integrity: prevent duplicate active registrations for one patient and schedule.
CREATE UNIQUE INDEX uq_pat_reg_active_patient_schedule
    ON pat_registration (patient_id, schedule_id)
    WHERE deleted = 0 AND reg_status = 'ACTIVE';
