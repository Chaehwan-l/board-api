-- 관리자 계정 생성
INSERT INTO user_account (
  username, email, password, role, signup_completed, provider, provider_id, created_at
)
SELECT
  '${admin_user}', '${admin_email}', '${admin_pw_hash}', 'ADMIN', 1, NULL, NULL, NOW()
WHERE NOT EXISTS (
  SELECT 1 FROM user_account
  WHERE username='${admin_user}' OR email='${admin_email}'
);
