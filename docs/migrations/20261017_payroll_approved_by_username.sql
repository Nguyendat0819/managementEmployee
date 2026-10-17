-- approved_by stores the authenticated username as an audit snapshot, not an employee code.
ALTER TABLE hr_service.payroll_runs
    DROP CONSTRAINT IF EXISTS fk_payroll_runs_approved_by;
