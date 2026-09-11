-- V2: Seed RBAC data
-- Password: His@2026 (BCrypt hash placeholder - DemoPasswordInitializer fixes at first startup)
-- Roles: 9  Permissions: 112  Demo Users: 8

-- ========================= ROLES =========================
INSERT INTO auth_role (id, role_code, role_name, description, data_scope, builtin, role_status, sort_order, created_by, created_time) VALUES
(1, 'ADMIN',                  'System Administrator',     'Full system access',                         'ALL',              1, 'ACTIVE', 1, 'system', CURRENT_TIMESTAMP),
(2, 'OUTPATIENT_DOCTOR',      'Outpatient Doctor',        'Outpatient consultation and prescription',   'DEPT',             1, 'ACTIVE', 2, 'system', CURRENT_TIMESTAMP),
(3, 'INPATIENT_DOCTOR',       'Inpatient Doctor',         'Inpatient orders and treatment',             'DEPT',             1, 'ACTIVE', 3, 'system', CURRENT_TIMESTAMP),
(4, 'NURSE',                  'Nurse',                    'Nursing care, triage, vital signs',          'DEPT',             1, 'ACTIVE', 4, 'system', CURRENT_TIMESTAMP),
(5, 'PHARMACIST',             'Pharmacist',               'Dispensing and inventory',                   'DEPT',             1, 'ACTIVE', 5, 'system', CURRENT_TIMESTAMP),
(6, 'CLINICAL_PHARMACIST',    'Clinical Pharmacist',      'Rx review, drug knowledge, ADR',             'ALL',              1, 'ACTIVE', 6, 'system', CURRENT_TIMESTAMP),
(7, 'CASHIER',                'Cashier',                  'Billing, payment, invoicing',                'SELF',             1, 'ACTIVE', 7, 'system', CURRENT_TIMESTAMP),
(8, 'REGISTRAR',              'Registrar',                'Patient registration and scheduling',        'DEPT',             1, 'ACTIVE', 8, 'system', CURRENT_TIMESTAMP),
(9, 'MEDICAL_RECORD_CODER',   'Medical Record Coder',     'DRG coding, audit, statistics',              'ALL',              1, 'ACTIVE', 9, 'system', CURRENT_TIMESTAMP);

-- ========================= PERMISSIONS =========================
-- Auth module (10)
INSERT INTO auth_permission (id, perm_code, perm_name, perm_type, parent_id, module_code, resource_path, http_method, sort_order, perm_status, created_by, created_time) VALUES
(10001, 'auth:user:list',      'List Users',      'API',    0, 'auth', '/api/auth/users',          'GET',    1, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10002, 'auth:user:create',    'Create User',     'API',    0, 'auth', '/api/auth/users',          'POST',   2, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10003, 'auth:user:update',    'Update User',     'API',    0, 'auth', '/api/auth/users/*',        'PUT',    3, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10004, 'auth:user:delete',    'Delete User',     'API',    0, 'auth', '/api/auth/users/*',        'DELETE', 4, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10005, 'auth:user:reset-pwd', 'Reset Password',  'API',    0, 'auth', '/api/auth/users/*/reset-password', 'POST', 5, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10006, 'auth:role:list',      'List Roles',      'API',    0, 'auth', '/api/auth/roles',          'GET',    6, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10007, 'auth:role:create',    'Create Role',     'API',    0, 'auth', '/api/auth/roles',          'POST',   7, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10008, 'auth:role:update',    'Update Role',     'API',    0, 'auth', '/api/auth/roles/*',        'PUT',    8, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10009, 'auth:role:delete',    'Delete Role',     'API',    0, 'auth', '/api/auth/roles/*',        'DELETE', 9, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10010, 'auth:log:list',       'List Logs',       'API',    0, 'auth', '/api/auth/logs/**',        'GET',   10, 'ACTIVE', 'system', CURRENT_TIMESTAMP);

-- Patient module (14)
INSERT INTO auth_permission (id, perm_code, perm_name, perm_type, parent_id, module_code, resource_path, http_method, sort_order, perm_status, created_by, created_time) VALUES
(10011, 'patient:patient:list',     'List Patients',     'API', 0, 'patient', '/api/patient/patients',       'GET',    1, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10012, 'patient:patient:create',   'Create Patient',    'API', 0, 'patient', '/api/patient/patients',       'POST',   2, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10013, 'patient:patient:update',   'Update Patient',    'API', 0, 'patient', '/api/patient/patients/*',     'PUT',    3, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10014, 'patient:patient:read',     'View Patient',      'API', 0, 'patient', '/api/patient/patients/*',     'GET',    4, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10015, 'patient:registration:create', 'Register',       'API', 0, 'patient', '/api/patient/registrations',  'POST',   5, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10016, 'patient:registration:cancel', 'Cancel Registration', 'API', 0, 'patient', '/api/patient/registrations/*/cancel', 'POST', 6, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10017, 'patient:registration:read',   'View Registration',   'API', 0, 'patient', '/api/patient/registrations/*', 'GET', 7, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10018, 'patient:registration:list',   'List Registrations',  'API', 0, 'patient', '/api/patient/registrations', 'GET', 8, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10019, 'patient:appointment:create',  'Book Appointment',    'API', 0, 'patient', '/api/patient/appointments', 'POST', 9, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10020, 'patient:appointment:cancel',  'Cancel Appointment',  'API', 0, 'patient', '/api/patient/appointments/*/cancel', 'POST', 10, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10021, 'patient:triage:update',       'Update Triage',       'API', 0, 'patient', '/api/patient/triage/*',  'PUT',   11, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10022, 'patient:department:list',     'List Departments',    'API', 0, 'patient', '/api/patient/departments', 'GET', 12, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10023, 'patient:doctor:list',         'List Doctors',        'API', 0, 'patient', '/api/patient/doctors',    'GET',   13, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10024, 'patient:schedule:read',       'View Schedule',       'API', 0, 'patient', '/api/patient/schedules',  'GET',   14, 'ACTIVE', 'system', CURRENT_TIMESTAMP);

-- Clinical module (15)
INSERT INTO auth_permission (id, perm_code, perm_name, perm_type, parent_id, module_code, resource_path, http_method, sort_order, perm_status, created_by, created_time) VALUES
(10025, 'clinical:record:create',   'Create Record',    'API', 0, 'clinical', '/api/clinical/records',       'POST',   1, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10026, 'clinical:record:update',   'Update Record',    'API', 0, 'clinical', '/api/clinical/records/*',     'PUT',    2, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10027, 'clinical:record:sign',     'Sign Record',      'API', 0, 'clinical', '/api/clinical/records/*/sign','POST',   3, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10028, 'clinical:record:read',     'View Record',      'API', 0, 'clinical', '/api/clinical/records/*',     'GET',    4, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10029, 'clinical:diagnosis:create','Create Diagnosis', 'API', 0, 'clinical', '/api/clinical/diagnoses',     'POST',   5, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10030, 'clinical:diagnosis:update','Update Diagnosis', 'API', 0, 'clinical', '/api/clinical/diagnoses/*',   'PUT',    6, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10031, 'clinical:prescription:create', 'Create Prescription', 'API', 0, 'clinical', '/api/clinical/prescriptions', 'POST', 7, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10032, 'clinical:prescription:read',   'View Prescription',   'API', 0, 'clinical', '/api/clinical/prescriptions/*', 'GET', 8, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10033, 'clinical:icd10:search',        'Search ICD-10',       'API', 0, 'clinical', '/api/clinical/icd10',  'GET',    9, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10034, 'clinical:order:create',    'Create Order',     'API', 0, 'clinical', '/api/clinical/orders',        'POST',  10, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10035, 'clinical:order:verify',    'Verify Order',     'API', 0, 'clinical', '/api/clinical/orders/*/verify','POST', 11, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10036, 'clinical:order:execute',   'Execute Order',    'API', 0, 'clinical', '/api/clinical/orders/*/execute','POST',12, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10037, 'clinical:order:stop',      'Stop Order',       'API', 0, 'clinical', '/api/clinical/orders/*/stop', 'POST',  13, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10038, 'clinical:nursing:create',  'Create Nursing',   'API', 0, 'clinical', '/api/clinical/nursing',       'POST',  14, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10039, 'clinical:nursing:update',  'Update Nursing',   'API', 0, 'clinical', '/api/clinical/nursing/*',     'PUT',   15, 'ACTIVE', 'system', CURRENT_TIMESTAMP);

-- Resource module (12)
INSERT INTO auth_permission (id, perm_code, perm_name, perm_type, parent_id, module_code, resource_path, http_method, sort_order, perm_status, created_by, created_time) VALUES
(10040, 'resource:drug:list',      'List Drugs',       'API', 0, 'resource', '/api/resource/drugs',         'GET',    1, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10041, 'resource:drug:create',    'Create Drug',      'API', 0, 'resource', '/api/resource/drugs',         'POST',   2, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10042, 'resource:drug:update',    'Update Drug',      'API', 0, 'resource', '/api/resource/drugs/*',       'PUT',    3, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10043, 'resource:stock:list',     'List Stock',       'API', 0, 'resource', '/api/resource/stocks',        'GET',    4, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10044, 'resource:stock:adjust',   'Adjust Stock',     'API', 0, 'resource', '/api/resource/stocks/*/adjust','POST', 5, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10045, 'resource:dispense:list',  'List Dispenses',   'API', 0, 'resource', '/api/resource/dispenses',     'GET',    6, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10046, 'resource:dispense:prepare','Prepare Dispense', 'API', 0, 'resource', '/api/resource/dispenses/*/prepare','POST', 7, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10047, 'resource:dispense:complete','Complete Dispense','API', 0, 'resource', '/api/resource/dispenses/*/dispense','POST',8, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10048, 'resource:storage:list',   'List Storage',     'API', 0, 'resource', '/api/resource/storages',      'GET',    9, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10049, 'resource:storage:create', 'Create Storage',   'API', 0, 'resource', '/api/resource/storages',      'POST',  10, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10050, 'resource:ward:list',      'List Wards',       'API', 0, 'resource', '/api/resource/wards',         'GET',   11, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10051, 'resource:bed:manage',     'Manage Beds',      'API', 0, 'resource', '/api/resource/beds/**',       'POST',  12, 'ACTIVE', 'system', CURRENT_TIMESTAMP);

-- Operations module (16)
INSERT INTO auth_permission (id, perm_code, perm_name, perm_type, parent_id, module_code, resource_path, http_method, sort_order, perm_status, created_by, created_time) VALUES
(10052, 'operations:bill:list',      'List Bills',        'API', 0, 'operations', '/api/operations/bills',         'GET',    1, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10053, 'operations:bill:create',    'Create Bill',       'API', 0, 'operations', '/api/operations/bills',         'POST',   2, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10054, 'operations:bill:settle',    'Settle Bill',       'API', 0, 'operations', '/api/operations/bills/*/settle','POST',   3, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10055, 'operations:bill:void',      'Void Bill',         'API', 0, 'operations', '/api/operations/bills/*/void',  'POST',   4, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10056, 'operations:bill:read',      'View Bill',         'API', 0, 'operations', '/api/operations/bills/*',       'GET',    5, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10057, 'operations:payment:create', 'Create Payment',    'API', 0, 'operations', '/api/operations/payments',      'POST',   6, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10058, 'operations:payment:refund', 'Refund Payment',    'API', 0, 'operations', '/api/operations/payments/*/refund','POST',7, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10059, 'operations:insurance:verify','Verify Insurance', 'API', 0, 'operations', '/api/operations/insurance/verify','POST', 8, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10060, 'operations:insurance:preauth','Insurance Preauth','API', 0, 'operations', '/api/operations/insurance/preauth','POST',9, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10061, 'operations:insurance:settle','Settle Insurance', 'API', 0, 'operations', '/api/operations/insurance/settle','POST',10, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10062, 'operations:charge-item:list','List Charge Items','API', 0, 'operations', '/api/operations/charge-items',  'GET',   11, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10063, 'operations:charge-item:create','Create Charge Item','API',0,'operations','/api/operations/charge-items',  'POST',  12, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10064, 'operations:charge-item:update','Update Charge Item','API',0,'operations','/api/operations/charge-items/*','PUT',   13, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10065, 'operations:prepaid:list',   'List Prepaid',      'API', 0, 'operations', '/api/operations/prepaid',       'GET',   14, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10066, 'operations:revenue:stat',   'Revenue Statistics','API', 0, 'operations', '/api/operations/revenue/**',    'GET',   15, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10067, 'operations:workload:stat',  'Workload Statistics','API',0, 'operations', '/api/operations/workload/**',   'GET',   16, 'ACTIVE', 'system', CURRENT_TIMESTAMP);

-- Pharma module (10)
INSERT INTO auth_permission (id, perm_code, perm_name, perm_type, parent_id, module_code, resource_path, http_method, sort_order, perm_status, created_by, created_time) VALUES
(10068, 'pharma:review:list',       'List Reviews',       'API', 0, 'pharma', '/api/pharma/reviews',            'GET',    1, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10069, 'pharma:review:create',     'Create Review',      'API', 0, 'pharma', '/api/pharma/reviews',            'POST',   2, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10070, 'pharma:review:approve',    'Approve Review',     'API', 0, 'pharma', '/api/pharma/reviews/*/approve',  'POST',   3, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10071, 'pharma:review:reject',     'Reject Review',      'API', 0, 'pharma', '/api/pharma/reviews/*/reject',   'POST',   4, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10072, 'pharma:review:override',   'Override Review',    'API', 0, 'pharma', '/api/pharma/reviews/*/override', 'POST',   5, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10073, 'pharma:rule:list',         'List Rules',         'API', 0, 'pharma', '/api/pharma/rules',              'GET',    6, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10074, 'pharma:rule:create',       'Create Rule',        'API', 0, 'pharma', '/api/pharma/rules',              'POST',   7, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10075, 'pharma:knowledge:list',    'List Knowledge',     'API', 0, 'pharma', '/api/pharma/knowledge',          'GET',    8, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10076, 'pharma:knowledge:create',  'Create Knowledge',   'API', 0, 'pharma', '/api/pharma/knowledge',          'POST',   9, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10077, 'pharma:interaction:list',  'List Interactions',  'API', 0, 'pharma', '/api/pharma/interactions',       'GET',   10, 'ACTIVE', 'system', CURRENT_TIMESTAMP);

-- CDSS module (8)
INSERT INTO auth_permission (id, perm_code, perm_name, perm_type, parent_id, module_code, resource_path, http_method, sort_order, perm_status, created_by, created_time) VALUES
(10078, 'cdss:diagnosis:suggest',   'Diagnosis Suggestion', 'API', 0, 'cdss', '/api/cdss/diagnosis/suggest',  'POST',   1, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10079, 'cdss:alert:list',          'List Alerts',           'API', 0, 'cdss', '/api/cdss/alerts',            'GET',    2, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10080, 'cdss:alert:ack',           'Acknowledge Alert',     'API', 0, 'cdss', '/api/cdss/alerts/*/ack',      'POST',   3, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10081, 'cdss:pathway:list',        'List Pathways',         'API', 0, 'cdss', '/api/cdss/pathways',          'GET',    4, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10082, 'cdss:pathway:manage',      'Manage Pathways',       'API', 0, 'cdss', '/api/cdss/pathways/**',       'POST',   5, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10083, 'cdss:knowledge:list',      'List Knowledge',        'API', 0, 'cdss', '/api/cdss/knowledge',         'GET',    6, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10084, 'cdss:knowledge:create',    'Create Knowledge',      'API', 0, 'cdss', '/api/cdss/knowledge',         'POST',   7, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10085, 'cdss:quality:list',        'Quality Indicators',    'API', 0, 'cdss', '/api/cdss/quality/**',        'GET',    8, 'ACTIVE', 'system', CURRENT_TIMESTAMP);

-- Emergency module (7)
INSERT INTO auth_permission (id, perm_code, perm_name, perm_type, parent_id, module_code, resource_path, http_method, sort_order, perm_status, created_by, created_time) VALUES
(10086, 'emergency:triage:create',  'Create Triage',     'API', 0, 'emergency', '/api/emergency/triage',       'POST',   1, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10087, 'emergency:triage:update',  'Update Triage',     'API', 0, 'emergency', '/api/emergency/triage/*',     'PUT',    2, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10088, 'emergency:triage:list',    'List Triage',       'API', 0, 'emergency', '/api/emergency/triage',       'GET',    3, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10089, 'emergency:greenchannel:create','Open Green Channel','API',0,'emergency','/api/emergency/green-channels','POST',  4, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10090, 'emergency:greenchannel:update','Update Green Channel','API',0,'emergency','/api/emergency/green-channels/*','PUT',5, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10091, 'emergency:resuscitation:create','Create Resuscitation','API',0,'emergency','/api/emergency/resuscitations','POST',6,'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10092, 'emergency:observation:manage', 'Manage Observation','API',0, 'emergency', '/api/emergency/observations/**','POST',7,'ACTIVE', 'system', CURRENT_TIMESTAMP);

-- DRG module (8)
INSERT INTO auth_permission (id, perm_code, perm_name, perm_type, parent_id, module_code, resource_path, http_method, sort_order, perm_status, created_by, created_time) VALUES
(10093, 'drg:case:list',         'List Cases',         'API', 0, 'drg', '/api/drg/cases',              'GET',    1, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10094, 'drg:case:group',        'Group Case',         'API', 0, 'drg', '/api/drg/cases/*/group',      'POST',   2, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10095, 'drg:audit:list',        'List Audits',        'API', 0, 'drg', '/api/drg/audits',             'GET',    3, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10096, 'drg:audit:create',      'Create Audit',       'API', 0, 'drg', '/api/drg/audits',             'POST',   4, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10097, 'drg:simulation:create', 'Create Simulation',  'API', 0, 'drg', '/api/drg/simulations',        'POST',   5, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10098, 'drg:simulation:list',   'List Simulations',   'API', 0, 'drg', '/api/drg/simulations',        'GET',    6, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10099, 'drg:performance:list',  'Dept Performance',   'API', 0, 'drg', '/api/drg/performance',        'GET',    7, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10100, 'drg:cost:list',         'Cost Control',       'API', 0, 'drg', '/api/drg/cost-control',       'GET',    8, 'ACTIVE', 'system', CURRENT_TIMESTAMP);

-- Platform module (12)
INSERT INTO auth_permission (id, perm_code, perm_name, perm_type, parent_id, module_code, resource_path, http_method, sort_order, perm_status, created_by, created_time) VALUES
(10101, 'platform:master-data:list',    'List Master Data',    'API', 0, 'platform', '/api/platform/master-data',     'GET',    1, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10102, 'platform:master-data:create',  'Create Master Data',  'API', 0, 'platform', '/api/platform/master-data',     'POST',   2, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10103, 'platform:master-data:update',  'Update Master Data',  'API', 0, 'platform', '/api/platform/master-data/*',   'PUT',    3, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10104, 'platform:master-data:publish', 'Publish Master Data', 'API', 0, 'platform', '/api/platform/master-data/*/publish','POST',4, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10105, 'platform:exchange:list',       'List Exchanges',      'API', 0, 'platform', '/api/platform/exchanges',       'GET',    5, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10106, 'platform:exchange:create',     'Create Exchange',     'API', 0, 'platform', '/api/platform/exchanges',       'POST',   6, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10107, 'platform:mapping:list',        'List Mappings',       'API', 0, 'platform', '/api/platform/mappings',        'GET',    7, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10108, 'platform:quality:list',        'List Quality Results','API', 0, 'platform', '/api/platform/quality',         'GET',    8, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10109, 'platform:quality:create',      'Create Quality Rule', 'API', 0, 'platform', '/api/platform/quality',         'POST',   9, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10110, 'platform:monitor:list',        'Integration Monitor', 'API', 0, 'platform', '/api/platform/monitor',         'GET',   10, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10111, 'platform:audit:list',          'Audit Trail',         'API', 0, 'platform', '/api/platform/audit',           'GET',   11, 'ACTIVE', 'system', CURRENT_TIMESTAMP),
(10112, 'platform:fhir:access',         'FHIR Access',         'API', 0, 'platform', '/api/platform/fhir/**',         'GET',   12, 'ACTIVE', 'system', CURRENT_TIMESTAMP);

-- ========================= DEMO USERS =========================
-- Password placeholder: DemoPasswordInitializer will fix at first startup
INSERT INTO auth_user (id, username, employee_no, password_hash, user_type, real_name, gender, phone, dept_id, dept_name, user_status, prescribe_right, antibiotic_level, created_by, created_time) VALUES
(1001, 'admin',        'EMP001', '$2a$PLACEHOLDER', 'EMPLOYEE', 'System Admin',        1, '13800000001', NULL, NULL,           'ACTIVE', 0, 0, 'system', CURRENT_TIMESTAMP),
(1002, 'zhangyisheng', 'DOC001', '$2a$PLACEHOLDER', 'EMPLOYEE', 'Zhang Yisheng',       1, '13800000002', 1001, 'Internal Medicine', 'ACTIVE', 1, 2, 'system', CURRENT_TIMESTAMP),
(1003, 'liyisheng',    'DOC002', '$2a$PLACEHOLDER', 'EMPLOYEE', 'Li Yisheng',          1, '13800000003', 1002, 'Surgery',           'ACTIVE', 1, 2, 'system', CURRENT_TIMESTAMP),
(1004, 'wanghushi',    'NUR001', '$2a$PLACEHOLDER', 'EMPLOYEE', 'Wang Hushi',          0, '13800000004', 1001, 'Internal Medicine', 'ACTIVE', 0, 0, 'system', CURRENT_TIMESTAMP),
(1005, 'zhaoyaoshi',   'PHA001', '$2a$PLACEHOLDER', 'EMPLOYEE', 'Zhao Yaoshi',         1, '13800000005', NULL, 'Pharmacy',          'ACTIVE', 0, 0, 'system', CURRENT_TIMESTAMP),
(1006, 'sunshoufei',   'PHA002', '$2a$PLACEHOLDER', 'EMPLOYEE', 'Sun Shoufei',         1, '13800000006', NULL, 'Pharmacy',          'ACTIVE', 1, 3, 'system', CURRENT_TIMESTAMP),
(1007, 'qiandaoyi',    'REG001', '$2a$PLACEHOLDER', 'EMPLOYEE', 'Qian Daoyi',          0, '13800000007', NULL, 'Registration',      'ACTIVE', 0, 0, 'system', CURRENT_TIMESTAMP),
(1008, 'zhouyisheng',  'DOC003', '$2a$PLACEHOLDER', 'EMPLOYEE', 'Zhou Yisheng',        1, '13800000008', 1010, 'Emergency',         'ACTIVE', 1, 2, 'system', CURRENT_TIMESTAMP);

-- ========================= USER-ROLE =========================
INSERT INTO auth_user_role (id, user_id, role_id, created_by, created_time) VALUES
(1, 1001, 1, 'system', CURRENT_TIMESTAMP),
(2, 1002, 2, 'system', CURRENT_TIMESTAMP),
(3, 1003, 3, 'system', CURRENT_TIMESTAMP),
(4, 1004, 4, 'system', CURRENT_TIMESTAMP),
(5, 1005, 5, 'system', CURRENT_TIMESTAMP),
(6, 1006, 6, 'system', CURRENT_TIMESTAMP),
(7, 1007, 8, 'system', CURRENT_TIMESTAMP),
(8, 1008, 4, 'system', CURRENT_TIMESTAMP),
(9, 1008, 2, 'system', CURRENT_TIMESTAMP);

-- ========================= ROLE-PERMISSION =========================
-- ADMIN: all permissions (10001-10112)
INSERT INTO auth_role_permission (id, role_id, permission_id, created_by, created_time)
SELECT gs, 1, gs, 'system', CURRENT_TIMESTAMP
FROM generate_series(10001, 10112) AS gs;

-- OUTPATIENT_DOCTOR (role 2): patient + clinical + resource read + cdss
INSERT INTO auth_role_permission (id, role_id, permission_id, created_by, created_time) VALUES
-- patient: list/read patients, registration, appointment, triage, dept/doctor/schedule
(20001, 2, 10011, 'system', CURRENT_TIMESTAMP),
(20002, 2, 10012, 'system', CURRENT_TIMESTAMP),
(20003, 2, 10014, 'system', CURRENT_TIMESTAMP),
(20004, 2, 10015, 'system', CURRENT_TIMESTAMP),
(20005, 2, 10017, 'system', CURRENT_TIMESTAMP),
(20006, 2, 10018, 'system', CURRENT_TIMESTAMP),
(20007, 2, 10019, 'system', CURRENT_TIMESTAMP),
(20008, 2, 10022, 'system', CURRENT_TIMESTAMP),
(20009, 2, 10023, 'system', CURRENT_TIMESTAMP),
(20010, 2, 10024, 'system', CURRENT_TIMESTAMP),
-- clinical: records, diagnosis, prescription, icd10
(20011, 2, 10025, 'system', CURRENT_TIMESTAMP),
(20012, 2, 10026, 'system', CURRENT_TIMESTAMP),
(20013, 2, 10027, 'system', CURRENT_TIMESTAMP),
(20014, 2, 10028, 'system', CURRENT_TIMESTAMP),
(20015, 2, 10029, 'system', CURRENT_TIMESTAMP),
(20016, 2, 10030, 'system', CURRENT_TIMESTAMP),
(20017, 2, 10031, 'system', CURRENT_TIMESTAMP),
(20018, 2, 10032, 'system', CURRENT_TIMESTAMP),
(20019, 2, 10033, 'system', CURRENT_TIMESTAMP),
-- resource: drug list, stock list
(20020, 2, 10040, 'system', CURRENT_TIMESTAMP),
(20021, 2, 10043, 'system', CURRENT_TIMESTAMP),
-- cdss: diagnosis suggest, alert list/ack, knowledge
(20022, 2, 10078, 'system', CURRENT_TIMESTAMP),
(20023, 2, 10079, 'system', CURRENT_TIMESTAMP),
(20024, 2, 10080, 'system', CURRENT_TIMESTAMP),
(20025, 2, 10083, 'system', CURRENT_TIMESTAMP),
-- operations: bill read
(20026, 2, 10052, 'system', CURRENT_TIMESTAMP),
(20027, 2, 10056, 'system', CURRENT_TIMESTAMP);

-- INPATIENT_DOCTOR (role 3): admission + orders + nursing read + bed list
INSERT INTO auth_role_permission (id, role_id, permission_id, created_by, created_time) VALUES
(30001, 3, 10011, 'system', CURRENT_TIMESTAMP),
(30002, 3, 10014, 'system', CURRENT_TIMESTAMP),
(30003, 3, 10025, 'system', CURRENT_TIMESTAMP),
(30004, 3, 10026, 'system', CURRENT_TIMESTAMP),
(30005, 3, 10027, 'system', CURRENT_TIMESTAMP),
(30006, 3, 10028, 'system', CURRENT_TIMESTAMP),
(30007, 3, 10029, 'system', CURRENT_TIMESTAMP),
(30008, 3, 10030, 'system', CURRENT_TIMESTAMP),
(30009, 3, 10031, 'system', CURRENT_TIMESTAMP),
(30010, 3, 10032, 'system', CURRENT_TIMESTAMP),
(30011, 3, 10033, 'system', CURRENT_TIMESTAMP),
(30012, 3, 10034, 'system', CURRENT_TIMESTAMP),
(30013, 3, 10037, 'system', CURRENT_TIMESTAMP),
(30014, 3, 10038, 'system', CURRENT_TIMESTAMP),
(30015, 3, 10039, 'system', CURRENT_TIMESTAMP),
(30016, 3, 10040, 'system', CURRENT_TIMESTAMP),
(30017, 3, 10050, 'system', CURRENT_TIMESTAMP),
(30018, 3, 10051, 'system', CURRENT_TIMESTAMP),
(30019, 3, 10078, 'system', CURRENT_TIMESTAMP),
(30020, 3, 10079, 'system', CURRENT_TIMESTAMP),
(30021, 3, 10080, 'system', CURRENT_TIMESTAMP);

-- NURSE (role 4): triage + nursing + order verify/execute + patient read
INSERT INTO auth_role_permission (id, role_id, permission_id, created_by, created_time) VALUES
(40001, 4, 10011, 'system', CURRENT_TIMESTAMP),
(40002, 4, 10014, 'system', CURRENT_TIMESTAMP),
(40003, 4, 10017, 'system', CURRENT_TIMESTAMP),
(40004, 4, 10018, 'system', CURRENT_TIMESTAMP),
(40005, 4, 10021, 'system', CURRENT_TIMESTAMP),
(40006, 4, 10028, 'system', CURRENT_TIMESTAMP),
(40007, 4, 10035, 'system', CURRENT_TIMESTAMP),
(40008, 4, 10036, 'system', CURRENT_TIMESTAMP),
(40009, 4, 10037, 'system', CURRENT_TIMESTAMP),
(40010, 4, 10038, 'system', CURRENT_TIMESTAMP),
(40011, 4, 10039, 'system', CURRENT_TIMESTAMP),
(40012, 4, 10050, 'system', CURRENT_TIMESTAMP),
(40013, 4, 10051, 'system', CURRENT_TIMESTAMP),
(40014, 4, 10079, 'system', CURRENT_TIMESTAMP),
(40015, 4, 10080, 'system', CURRENT_TIMESTAMP),
-- emergency triage
(40016, 4, 10086, 'system', CURRENT_TIMESTAMP),
(40017, 4, 10087, 'system', CURRENT_TIMESTAMP),
(40018, 4, 10088, 'system', CURRENT_TIMESTAMP);

-- PHARMACIST (role 5): dispense + drug catalog + stock + pharma review list
INSERT INTO auth_role_permission (id, role_id, permission_id, created_by, created_time) VALUES
(50001, 5, 10040, 'system', CURRENT_TIMESTAMP),
(50002, 5, 10041, 'system', CURRENT_TIMESTAMP),
(50003, 5, 10042, 'system', CURRENT_TIMESTAMP),
(50004, 5, 10043, 'system', CURRENT_TIMESTAMP),
(50005, 5, 10044, 'system', CURRENT_TIMESTAMP),
(50006, 5, 10045, 'system', CURRENT_TIMESTAMP),
(50007, 5, 10046, 'system', CURRENT_TIMESTAMP),
(50008, 5, 10047, 'system', CURRENT_TIMESTAMP),
(50009, 5, 10048, 'system', CURRENT_TIMESTAMP),
(50010, 5, 10049, 'system', CURRENT_TIMESTAMP),
(50011, 5, 10068, 'system', CURRENT_TIMESTAMP),
(50012, 5, 10077, 'system', CURRENT_TIMESTAMP);

-- CLINICAL_PHARMACIST (role 6): pharma full + cdss + drug knowledge
INSERT INTO auth_role_permission (id, role_id, permission_id, created_by, created_time) VALUES
(60001, 6, 10040, 'system', CURRENT_TIMESTAMP),
(60002, 6, 10068, 'system', CURRENT_TIMESTAMP),
(60003, 6, 10069, 'system', CURRENT_TIMESTAMP),
(60004, 6, 10070, 'system', CURRENT_TIMESTAMP),
(60005, 6, 10071, 'system', CURRENT_TIMESTAMP),
(60006, 6, 10072, 'system', CURRENT_TIMESTAMP),
(60007, 6, 10073, 'system', CURRENT_TIMESTAMP),
(60008, 6, 10074, 'system', CURRENT_TIMESTAMP),
(60009, 6, 10075, 'system', CURRENT_TIMESTAMP),
(60010, 6, 10076, 'system', CURRENT_TIMESTAMP),
(60011, 6, 10077, 'system', CURRENT_TIMESTAMP),
(60012, 6, 10078, 'system', CURRENT_TIMESTAMP),
(60013, 6, 10079, 'system', CURRENT_TIMESTAMP),
(60014, 6, 10080, 'system', CURRENT_TIMESTAMP),
(60015, 6, 10083, 'system', CURRENT_TIMESTAMP),
(60016, 6, 10084, 'system', CURRENT_TIMESTAMP),
(60017, 6, 10085, 'system', CURRENT_TIMESTAMP);

-- CASHIER (role 7): operations billing/payment/insurance
INSERT INTO auth_role_permission (id, role_id, permission_id, created_by, created_time) VALUES
(70001, 7, 10052, 'system', CURRENT_TIMESTAMP),
(70002, 7, 10053, 'system', CURRENT_TIMESTAMP),
(70003, 7, 10054, 'system', CURRENT_TIMESTAMP),
(70004, 7, 10055, 'system', CURRENT_TIMESTAMP),
(70005, 7, 10056, 'system', CURRENT_TIMESTAMP),
(70006, 7, 10057, 'system', CURRENT_TIMESTAMP),
(70007, 7, 10058, 'system', CURRENT_TIMESTAMP),
(70008, 7, 10059, 'system', CURRENT_TIMESTAMP),
(70009, 7, 10060, 'system', CURRENT_TIMESTAMP),
(70010, 7, 10061, 'system', CURRENT_TIMESTAMP),
(70011, 7, 10062, 'system', CURRENT_TIMESTAMP),
(70012, 7, 10065, 'system', CURRENT_TIMESTAMP),
(70013, 7, 10066, 'system', CURRENT_TIMESTAMP);

-- REGISTRAR (role 8): registration + appointment + patient + schedule + department
INSERT INTO auth_role_permission (id, role_id, permission_id, created_by, created_time) VALUES
(80001, 8, 10011, 'system', CURRENT_TIMESTAMP),
(80002, 8, 10012, 'system', CURRENT_TIMESTAMP),
(80003, 8, 10013, 'system', CURRENT_TIMESTAMP),
(80004, 8, 10014, 'system', CURRENT_TIMESTAMP),
(80005, 8, 10015, 'system', CURRENT_TIMESTAMP),
(80006, 8, 10016, 'system', CURRENT_TIMESTAMP),
(80007, 8, 10017, 'system', CURRENT_TIMESTAMP),
(80008, 8, 10018, 'system', CURRENT_TIMESTAMP),
(80009, 8, 10019, 'system', CURRENT_TIMESTAMP),
(80010, 8, 10020, 'system', CURRENT_TIMESTAMP),
(80011, 8, 10022, 'system', CURRENT_TIMESTAMP),
(80012, 8, 10023, 'system', CURRENT_TIMESTAMP),
(80013, 8, 10024, 'system', CURRENT_TIMESTAMP),
(80014, 8, 10052, 'system', CURRENT_TIMESTAMP),
(80015, 8, 10056, 'system', CURRENT_TIMESTAMP);

-- MEDICAL_RECORD_CODER (role 9): DRG + audit + statistics
INSERT INTO auth_role_permission (id, role_id, permission_id, created_by, created_time) VALUES
(90001, 9, 10011, 'system', CURRENT_TIMESTAMP),
(90002, 9, 10014, 'system', CURRENT_TIMESTAMP),
(90003, 9, 10028, 'system', CURRENT_TIMESTAMP),
(90004, 9, 10052, 'system', CURRENT_TIMESTAMP),
(90005, 9, 10056, 'system', CURRENT_TIMESTAMP),
(90006, 9, 10066, 'system', CURRENT_TIMESTAMP),
(90007, 9, 10067, 'system', CURRENT_TIMESTAMP),
(90008, 9, 10093, 'system', CURRENT_TIMESTAMP),
(90009, 9, 10094, 'system', CURRENT_TIMESTAMP),
(90010, 9, 10095, 'system', CURRENT_TIMESTAMP),
(90011, 9, 10096, 'system', CURRENT_TIMESTAMP),
(90012, 9, 10097, 'system', CURRENT_TIMESTAMP),
(90013, 9, 10098, 'system', CURRENT_TIMESTAMP),
(90014, 9, 10099, 'system', CURRENT_TIMESTAMP),
(90015, 9, 10100, 'system', CURRENT_TIMESTAMP);
