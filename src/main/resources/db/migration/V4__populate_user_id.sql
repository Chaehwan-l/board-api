-- author -> user_id 매핑
UPDATE post p
JOIN user_account u
  ON p.author = u.username
SET p.user_id = u.id;
