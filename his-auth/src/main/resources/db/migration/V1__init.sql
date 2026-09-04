-- his_auth schema: RBAC + audit tables
-- D4 portable: no SERIAL/JSONB/TIMESTAMPTZ/BOOLEAN/ILIKE/RETURNING/ON CONFLICT

-- ==================== auth_user ====================
CREATE TABLE auth_user (
    id               BIGINT        NOT NULL,
    username         VARCHAR(64)   NOT NULL,
    employee_no      VARCHAR(32),
    password_hash    VARCHAR(255)  NOT NULL,
    user_type        VARCHAR(32)   NOT NULL DEFAULT 'EMPLOYEE',
    real_name        VARCHAR(64)   NOT NULL,
    gender           SMALLINT      NOT NULL DEFAULT 0,
    phone            VARCHAR(20),
    email            VARCHAR(128),
    dept_id          BIGINT,
    dept_name        VARCHAR(128),
    user_status      VARCHAR(16)   NOT NULL DEFAULT 'ACTIVE',
    failed_attempts  SMALLINT      NOT NULL DEFAULT 0,
    locked_until     TIMESTAMP,
    pwd_updated_at   TIMESTAMP,
    last_login_at    TIMESTAMP,
    last_login_ip    VARCHAR(64),
    prescribe_right  SMALLINT      NOT NULL DEFAULT 0,
    antibiotic_level SMALLINT      NOT NULL DEFAULT 0,
    remark           VARCHAR(512),
    created_by       VARCHAR(64),
    created_time     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by       VARCHAR(64),
    updated_time     TIMESTAMP,
    deleted          SMALLINT      NOT NULL DEFAULT 0,
    CONSTRAINT pk_auth_user PRIMARY KEY (id)
);

CREATE UNIQUE INDEX uq_auth_user_username    ON auth_user (username);
CREATE UNIQUE INDEX uq_auth_user_employee_no ON auth_user (employee_no);
CREATE INDEX idx_auth_user_dept_id     ON auth_user (dept_id);
CREATE INDEX idx_auth_user_user_status ON auth_user (user_status);

-- ==================== auth_role ====================
CREATE TABLE auth_role (
    id          BIGINT       NOT NULL,
    role_code   VARCHAR(64)  NOT NULL,
    role_name   VARCHAR(128) NOT NULL,
    description VARCHAR(512),
    data_scope  VARCHAR(32)  NOT NULL DEFAULT 'SELF',
    builtin     SMALLINT     NOT NULL DEFAULT 0,
    role_status VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE',
    sort_order  INT          NOT NULL DEFAULT 0,
    created_by  VARCHAR(64),
    created_time TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by  VARCHAR(64),
    updated_time TIMESTAMP,
    deleted     SMALLINT     NOT NULL DEFAULT 0,
    CONSTRAINT pk_auth_role PRIMARY KEY (id)
);

CREATE UNIQUE INDEX uq_auth_role_role_code ON auth_role (role_code);

-- ==================== auth_permission ====================
CREATE TABLE auth_permission (
    id            BIGINT       NOT NULL,
    perm_code     VARCHAR(128) NOT NULL,
    perm_name     VARCHAR(128) NOT NULL,
    perm_type     VARCHAR(16)  NOT NULL,
    parent_id     BIGINT       NOT NULL DEFAULT 0,
    module_code   VARCHAR(32),
    resource_path VARCHAR(256),
    http_method   VARCHAR(16),
    icon          VARCHAR(128),
    sort_order    INT          NOT NULL DEFAULT 0,
    perm_status   VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE',
    created_by    VARCHAR(64),
    created_time  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by    VARCHAR(64),
    updated_time  TIMESTAMP,
    deleted       SMALLINT     NOT NULL DEFAULT 0,
    CONSTRAINT pk_auth_permission PRIMARY KEY (id)
);

CREATE UNIQUE INDEX uq_auth_permission_perm_code ON auth_permission (perm_code);
CREATE INDEX idx_auth_permission_module     ON auth_permission (module_code);
CREATE INDEX idx_auth_permission_parent     ON auth_permission (parent_id);
CREATE INDEX idx_auth_permission_perm_type  ON auth_permission (perm_type);

-- ==================== auth_user_role ====================
CREATE TABLE auth_user_role (
    id         BIGINT  NOT NULL,
    user_id    BIGINT  NOT NULL,
    role_id    BIGINT  NOT NULL,
    created_by VARCHAR(64),
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_auth_user_role PRIMARY KEY (id)
);

CREATE UNIQUE INDEX uq_auth_user_role ON auth_user_role (user_id, role_id);
CREATE INDEX idx_auth_user_role_role_id ON auth_user_role (role_id);

-- ==================== auth_role_permission ====================
CREATE TABLE auth_role_permission (
    id            BIGINT  NOT NULL,
    role_id       BIGINT  NOT NULL,
    permission_id BIGINT  NOT NULL,
    created_by    VARCHAR(64),
    created_time  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_auth_role_permission PRIMARY KEY (id)
);

CREATE UNIQUE INDEX uq_auth_role_perm ON auth_role_permission (role_id, permission_id);
CREATE INDEX idx_auth_role_perm_perm_id ON auth_role_permission (permission_id);

-- ==================== auth_login_log ====================
CREATE TABLE auth_login_log (
    id           BIGINT       NOT NULL,
    user_id      BIGINT,
    username     VARCHAR(64),
    login_type   VARCHAR(16)  NOT NULL DEFAULT 'LOGIN',
    login_result VARCHAR(16)  NOT NULL,
    login_ip     VARCHAR(64),
    user_agent   TEXT,
    login_time   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    remark       VARCHAR(512),
    CONSTRAINT pk_auth_login_log PRIMARY KEY (id)
);

CREATE INDEX idx_auth_login_log_user_id   ON auth_login_log (user_id);
CREATE INDEX idx_auth_login_log_username  ON auth_login_log (username);
CREATE INDEX idx_auth_login_log_login_time ON auth_login_log (login_time);

-- ==================== auth_operation_log ====================
CREATE TABLE auth_operation_log (
    id              BIGINT       NOT NULL,
    user_id         BIGINT,
    username        VARCHAR(64),
    operation_type  VARCHAR(32)  NOT NULL,
    module          VARCHAR(64),
    description     VARCHAR(512),
    request_method  VARCHAR(16),
    request_url     VARCHAR(512),
    request_params  TEXT,
    response_code   INT,
    ip              VARCHAR(64),
    execution_time  BIGINT       NOT NULL DEFAULT 0,
    operation_time  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_auth_operation_log PRIMARY KEY (id)
);

CREATE INDEX idx_auth_op_log_user_id  ON auth_operation_log (user_id);
CREATE INDEX idx_auth_op_log_username ON auth_operation_log (username);
CREATE INDEX idx_auth_op_log_op_time  ON auth_operation_log (operation_time);
CREATE INDEX idx_auth_op_log_module   ON auth_operation_log (module);
