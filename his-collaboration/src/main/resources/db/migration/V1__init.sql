-- Smart HIS collaboration schema init

CREATE TABLE collab_consultation (
    id                  BIGINT        NOT NULL,
    consultation_no     VARCHAR(32)   NOT NULL,
    patient_id          BIGINT        NOT NULL,
    encounter_id        BIGINT,
    applicant_id        BIGINT        NOT NULL,
    applicant_name      VARCHAR(64)   NOT NULL,
    applicant_dept_id   BIGINT        NOT NULL,
    applicant_dept_name VARCHAR(64)   NOT NULL,
    target_dept_id      BIGINT        NOT NULL,
    target_dept_name    VARCHAR(64)   NOT NULL,
    consultant_id       BIGINT,
    consultant_name     VARCHAR(64),
    consultation_type   VARCHAR(16)   NOT NULL,
    consultation_reason TEXT          NOT NULL,
    consultation_status VARCHAR(16)   NOT NULL DEFAULT 'PENDING',
    applied_time        TIMESTAMP     NOT NULL,
    accepted_time       TIMESTAMP,
    completed_time      TIMESTAMP,
    consultation_opinion TEXT,
    reject_reason       VARCHAR(500),
    created_by          VARCHAR(64),
    created_time        TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by          VARCHAR(64),
    updated_time        TIMESTAMP,
    deleted             SMALLINT      NOT NULL DEFAULT 0,
    CONSTRAINT pk_collab_consultation PRIMARY KEY (id),
    CONSTRAINT uq_collab_consultation_no UNIQUE (consultation_no)
);
CREATE INDEX idx_collab_consultation_patient ON collab_consultation (patient_id);
CREATE INDEX idx_collab_consultation_target ON collab_consultation (target_dept_id, consultation_status);
CREATE INDEX idx_collab_consultation_applied ON collab_consultation (applied_time);

CREATE TABLE collab_referral (
    id                  BIGINT        NOT NULL,
    referral_no         VARCHAR(32)   NOT NULL,
    patient_id          BIGINT        NOT NULL,
    encounter_id        BIGINT,
    referral_type       VARCHAR(16)   NOT NULL,
    source_org_id       BIGINT        NOT NULL,
    source_org_name     VARCHAR(128)  NOT NULL,
    source_dept_id      BIGINT,
    source_dept_name    VARCHAR(64),
    target_org_id       BIGINT        NOT NULL,
    target_org_name     VARCHAR(128)  NOT NULL,
    target_dept_id      BIGINT,
    target_dept_name    VARCHAR(64),
    referral_reason     TEXT          NOT NULL,
    clinical_summary    TEXT,
    referral_status     VARCHAR(16)   NOT NULL DEFAULT 'PENDING',
    applied_by          BIGINT        NOT NULL,
    applied_time        TIMESTAMP     NOT NULL,
    accepted_by         BIGINT,
    accepted_time       TIMESTAMP,
    completed_time      TIMESTAMP,
    reject_reason       VARCHAR(500),
    created_by          VARCHAR(64),
    created_time        TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by          VARCHAR(64),
    updated_time        TIMESTAMP,
    deleted             SMALLINT      NOT NULL DEFAULT 0,
    CONSTRAINT pk_collab_referral PRIMARY KEY (id),
    CONSTRAINT uq_collab_referral_no UNIQUE (referral_no)
);
CREATE INDEX idx_collab_referral_patient ON collab_referral (patient_id);
CREATE INDEX idx_collab_referral_target ON collab_referral (target_org_id, referral_status);
CREATE INDEX idx_collab_referral_applied ON collab_referral (applied_time);

CREATE TABLE collab_mdt (
    id                  BIGINT        NOT NULL,
    mdt_no              VARCHAR(32)   NOT NULL,
    patient_id          BIGINT        NOT NULL,
    encounter_id        BIGINT,
    mdt_type            VARCHAR(16)   NOT NULL,
    subject             VARCHAR(256)  NOT NULL,
    clinical_summary    TEXT          NOT NULL,
    organizer_id        BIGINT        NOT NULL,
    organizer_name      VARCHAR(64)   NOT NULL,
    organizer_dept_id   BIGINT        NOT NULL,
    organizer_dept_name VARCHAR(64)   NOT NULL,
    participant_details TEXT,
    scheduled_time      TIMESTAMP,
    meeting_location    VARCHAR(256),
    mdt_status          VARCHAR(16)   NOT NULL DEFAULT 'APPLIED',
    conclusion          TEXT,
    completed_time      TIMESTAMP,
    created_by          VARCHAR(64),
    created_time        TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by          VARCHAR(64),
    updated_time        TIMESTAMP,
    deleted             SMALLINT      NOT NULL DEFAULT 0,
    CONSTRAINT pk_collab_mdt PRIMARY KEY (id),
    CONSTRAINT uq_collab_mdt_no UNIQUE (mdt_no)
);
CREATE INDEX idx_collab_mdt_patient ON collab_mdt (patient_id);
CREATE INDEX idx_collab_mdt_schedule ON collab_mdt (scheduled_time, mdt_status);

CREATE TABLE collab_care_plan (
    id                  BIGINT        NOT NULL,
    plan_no             VARCHAR(32)   NOT NULL,
    patient_id          BIGINT        NOT NULL,
    encounter_id        BIGINT,
    plan_type           VARCHAR(24)   NOT NULL,
    plan_title          VARCHAR(256)  NOT NULL,
    assessment          TEXT,
    goals               TEXT         NOT NULL,
    interventions       TEXT         NOT NULL,
    owner_id            BIGINT        NOT NULL,
    owner_name          VARCHAR(64)   NOT NULL,
    owner_dept_id       BIGINT,
    owner_dept_name     VARCHAR(64),
    start_date          DATE          NOT NULL,
    end_date            DATE,
    plan_status         VARCHAR(16)   NOT NULL DEFAULT 'DRAFT',
    completion_summary  TEXT,
    created_by          VARCHAR(64),
    created_time        TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by          VARCHAR(64),
    updated_time        TIMESTAMP,
    deleted             SMALLINT      NOT NULL DEFAULT 0,
    CONSTRAINT pk_collab_care_plan PRIMARY KEY (id),
    CONSTRAINT uq_collab_care_plan_no UNIQUE (plan_no)
);
CREATE INDEX idx_collab_care_plan_patient ON collab_care_plan (patient_id);
CREATE INDEX idx_collab_care_plan_owner ON collab_care_plan (owner_id, plan_status);
CREATE INDEX idx_collab_care_plan_dates ON collab_care_plan (start_date, end_date);
