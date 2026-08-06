CREATE TABLE IF NOT EXISTS users (
                       user_id BIGINT NOT NULL AUTO_INCREMENT,
                       email VARCHAR(100) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       nickname VARCHAR(10) NOT NULL UNIQUE,
                       profile_image VARCHAR(500) NOT NULL,
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP,
                       deleted_at TIMESTAMP NULL,
                       PRIMARY KEY (user_id)
);

CREATE TABLE IF NOT EXISTS posts (
                       post_id	BIGINT NOT NULL AUTO_INCREMENT,
                       user_id	BIGINT	NOT NULL,
                       title VARCHAR(26) NOT NULL,
                       content	MEDIUMTEXT NOT NULL,
                       content_image VARCHAR(500) NULL,
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP,
                       deleted_at TIMESTAMP NULL,
                       view_count INT NOT NULL DEFAULT 0,
                       category VARCHAR(20) NOT NULL,
                       PRIMARY KEY(post_id),
                       FOREIGN KEY(user_id) REFERENCES users(user_id)
);
CREATE INDEX IF NOT EXISTS idx_posts_created_at ON posts(created_at);

CREATE TABLE IF NOT EXISTS comments (
                          comment_id BIGINT NOT NULL AUTO_INCREMENT,
                          user_id BIGINT NOT NULL,
                          post_id BIGINT NOT NULL,
                          content TEXT NOT NULL,
                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP,
                          deleted_at TIMESTAMP NULL,
                          PRIMARY KEY(comment_id),
                          FOREIGN KEY(user_id) REFERENCES users(user_id),
                          FOREIGN KEY(post_id) REFERENCES posts(post_id)
);

CREATE TABLE IF NOT EXISTS likes (
                       like_id	BIGINT NOT NULL AUTO_INCREMENT,
                       user_id	BIGINT	NOT NULL,
                       post_id	BIGINT	NOT NULL,
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       PRIMARY KEY(like_id),
                       FOREIGN KEY(user_id) REFERENCES users(user_id),
                       FOREIGN KEY(post_id) REFERENCES posts(post_id),
                       UNIQUE(user_id, post_id)
);
CREATE TABLE IF NOT EXISTS refresh_tokens (
                    refresh_token_id BIGINT NOT NULL AUTO_INCREMENT,
                    token VARCHAR(1000) NOT NULL UNIQUE,
                    user_id BIGINT NOT NULL,
                    expires_at TIMESTAMP NOT NULL,
                    PRIMARY KEY (refresh_token_id),
                    FOREIGN KEY (user_id) REFERENCES users(user_id)
    );

CREATE TABLE IF NOT EXISTS tags (
                    tag_id BIGINT NOT NULL AUTO_INCREMENT,
                    name VARCHAR(20) NOT NULL UNIQUE,
                    PRIMARY KEY (tag_id)
    );

CREATE TABLE IF NOT EXISTS post_tags (
                     post_tag_id BIGINT NOT NULL AUTO_INCREMENT,
                     post_id BIGINT NOT NULL,
                     tag_id BIGINT NOT NULL,
                     PRIMARY KEY (post_tag_id),
                    CONSTRAINT unique_post_tag
                    UNIQUE (post_id, tag_id),
                    FOREIGN KEY (post_id)
                    REFERENCES posts(post_id),
                    FOREIGN KEY (tag_id)
                    REFERENCES tags(tag_id)
    );

CREATE INDEX IF NOT EXISTS idx_post_tags_post_id ON post_tags(post_id);

CREATE INDEX IF NOT EXISTS idx_post_tags_tag_id ON post_tags(tag_id);