-- 常用短语种子数据
-- 为临床病历编辑器提供常用短语模板

INSERT INTO cli_common_phrase (id, phrase_name, phrase_content, phrase_type, dept_id, user_id, sort_order, created_time, updated_time) VALUES
-- 主诉类短语
(1, '发热待查', '发热 3 天，体温最高 38.5°C', 'CHIEF_COMPLAINT', NULL, NULL, 1, NOW(), NOW()),
(2, '咳嗽咳痰', '咳嗽、咳痰 1 周', 'CHIEF_COMPLAINT', NULL, NULL, 2, NOW(), NOW()),
(3, '头痛头晕', '头痛、头晕 2 天', 'CHIEF_COMPLAINT', NULL, NULL, 3, NOW(), NOW()),
(4, '腹痛腹泻', '腹痛、腹泻 1 天', 'CHIEF_COMPLAINT', NULL, NULL, 4, NOW(), NOW()),
(5, '高血压复诊', '高血压复诊，规律服药中', 'CHIEF_COMPLAINT', NULL, NULL, 5, NOW(), NOW()),
(6, '糖尿病复诊', '糖尿病复诊，血糖监测中', 'CHIEF_COMPLAINT', NULL, NULL, 6, NOW(), NOW()),

-- 现病史类短语
(7, '上呼吸道感染', '患者诉咳嗽、咽痛，症状持续 3 天，无明显加重。伴少量白痰，无发热、胸痛。自服感冒药后症状稍有缓解。', 'PRESENT_ILLNESS', NULL, NULL, 1, NOW(), NOW()),
(8, '急性胃肠炎', '患者诉腹痛、腹泻 1 天，大便 5-6 次/日，稀水样便，伴恶心、纳差。无发热、呕吐。', 'PRESENT_ILLNESS', NULL, NULL, 2, NOW(), NOW()),
(9, '高血压随访', '患者高血压病史 5 年，规律服用降压药，自测血压较前平稳，控制在 130/80 mmHg 左右。无头晕、头痛、胸闷等不适。', 'PRESENT_ILLNESS', NULL, NULL, 3, NOW(), NOW()),
(10, '糖尿病随访', '患者糖尿病病史 3 年，规律使用降糖药物，近期空腹血糖波动在 6-8 mmol/L，餐后 2 小时血糖 8-10 mmol/L。无低血糖症状。', 'PRESENT_ILLNESS', NULL, NULL, 4, NOW(), NOW()),

-- 查体类短语
(11, '咽部充血', '咽部充血，双侧扁桃体无肿大，双肺呼吸音清，未闻及明显干湿性啰音。', 'PHYSICAL_EXAM', NULL, NULL, 1, NOW(), NOW()),
(12, '腹部压痛', '腹软，中上腹轻度压痛，无反跳痛，肝脾肋下未触及，肠鸣音活跃，4-5 次/分。', 'PHYSICAL_EXAM', NULL, NULL, 2, NOW(), NOW()),
(13, '一般情况可', '一般情况可，神清，精神可，心肺查体未见明显异常。腹软，无压痛。双下肢无水肿。', 'PHYSICAL_EXAM', NULL, NULL, 3, NOW(), NOW()),
(14, '生命体征平稳', 'T 36.5°C，P 78 次/分，R 18 次/分，BP 120/80 mmHg。神清，精神可，查体合作。', 'PHYSICAL_EXAM', NULL, NULL, 4, NOW(), NOW()),

-- 诊疗计划类短语
(15, '对症治疗', '对症治疗，充分休息，多饮水，必要时复诊。', 'TREATMENT_PLAN', NULL, NULL, 1, NOW(), NOW()),
(16, '继续当前方案', '继续当前治疗方案，监测血压/血糖，低盐低脂饮食，定期复诊。', 'TREATMENT_PLAN', NULL, NULL, 2, NOW(), NOW()),
(17, '完善检查', '完善血常规、生化等检查，根据结果调整治疗方案。', 'TREATMENT_PLAN', NULL, NULL, 3, NOW(), NOW()),
(18, '健康指导', '健康指导：清淡饮食，避免辛辣刺激，规律作息，如有不适及时就诊。', 'TREATMENT_PLAN', NULL, NULL, 4, NOW(), NOW());
