-- user_id 컬럼 추가 (NULL 허용)
ALTER TABLE post
  ADD COLUMN user_id BIGINT;
