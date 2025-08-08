-- provider : 인증 사이트 이름 / provider_id : 인증한 곳 id
ALTER TABLE user_account
  ADD COLUMN provider VARCHAR(20),
  ADD COLUMN provider_id VARCHAR(100);