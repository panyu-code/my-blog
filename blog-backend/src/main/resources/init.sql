-- 创建数据库（如果不存在）
CREATE
DATABASE IF NOT EXISTS blog DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE
blog;

-- 用户表
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`
(
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username`        VARCHAR(50)  NOT NULL DEFAULT '' COMMENT '用户名',
    `password`        VARCHAR(255) NOT NULL DEFAULT '' COMMENT '密码（MD5加密）',
    `nickname`        VARCHAR(50)  NOT NULL DEFAULT '' COMMENT '昵称',
    `email`           VARCHAR(100) NOT NULL DEFAULT '' COMMENT '邮箱',
    `avatar`          VARCHAR(500) NOT NULL DEFAULT '' COMMENT '头像URL',
    `role`            INT          NOT NULL DEFAULT 0 COMMENT '角色：0-普通用户 1-编辑 2-管理员',
    `status`          TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    `last_login_time` DATETIME NULL DEFAULT NULL COMMENT '最后登录时间',
    `last_login_ip`   VARCHAR(50)  NOT NULL DEFAULT '' COMMENT '最后登录IP',
    `deleted`         TINYINT      NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除 1-已删除',
    `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`),
    KEY               `idx_role` (`role`),
    KEY               `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 分类表
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category`
(
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `name`        VARCHAR(50)  NOT NULL DEFAULT '' COMMENT '分类名称',
    `description` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '分类描述',
    `sort`        INT          NOT NULL DEFAULT 0 COMMENT '排序',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除 1-已删除',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`),
    KEY           `idx_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分类表';

-- 标签表
DROP TABLE IF EXISTS `tag`;
CREATE TABLE `tag`
(
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '标签ID',
    `name`        VARCHAR(50) NOT NULL DEFAULT '' COMMENT '标签名称',
    `category_id` BIGINT      NOT NULL DEFAULT 0 COMMENT '所属分类ID',
    `color`       VARCHAR(20) NOT NULL DEFAULT '#409EFF' COMMENT '标签颜色',
    `deleted`     TINYINT     NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除 1-已删除',
    `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`),
    KEY           `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签表';

-- 文章表
DROP TABLE IF EXISTS `article`;
CREATE TABLE `article`
(
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '文章ID',
    `title`         VARCHAR(200) NOT NULL DEFAULT '' COMMENT '文章标题',
    `summary`       VARCHAR(500) NOT NULL DEFAULT '' COMMENT '文章摘要',
    `content`       LONGTEXT     NOT NULL COMMENT '文章内容（Markdown）',
    `cover`         VARCHAR(500) NOT NULL DEFAULT '' COMMENT '封面图URL',
    `category_id`   BIGINT       NOT NULL DEFAULT 0 COMMENT '分类ID',
    `author_id`     BIGINT       NOT NULL DEFAULT 0 COMMENT '作者ID',
    `status`        TINYINT      NOT NULL DEFAULT 0 COMMENT '状态：0-草稿 1-已发布',
    `is_top`        TINYINT      NOT NULL DEFAULT 0 COMMENT '是否置顶：0-否 1-是',
    `view_count`    INT          NOT NULL DEFAULT 0 COMMENT '浏览量',
    `like_count`    INT          NOT NULL DEFAULT 0 COMMENT '点赞数',
    `comment_count` INT          NOT NULL DEFAULT 0 COMMENT '评论数',
    `deleted`       TINYINT      NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除 1-已删除',
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `publish_time`  DATETIME NULL DEFAULT NULL COMMENT '发布时间',
    PRIMARY KEY (`id`),
    KEY             `idx_category_id` (`category_id`),
    KEY             `idx_author_id` (`author_id`),
    KEY             `idx_status` (`status`),
    KEY             `idx_create_time` (`create_time`),
    KEY             `idx_publish_time` (`publish_time`),
    FULLTEXT KEY `ft_title_content` (`title`, `content`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章表';

-- 文章标签关联表
DROP TABLE IF EXISTS `article_tag`;
CREATE TABLE `article_tag`
(
    `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `article_id`  BIGINT   NOT NULL DEFAULT 0 COMMENT '文章ID',
    `tag_id`      BIGINT   NOT NULL DEFAULT 0 COMMENT '标签ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_article_tag` (`article_id`, `tag_id`),
    KEY           `idx_tag_id` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章标签关联表';

-- 评论表
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment`
(
    `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '评论ID',
    `article_id`  BIGINT   NOT NULL DEFAULT 0 COMMENT '文章ID',
    `user_id`     BIGINT   NOT NULL DEFAULT 0 COMMENT '用户ID',
    `parent_id`   BIGINT   NOT NULL DEFAULT 0 COMMENT '父评论ID，0表示一级评论',
    `reply_to_id` BIGINT   NOT NULL DEFAULT 0 COMMENT '回复的评论ID，0表示回复文章',
    `content`     TEXT     NOT NULL COMMENT '评论内容',
    `like_count`  INT      NOT NULL DEFAULT 0 COMMENT '点赞数',
    `status`      TINYINT  NOT NULL DEFAULT 1 COMMENT '状态：0-待审核 1-已通过 2-已拒绝',
    `deleted`     TINYINT  NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除 1-已删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY           `idx_article_id` (`article_id`),
    KEY           `idx_user_id` (`user_id`),
    KEY           `idx_parent_id` (`parent_id`),
    KEY           `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';

-- 文件上传表
DROP TABLE IF EXISTS `upload`;
CREATE TABLE `upload`
(
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '文件ID',
    `original_name` VARCHAR(255) NOT NULL DEFAULT '' COMMENT '原始文件名',
    `file_name`     VARCHAR(255) NOT NULL DEFAULT '' COMMENT '存储文件名',
    `file_path`     VARCHAR(500) NOT NULL DEFAULT '' COMMENT '文件路径',
    `file_url`      VARCHAR(500) NOT NULL DEFAULT '' COMMENT '文件URL',
    `file_type`     VARCHAR(50)  NOT NULL DEFAULT '' COMMENT '文件类型',
    `file_size`     BIGINT       NOT NULL DEFAULT 0 COMMENT '文件大小（字节）',
    `user_id`       BIGINT       NOT NULL DEFAULT 0 COMMENT '上传用户ID',
    `deleted`       TINYINT      NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除 1-已删除',
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY             `idx_user_id` (`user_id`),
    KEY             `idx_file_type` (`file_type`),
    KEY             `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件上传表';

-- 系统设置表
DROP TABLE IF EXISTS `settings`;
CREATE TABLE `settings`
(
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '设置ID',
    `key`         VARCHAR(100) NOT NULL DEFAULT '' COMMENT '设置键',
    `value`       TEXT         NOT NULL COMMENT '设置值',
    `description` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '描述',
    `type`        VARCHAR(20)  NOT NULL DEFAULT 'string' COMMENT '值类型：string/number/boolean/json',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_key` (`key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统设置表';

-- 插入默认管理员账号（密码：admin123，使用MD5加密后的值）
-- 注意：密码需要在应用层使用 MD5(密码+用户ID) 加密
INSERT INTO `user` (`username`, `password`, `nickname`, `email`, `role`, `status`)
VALUES ('admin', 'e10adc3949ba59abbe56e057f20f883e', '管理员', 'admin@example.com', 'admin', 1);

-- 插入默认分类
INSERT INTO `category` (`name`, `description`, `sort`)
VALUES ('技术', '技术相关文章', 1),
       ('生活', '生活随笔', 2),
       ('随笔', '个人随笔', 3);

-- 插入默认标签（关联到分类）
INSERT INTO `tag` (`name`, `category_id`, `color`)
VALUES ('Java', 1, '#E74C3C'),
       ('Spring', 1, '#27AE60'),
       ('Vue', 1, '#42B883'),
       ('MySQL', 1, '#3498DB'),
       ('前端', 1, '#E67E22'),
       ('生活感悟', 2, '#9B59B6'),
       ('旅行', 2, '#1ABC9C'),
       ('思考', 3, '#34495E');

-- 插入默认系统设置
INSERT INTO `settings` (`key`, `value`, `description`, `type`)
VALUES ('site_name', '我的博客', '网站名称', 'string'),
       ('site_description', '一个简洁的个人博客系统', '网站描述', 'string'),
       ('site_keywords', '博客,技术,分享', '网站关键词', 'string'),
       ('page_size', '10', '每页文章数', 'number'),
       ('comment_audit', 'false', '评论是否需要审核', 'boolean');
