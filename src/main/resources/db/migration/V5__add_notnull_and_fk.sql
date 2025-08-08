-- NOT NULL 제약 및 FK 생성
ALTER TABLE post
  MODIFY COLUMN user_id BIGINT NOT NULL,
  ADD CONSTRAINT fk_post_user
    FOREIGN KEY (user_id)
    REFERENCES user_account(id);
