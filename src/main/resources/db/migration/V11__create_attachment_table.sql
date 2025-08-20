CREATE TABLE attachment (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  post_id BIGINT NULL,
  s3_key VARCHAR(512) NOT NULL,
  original_name VARCHAR(255) NOT NULL,
  content_type VARCHAR(100),
  size BIGINT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_attachment_post FOREIGN KEY (post_id) REFERENCES post(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE INDEX ix_attachment_post ON attachment(post_id);
