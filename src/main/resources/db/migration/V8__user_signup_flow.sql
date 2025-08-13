-- 1) email 컬럼 없으면 추가 (NULL로)
SET @has_email := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user_account' AND COLUMN_NAME = 'email'
);
SET @sql := IF(@has_email = 0,
  'ALTER TABLE user_account ADD COLUMN email VARCHAR(255) NULL AFTER username',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 2) signup_completed 컬럼 없으면 추가
SET @has_sc := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user_account' AND COLUMN_NAME = 'signup_completed'
);
SET @sql := IF(@has_sc = 0,
  'ALTER TABLE user_account ADD COLUMN signup_completed TINYINT(1) NOT NULL DEFAULT 0 AFTER provider_id',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 3) 이메일 백필(공백/NULL → 유일값)
UPDATE user_account
SET email = CASE
  WHEN email IS NULL OR email = '' THEN CONCAT(username, '+', id, '@placeholder.local')
  ELSE email
END;

-- 4) email NOT NULL 보장
ALTER TABLE user_account MODIFY email VARCHAR(255) NOT NULL;

-- 5) 기존 비밀번호 있던 계정은 완료 처리
UPDATE user_account
SET signup_completed = 1
WHERE password IS NOT NULL AND password <> '';

-- 6) ux_user_email 인덱스가 없으면 생성
SET @has_idx_email := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user_account' AND INDEX_NAME = 'ux_user_email'
);
SET @sql := IF(@has_idx_email = 0,
  'CREATE UNIQUE INDEX ux_user_email ON user_account(email)',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 7) ux_user_provider 인덱스가 없으면 생성
SET @has_idx_provider := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user_account' AND INDEX_NAME = 'ux_user_provider'
);
SET @sql := IF(@has_idx_provider = 0,
  'CREATE UNIQUE INDEX ux_user_provider ON user_account(provider, provider_id)',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
