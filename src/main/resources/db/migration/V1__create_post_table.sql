CREATE TABLE post (
    id        BIGINT       AUTO_INCREMENT PRIMARY KEY,
    title     VARCHAR(255) NOT NULL,
    content   TEXT          NOT NULL,
    author    VARCHAR(100),
    created_at TIMESTAMP   DEFAULT CURRENT_TIMESTAMP
);
