ALTER TABLE hr_service.users
    ADD COLUMN IF NOT EXISTS full_name VARCHAR(200);

UPDATE hr_service.users
SET full_name = user_name
WHERE full_name IS NULL;
