ALTER TABLE hr_service.users
    ADD COLUMN IF NOT EXISTS password_hash VARCHAR(100);

-- Tài khoản hiện có phải đặt lại mật khẩu trước khi đăng nhập nội bộ.
-- Sau khi hoàn tất, có thể áp ràng buộc NOT NULL cho password_hash.
