-- Generate a rolling set of outpatient schedules from the seeded weekly templates.
-- This keeps a fresh local database immediately usable by the registration window.
INSERT INTO pat_schedule (
    id, dept_id, doctor_id, schedule_date, time_period, start_time, end_time,
    total_quota, used_quota, reg_fee, reg_level, schedule_status, revision,
    created_by, created_time, deleted
)
SELECT
    2000000000000000 + ROW_NUMBER() OVER (ORDER BY days.schedule_date, tpl.id),
    tpl.dept_id,
    tpl.doctor_id,
    days.schedule_date::date,
    tpl.time_period,
    tpl.start_time,
    tpl.end_time,
    tpl.total_quota,
    0,
    tpl.reg_fee,
    tpl.reg_level,
    'ACTIVE',
    0,
    'system',
    CURRENT_TIMESTAMP,
    0
FROM generate_series(CURRENT_DATE, CURRENT_DATE + 90, INTERVAL '1 day') AS days(schedule_date)
JOIN pat_schedule_template tpl
  ON tpl.day_of_week = EXTRACT(ISODOW FROM days.schedule_date)::int
 AND tpl.template_status = 'ACTIVE'
WHERE NOT EXISTS (
    SELECT 1
    FROM pat_schedule existing
    WHERE existing.doctor_id = tpl.doctor_id
      AND existing.schedule_date = days.schedule_date::date
      AND existing.time_period = tpl.time_period
      AND existing.deleted = 0
);
