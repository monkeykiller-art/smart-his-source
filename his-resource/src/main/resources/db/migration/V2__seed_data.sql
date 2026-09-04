-- Seed data for his_resource: pharmacies, wards, beds, sample drugs

-- Pharmacies (药房)
INSERT INTO res_pharmacy (id, pharmacy_code, pharmacy_name, pharmacy_type, dept_id, location, pharmacy_status, created_time, updated_time)
VALUES
(2300000000000000001, 'YF001', '门诊西药房', 'OUTPATIENT', 1001, '门诊楼1楼', 'ACTIVE', NOW(), NOW()),
(2300000000000000002, 'YF002', '门诊中药房', 'OUTPATIENT', 1001, '门诊楼1楼', 'ACTIVE', NOW(), NOW()),
(2300000000000000003, 'YF003', '住院药房', 'INPATIENT', 1002, '住院楼1楼', 'ACTIVE', NOW(), NOW()),
(2300000000000000004, 'YF004', '中药库', 'STORAGE', 1002, '后勤楼2楼', 'ACTIVE', NOW(), NOW());

-- Wards (病区)
INSERT INTO res_ward (id, ward_code, ward_name, dept_id, ward_type, floor_location, bed_count, nurse_station, ward_status, created_time, updated_time)
VALUES
(2300000000000000011, 'BQ001', '内科一病区', 2001, 'GENERAL', '住院楼3楼', 20, '内科护士站', 'ACTIVE', NOW(), NOW()),
(2300000000000000012, 'BQ002', '内科二病区', 2001, 'GENERAL', '住院楼4楼', 18, '内科护士站', 'ACTIVE', NOW(), NOW()),
(2300000000000000013, 'BQ003', '外科一病区', 2002, 'GENERAL', '住院楼5楼', 22, '外科护士站', 'ACTIVE', NOW(), NOW()),
(2300000000000000014, 'BQ004', '外科二病区', 2002, 'GENERAL', '住院楼6楼', 20, '外科护士站', 'ACTIVE', NOW(), NOW()),
(2300000000000000015, 'BQ005', '妇产科病区', 2003, 'GENERAL', '住院楼7楼', 16, '妇产科护士站', 'ACTIVE', NOW(), NOW()),
(2300000000000000016, 'BQ006', '儿科病区', 2004, 'GENERAL', '住院楼8楼', 15, '儿科护士站', 'ACTIVE', NOW(), NOW()),
(2300000000000000017, 'BQ007', 'ICU', 2005, 'ICU', '住院楼2楼', 10, 'ICU护士站', 'ACTIVE', NOW(), NOW()),
(2300000000000000018, 'BQ008', '骨科病区', 2006, 'GENERAL', '住院楼9楼', 18, '骨科护士站', 'ACTIVE', NOW(), NOW());

-- Beds (床位) — sample for 内科一病区 (ward_id = 2300000000000000011)
INSERT INTO res_bed (id, bed_no, ward_id, room_no, bed_type, bed_rank, floor_no, bed_status, daily_fee, sort_order, created_time, updated_time)
VALUES
-- Room 301
(2300000000000000101, '301-1', 2300000000000000011, '301', 'NORMAL', 'THIRD_CLASS', '3', 'AVAILABLE', 40.0000, 1, NOW(), NOW()),
(2300000000000000102, '301-2', 2300000000000000011, '301', 'NORMAL', 'THIRD_CLASS', '3', 'AVAILABLE', 40.0000, 2, NOW(), NOW()),
(2300000000000000103, '301-3', 2300000000000000011, '301', 'NORMAL', 'THIRD_CLASS', '3', 'AVAILABLE', 40.0000, 3, NOW(), NOW()),
(2300000000000000104, '301-4', 2300000000000000011, '301', 'NORMAL', 'THIRD_CLASS', '3', 'AVAILABLE', 40.0000, 4, NOW(), NOW()),
-- Room 302
(2300000000000000105, '302-1', 2300000000000000011, '302', 'NORMAL', 'SECOND_CLASS', '3', 'AVAILABLE', 60.0000, 5, NOW(), NOW()),
(2300000000000000106, '302-2', 2300000000000000011, '302', 'NORMAL', 'SECOND_CLASS', '3', 'AVAILABLE', 60.0000, 6, NOW(), NOW()),
-- Room 303 (single)
(2300000000000000107, '303-1', 2300000000000000011, '303', 'NORMAL', 'FIRST_CLASS', '3', 'AVAILABLE', 100.0000, 7, NOW(), NOW()),
-- Room 304
(2300000000000000108, '304-1', 2300000000000000011, '304', 'NORMAL', 'THIRD_CLASS', '3', 'AVAILABLE', 40.0000, 8, NOW(), NOW()),
(2300000000000000109, '304-2', 2300000000000000011, '304', 'NORMAL', 'THIRD_CLASS', '3', 'AVAILABLE', 40.0000, 9, NOW(), NOW()),
(2300000000000000110, '304-3', 2300000000000000011, '304', 'NORMAL', 'THIRD_CLASS', '3', 'AVAILABLE', 40.0000, 10, NOW(), NOW()),
(2300000000000000111, '304-4', 2300000000000000011, '304', 'NORMAL', 'THIRD_CLASS', '3', 'AVAILABLE', 40.0000, 11, NOW(), NOW());

-- Sample drugs (药品目录)
INSERT INTO res_drug (id, drug_code, drug_name, name_pinyin, generic_name, dosage_form, spec, unit, drug_type, is_insurance, is_antibiotic, antibiotic_level, need_skin_test, drug_status, created_time, updated_time)
VALUES
(2300000000000000201, 'YP001', '阿莫西林胶囊', 'AMXSNJ', '阿莫西林', 'CAPSULE', '0.5g*24粒', '盒', 'WESTERN', 1, 1, 1, 0, 1, NOW(), NOW()),
(2300000000000000202, 'YP002', '头孢克洛胶囊', 'TBKLJN', '头孢克洛', 'CAPSULE', '0.25g*12粒', '盒', 'WESTERN', 1, 1, 2, 1, 1, NOW(), NOW()),
(2300000000000000203, 'YP003', '布洛芬缓释胶囊', 'BLFHJSJ', '布洛芬', 'CAPSULE', '0.3g*20粒', '盒', 'WESTERN', 1, 0, 0, 0, 1, NOW(), NOW()),
(2300000000000000204, 'YP004', '注射用青霉素钠', 'ZSYQMSNR', '青霉素', 'INJECTION', '80万U', '支', 'WESTERN', 1, 1, 1, 1, 1, NOW(), NOW()),
(2300000000000000205, 'YP005', '0.9%氯化钠注射液', '0.9LHNYZSY', '氯化钠', 'INJECTION', '250ml', '瓶', 'WESTERN', 1, 0, 0, 0, 1, NOW(), NOW()),
(2300000000000000206, 'YP006', '5%葡萄糖注射液', '5%PTPZSY', '葡萄糖', 'INJECTION', '250ml', '瓶', 'WESTERN', 1, 0, 0, 0, 1, NOW(), NOW()),
(2300000000000000207, 'YP007', '奥美拉唑肠溶胶囊', 'AMLZCRJN', '奥美拉唑', 'CAPSULE', '20mg*14粒', '盒', 'WESTERN', 1, 0, 0, 0, 1, NOW(), NOW()),
(2300000000000000208, 'YP008', '硝苯地平控释片', 'XBDPKSP', '硝苯地平', 'TABLET', '30mg*7片', '盒', 'WESTERN', 1, 0, 0, 0, 1, NOW(), NOW()),
(2300000000000000209, 'YP009', '二甲双胍片', 'EJSP', '二甲双胍', 'TABLET', '0.5g*30片', '盒', 'WESTERN', 1, 0, 0, 0, 1, NOW(), NOW()),
(2300000000000000210, 'YP010', '阿托伐他汀钙片', 'ATFTDGP', '阿托伐他汀', 'TABLET', '20mg*7片', '盒', 'WESTERN', 1, 0, 0, 0, 1, NOW(), NOW()),
(2300000000000000211, 'YP011', '维生素C片', 'WSSCP', '维生素C', 'TABLET', '0.1g*100片', '瓶', 'WESTERN', 1, 0, 0, 0, 1, NOW(), NOW()),
(2300000000000000212, 'YP012', '板蓝根颗粒', 'BLGKL', '板蓝根', 'GRANULE', '10g*20袋', '盒', 'TCM', 1, 0, 0, 0, 1, NOW(), NOW());

-- Suppliers (供应商)
INSERT INTO res_supplier (id, supplier_code, supplier_name, contact_person, contact_phone, supplier_type, supplier_status, created_time, updated_time)
VALUES
(2300000000000000301, 'GYS001', '国药控股医药有限公司', '张三', '13800001111', 'DRUG', 'ACTIVE', NOW(), NOW()),
(2300000000000000302, 'GYS002', '华润医药商业集团', '李四', '13800002222', 'DRUG', 'ACTIVE', NOW(), NOW()),
(2300000000000000303, 'GYS003', '九州通医药集团', '王五', '13800003333', 'DRUG', 'ACTIVE', NOW(), NOW());
