CREATE TABLE `image` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT COMMENT '이미지 고유 ID',
  `url` varchar(500) NOT NULL COMMENT '이미지 파일의 저장 경로 혹은 URL',
  `user_id` bigint,
  `created_at` datetime DEFAULT (now()) COMMENT '이미지 등록 시각',
  `modified_at` datetime DEFAULT (now()) COMMENT '이미지 수정 시각'
);

CREATE TABLE `user` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `login_id` varchar(12) UNIQUE NOT NULL COMMENT '회원 아이디',
  `password` varchar(255) NOT NULL COMMENT '사용자 비밀번호(영문/숫자 조합 8~12자)',
  `nickname` varchar(100) UNIQUE NOT NULL COMMENT '회원 닉네임 (최대 10자)',
  `name` varchar(100) NOT NULL COMMENT '실명 또는 이름',
  `birthdate` date NOT NULL COMMENT '생년월일',
  `email` varchar(255) UNIQUE NOT NULL COMMENT '이메일',
  `profile_image_id` bigint COMMENT '프로필 사진 URL (선택사항)',
  `social` varchar(50) DEFAULT null COMMENT 'SNS 제공자 (예: ''KAKAO'', ''GOOGLE''); 일반 가입 시에는 NULL',
  `social_user_id` varchar(255) DEFAULT null COMMENT 'SNS에서 제공하는 사용자 고유 식별자; 일반 가입 시에는 NULL',
  `social_link` varchar(255) DEFAULT null COMMENT 'SNS 계정의 연동 URL 또는 식별자; 일반 가입 시에는 NULL',
  `created_at` datetime DEFAULT (now()) COMMENT '회원 가입 시각',
  `modified_at` datetime DEFAULT (now()) COMMENT '회원 정보 수정 시각'
);

CREATE TABLE `terms` (
  `id` int PRIMARY KEY COMMENT '약관 고유 아이디',
  `type` ENUM ('TERMS_OF_USE', 'PRIVACY_POLICY') NOT NULL COMMENT '약관 종류 (이용약관, 개인정보처리방침 등)',
  `required` boolean NOT NULL DEFAULT false,
  `created_at` datetime DEFAULT (now()) COMMENT '약관 등록 시각',
  `modified_at` datetime DEFAULT (now()) COMMENT '약관 수정 시각'
);

CREATE TABLE `user_terms` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT COMMENT '회원 약관 동의 고유 아이디',
  `user_id` bigint COMMENT '회원 아이디',
  `term_id` int COMMENT '약관 아이디',
  `created_at` datetime DEFAULT (now()) COMMENT '동의 등록 시각',
  `modified_at` datetime DEFAULT (now()) COMMENT '동의 수정 시각'
);

CREATE TABLE `post` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT COMMENT '게시물 고유 ID',
  `user_id` bigint NOT NULL COMMENT '게시물을 작성한 회원의 아이디',
  `category` varchar(50) NOT NULL COMMENT '게시물 카테고리 (예: 카페, 편의점, 음식점, 기타)',
  `title` varchar(100) NOT NULL COMMENT '게시물 제목 (최대 100자)',
  `content` text NOT NULL COMMENT '게시물 내용',
  `image_ids` json COMMENT '첨부 이미지 URL 목록 등 (JSON 형식)',
  `view_count` int DEFAULT 0 COMMENT '게시물 조회수',
  `like_count` int DEFAULT 0 COMMENT '게시물 좋아요 수',
  `bookmark_count` int DEFAULT 0 COMMENT '게시물 북마크 수',
  `created_at` datetime DEFAULT (now()) COMMENT '게시물 작성 시각',
  `modified_at` datetime DEFAULT (now()) COMMENT '게시물 수정 시각'
);

CREATE TABLE `post_hashtag` (
  `post_id` bigint NOT NULL COMMENT '해당 태그가 적용된 게시물 아이디',
  `hashtag` varchar(50) NOT NULL COMMENT '게시물에 달린 태그 (최대 50자)',
  `created_at` datetime DEFAULT (now()) COMMENT '태그 등록 시각',
  `modified_at` datetime DEFAULT (now()) COMMENT '태그 수정 시각'
);

CREATE TABLE `post_like` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT COMMENT '좋아요 고유 ID',
  `post_id` bigint NOT NULL COMMENT '좋아요 대상 게시물 ID',
  `user_id` bigint NOT NULL COMMENT '좋아요 누른 회원 ID',
  `created_at` datetime DEFAULT (now()) COMMENT '좋아요 등록 시각'
);

CREATE TABLE `user_bookmark` (
  `user_id` bigint COMMENT '북마크를 등록한 회원의 아이디',
  `post_id` bigint COMMENT '북마크한 게시물의 아이디',
  `created_at` datetime DEFAULT (now()) COMMENT '북마크 등록 시각',
  `modified_at` datetime DEFAULT (now()) COMMENT '북마크 수정 시각',
  PRIMARY KEY (`user_id`, `post_id`)
);

CREATE TABLE `review` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT COMMENT '리뷰 고유 ID',
  `user_id` bigint NOT NULL COMMENT '리뷰를 작성한 회원의 아이디',
  `post_id` bigint NOT NULL COMMENT '리뷰 대상 게시물의 아이디',
  `content` text NOT NULL COMMENT '리뷰 내용',
  `rate` decimal(3,1) NOT NULL COMMENT '게시물에 대한 총 평점',
  `created_at` datetime DEFAULT (now()) COMMENT '리뷰 작성 시각',
  `modified_at` datetime DEFAULT (now()) COMMENT '리뷰 수정 시각'
);

CREATE TABLE `action_log` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT COMMENT '행동 로그 고유 ID',
  `post_id` bigint NOT NULL COMMENT '행동 대상 게시물 ID',
  `user_id` bigint COMMENT '행동 수행 회원 ID (익명인 경우 NULL 가능)',
  `action_type` varchar(20) NOT NULL COMMENT '행동 종류 (VIEW, LIKE, BOOKMARK 등)',
  `action_time` datetime DEFAULT (now()) COMMENT '행동 발생 시각'
);

CREATE TABLE `tag_stats` (
  `tag` varchar(50) PRIMARY KEY COMMENT 'post_hashtag.hashtag와 1:1 매핑된 태그 이름',
  `use_count` bigint NOT NULL DEFAULT 0 COMMENT '최근 X일간 post_hashtag 사용 횟수',
  `updated_at` datetime NOT NULL DEFAULT (now()) COMMENT 'use_count 최종 갱신 시각'
);

CREATE TABLE `tag_search_log` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT COMMENT '로그 고유 ID',
  `tag` varchar(50) NOT NULL COMMENT '사용자가 입력한 prefix',
  `searched_at` datetime NOT NULL DEFAULT (now()) COMMENT '자동완성 API 호출 시각',
  `user_id` bigint COMMENT '요청자 회원 ID'
);

CREATE INDEX `post_index_0` ON `post` (`title`);

CREATE INDEX `post_index_1` ON `post` (`created_at`);

CREATE UNIQUE INDEX `post_hashtag_index_2` ON `post_hashtag` (`post_id`, `hashtag`);

CREATE INDEX `post_hashtag_index_3` ON `post_hashtag` (`hashtag`);

CREATE UNIQUE INDEX `post_like_index_4` ON `post_like` (`post_id`, `user_id`);

CREATE INDEX `tag_stats_index_5` ON `tag_stats` (`use_count`);

CREATE INDEX `tag_search_log_index_6` ON `tag_search_log` (`tag`, `searched_at`);

ALTER TABLE `image` COMMENT = '프로필 사진이나 게시물 이미지 등을 저장할 때 참조';

ALTER TABLE `user` COMMENT = '회원 가입 시 필요한 추가 정보(이름, 생년월일, 휴대폰 번호, 동의 항목 등)를 포함한 회원 기본 정보 테이블';

ALTER TABLE `terms` COMMENT = '서비스 이용 약관 및 개인정보 처리방침 정보 저장';

ALTER TABLE `user_terms` COMMENT = '회원이 약관에 동의한 내역 저장';

ALTER TABLE `post` COMMENT = '회원이 작성한 게시물 정보 및 통계 데이터 저장';

ALTER TABLE `post_hashtag` COMMENT = '게시물 태그 정보를 관리하여 검색 및 필터링 활용';

ALTER TABLE `post_like` COMMENT = '회원이 게시물에 남긴 좋아요 이력';

ALTER TABLE `user_bookmark` COMMENT = '회원이 북마크한 게시물 정보 저장';

ALTER TABLE `review` COMMENT = '회원이 게시물에 남긴 리뷰 및 평점 정보 저장';

ALTER TABLE `action_log` COMMENT = '모든 게시물 관련 행동 로그를 단일 테이블에 기록함. 초기에는 파티셔닝 없이 관리하며, 데이터량 증가 시 개선할 수 있음.';

ALTER TABLE `tag_stats` COMMENT = 'post_hashtag 테이블을 주기 배치로 집계해 저장';

ALTER TABLE `tag_search_log` COMMENT = '프론트가 자동완성 입력 시 prefix를 INSERT';

ALTER TABLE `image` ADD FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);

ALTER TABLE `user` ADD FOREIGN KEY (`profile_image_id`) REFERENCES `image` (`id`);

ALTER TABLE `user_terms` ADD FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);

ALTER TABLE `user_terms` ADD FOREIGN KEY (`term_id`) REFERENCES `terms` (`id`);

ALTER TABLE `post` ADD FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);

ALTER TABLE `post_hashtag` ADD FOREIGN KEY (`post_id`) REFERENCES `post` (`id`);

ALTER TABLE `post_like` ADD FOREIGN KEY (`post_id`) REFERENCES `post` (`id`);

ALTER TABLE `post_like` ADD FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);

ALTER TABLE `user_bookmark` ADD FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);

ALTER TABLE `user_bookmark` ADD FOREIGN KEY (`post_id`) REFERENCES `post` (`id`);

ALTER TABLE `review` ADD FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);

ALTER TABLE `review` ADD FOREIGN KEY (`post_id`) REFERENCES `post` (`id`);

ALTER TABLE `action_log` ADD FOREIGN KEY (`post_id`) REFERENCES `post` (`id`);

ALTER TABLE `action_log` ADD FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);

ALTER TABLE `tag_search_log` ADD FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);
