ALTER TABLE hr_service.users
    ADD COLUMN IF NOT EXISTS auth_state_changed_at TIMESTAMPTZ;
