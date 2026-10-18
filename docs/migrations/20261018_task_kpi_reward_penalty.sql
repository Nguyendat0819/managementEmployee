ALTER TABLE hr_service.employee_tasks
    ADD COLUMN IF NOT EXISTS kpi_threshold_percent INTEGER NOT NULL DEFAULT 80,
    ADD COLUMN IF NOT EXISTS penalty_amount NUMERIC(15, 2) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS completion_percent INTEGER;

ALTER TABLE hr_service.employee_tasks
    ADD CONSTRAINT employee_tasks_kpi_threshold_percent_check
        CHECK (kpi_threshold_percent BETWEEN 0 AND 100),
    ADD CONSTRAINT employee_tasks_completion_percent_check
        CHECK (completion_percent IS NULL OR completion_percent BETWEEN 0 AND 100),
    ADD CONSTRAINT employee_tasks_penalty_amount_check
        CHECK (penalty_amount >= 0);
