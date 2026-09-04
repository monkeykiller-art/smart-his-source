-- Seed data for his_pharma: CDSS alert configs, sample drug interactions, dose limits

-- CDSS alert configurations
INSERT INTO pha_cdss_alert_config (id, alert_type, alert_name, alert_level, is_enabled, description, sort_order, created_time, updated_time)
VALUES
(2400000000000000001, 'DRUG_INTERACTION', '药物相互作用检查', 'ERROR', 1, '检查处方中药物之间的相互作用', 1, NOW(), NOW()),
(2400000000000000002, 'DRUG_ALLERGY', '药物过敏禁忌检查', 'ERROR', 1, '检查患者过敏史与处方药物的禁忌', 2, NOW(), NOW()),
(2400000000000000003, 'DOSE_EXCEEDED', '剂量超限检查', 'WARNING', 1, '检查处方药物剂量是否超过最大限量', 3, NOW(), NOW()),
(2400000000000000004, 'DUPLICATE_DRUG', '重复用药检查', 'WARNING', 1, '检查是否存在相同药理作用的重复用药', 4, NOW(), NOW()),
(2400000000000000005, 'SKIN_TEST_REQUIRED', '皮试药品检查', 'ERROR', 1, '检查需要皮试的药品是否已有皮试结果', 5, NOW(), NOW()),
(2400000000000000006, 'ANTIBIOTIC_LEVEL', '抗菌药物分级检查', 'WARNING', 1, '检查医师抗菌药物处方权限级别', 6, NOW(), NOW()),
(2400000000000000007, 'DRUG_CONTRAINDICATION', '药物禁忌症检查', 'ERROR', 1, '检查患者诊断与药物禁忌症是否冲突', 7, NOW(), NOW()),
(2400000000000000008, 'SPECIAL_DRUG', '特殊药品管控检查', 'WARNING', 1, '检查麻醉/精神药品处方权限和限量', 8, NOW(), NOW());

-- Sample drug interactions
INSERT INTO pha_drug_interaction (id, drug_code_a, drug_code_b, interaction_level, interaction_desc, suggestion, is_active, created_time, updated_time)
VALUES
(2400000000000000011, 'YP004', 'YP002', 'ERROR', '青霉素类与头孢菌素类可能存在交叉过敏', '注意询问过敏史，必要时更换药物', 1, NOW(), NOW()),
(2400000000000000012, 'YP008', 'YP010', 'WARNING', '硝苯地平与阿托伐他汀合用可能增加他汀血药浓度', '监测肌痛症状，必要时调整他汀剂量', 1, NOW(), NOW()),
(2400000000000000013, 'YP009', 'YP003', 'WARNING', '二甲双胍与布洛芬合用可能增加乳酸酸中毒风险', '监测肾功能，短期使用布洛芬', 1, NOW(), NOW());

-- Sample dose limits
INSERT INTO pha_dose_limit (id, drug_code, patient_type, max_single_dose, max_single_unit, max_daily_dose, max_daily_unit, max_freq_per_day, description, is_active, created_time, updated_time)
VALUES
(2400000000000000021, 'YP001', 'ADULT', 1.0000, 'g', 3.0000, 'g', 4, '阿莫西林成人最大日剂量3g', 1, NOW(), NOW()),
(2400000000000000022, 'YP003', 'ADULT', 0.6000, 'g', 2.4000, 'g', 4, '布洛芬成人最大日剂量2.4g', 1, NOW(), NOW()),
(2400000000000000023, 'YP008', 'ADULT', 30.0000, 'mg', 60.0000, 'mg', 2, '硝苯地平控释片每日最大60mg', 1, NOW(), NOW()),
(2400000000000000024, 'YP009', 'ADULT', 1.0000, 'g', 2.5500, 'g', 3, '二甲双胍成人最大日剂量2.55g', 1, NOW(), NOW()),
(2400000000000000025, 'YP010', 'ADULT', 20.0000, 'mg', 80.0000, 'mg', 1, '阿托伐他汀最大日剂量80mg', 1, NOW(), NOW());

-- Sample drug contraindications
INSERT INTO pha_drug_contraindication (id, drug_code, contraindication_type, contraindication_name, severity_level, description, is_active, created_time, updated_time)
VALUES
(2400000000000000031, 'YP001', 'ALLERGY', '青霉素过敏', 'ERROR', '对青霉素类药物过敏者禁用', 1, NOW(), NOW()),
(2400000000000000032, 'YP002', 'ALLERGY', '头孢菌素过敏', 'ERROR', '对头孢菌素类药物过敏者禁用', 1, NOW(), NOW()),
(2400000000000000033, 'YP003', 'DISEASE', '消化性溃疡活动期', 'WARNING', '活动性消化性溃疡患者慎用布洛芬', 1, NOW(), NOW()),
(2400000000000000034, 'YP009', 'DISEASE', '严重肾功能不全', 'ERROR', 'eGFR<30的患者禁用二甲双胍', 1, NOW(), NOW()),
(2400000000000000035, 'YP008', 'DISEASE', '严重主动脉瓣狭窄', 'ERROR', '严重主动脉瓣狭窄患者禁用硝苯地平', 1, NOW(), NOW());
