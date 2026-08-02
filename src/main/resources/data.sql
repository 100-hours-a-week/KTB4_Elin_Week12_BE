INSERT INTO users ( email, password, nickname, profile_image, created_at, updated_at, deleted_at) VALUES ( 'elin@example.com', 'Test1234!', 'elin', 'https://image.kr/img1.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL);
INSERT INTO users ( email, password, nickname, profile_image, created_at, updated_at, deleted_at) VALUES ( 'abcd@example.com', 'Test5678!', 'abcd', 'https://image.kr/img2.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL);
INSERT INTO posts ( user_id, title, content, content_image, created_at, updated_at, deleted_at,view_count, category) VALUES ( 1, '제목1', '첫 게시글입니다.', 'https://image.kr/content1.jpg' , CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL, 0,'BACKEND');
INSERT INTO posts (user_id, title, content, content_image, created_at, updated_at, deleted_at, view_count, category) VALUES ( 2, '제목2', '두 번째 게시글입니다.', 'https://image.kr/content2.jpg' , CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL,  0,'BACKEND');
INSERT INTO comments ( user_id, post_id, content, created_at, updated_at, deleted_at) VALUES ( 1, 1, '첫 댓글입니다.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL);
INSERT INTO comments ( user_id, post_id, content, created_at, updated_at, deleted_at) VALUES ( 2,1,'첫 게시글의 두 번째 댓글입니다.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL);
INSERT INTO likes ( user_id, post_id) VALUES (1,1);
INSERT INTO likes (user_id, post_id) VALUES (2,1);
