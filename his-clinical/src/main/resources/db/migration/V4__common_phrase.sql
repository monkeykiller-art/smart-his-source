-- 常用短语表
CREATE TABLE IF NOT EXISTS cli_common_phrase (
    id BIGINT PRIMARY KEY,
    phrase_name VARCHAR(100) NOT NULL,
    phrase_content TEXT NOT NULL,
    phrase_type VARCHAR(20) NOT NULL,
    dept_id BIGINT,
    user_id BIGINT,
    sort_order INT DEFAULT 0,
    created_by BIGINT,
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE cli_common_phrase IS '常用短语表';
COMMENT ON COLUMN cli_common_phrase.id IS '主键ID';
COMMENT ON COLUMN cli_common_phrase.phrase_name IS '短语名称';
COMMENT ON COLUMN cli_common_phrase.phrase_content IS '短语内容';
COMMENT ON COLUMN cli_common_phrase.phrase_type IS '短语类型：CHIEF_COMPLAINT-主诉, PRESENT_ILLNESS-现病史, PHYSICAL_EXAM-查体, TREATMENT_PLAN-诊疗计划';
COMMENT ON COLUMN cli_common_phrase.dept_id IS '科室ID（科室级短语）';
COMMENT ON COLUMN cli_common_phrase.user_id IS '用户ID（个人短语）';
COMMENT ON COLUMN cli_common_phrase.sort_order IS '排序号';

CREATE INDEX idx_common_phrase_type ON cli_common_phrase(phrase_type);
CREATE INDEX idx_common_phrase_dept ON cli_common_phrase(dept_id);
CREATE INDEX idx_common_phrase_user ON cli_common_phrase(user_id);
