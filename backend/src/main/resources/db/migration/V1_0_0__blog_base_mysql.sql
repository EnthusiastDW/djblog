-- 用户表
CREATE TABLE db_user
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username   VARCHAR(50) UNIQUE  NOT NULL COMMENT '用户名',
    email      VARCHAR(100) UNIQUE NOT NULL COMMENT '邮箱',
    password   VARCHAR(255)        NOT NULL COMMENT '密码',
    avatar_url VARCHAR(255) COMMENT '头像地址',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_username (username)
) COMMENT = '用户表';

-- 分类表
CREATE TABLE category
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '分类ID',
    name        VARCHAR(50)         NOT NULL COMMENT '分类名称',
    slug        VARCHAR(100) UNIQUE NOT NULL COMMENT '分类Slug 由名称生成',
    description TEXT COMMENT '分类描述',
    parent_id   BIGINT COMMENT '父级分类ID',
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_category_slug (slug)
) COMMENT = '分类表';

-- 文章表
CREATE TABLE post
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '文章ID',
    title        VARCHAR(255) NOT NULL COMMENT '文章标题',
    content      TEXT         NOT NULL COMMENT '文章内容',
    slug         VARCHAR(255) UNIQUE COMMENT '文章Slug 由标题生成',
    cover_image  VARCHAR(255) COMMENT '封面图片地址',
    status       VARCHAR(20) DEFAULT 'DRAFT' COMMENT '文章状态',
    author_id    BIGINT COMMENT '作者ID',
    category_id  BIGINT COMMENT '分类ID',
    published_at TIMESTAMP COMMENT '发布时间',
    created_at   TIMESTAMP   DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at   TIMESTAMP   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_post_status (status),
    INDEX idx_post_author (author_id),
    INDEX idx_post_category (category_id),
    INDEX idx_post_published_at (published_at)
) COMMENT = '文章表';

-- 标签表
CREATE TABLE tag
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '标签ID',
    name       VARCHAR(50)         NOT NULL COMMENT '标签名称',
    slug       VARCHAR(100) UNIQUE NOT NULL COMMENT '标签Slug 由名称生成',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_tag_slug (slug)
) COMMENT = '标签表';

-- 文章-标签关联表
CREATE TABLE post_tag
(
    post_id BIGINT NOT NULL COMMENT '文章ID',
    tag_id  BIGINT NOT NULL COMMENT '标签ID',
    PRIMARY KEY (post_id, tag_id)
) COMMENT = '文章-标签关联表';

-- 评论表
CREATE TABLE comment
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '评论ID',
    content      TEXT NOT NULL COMMENT '评论内容',
    author_name  VARCHAR(100) COMMENT '作者名称',
    author_email VARCHAR(100) COMMENT '作者邮箱',
    author_url   VARCHAR(255) COMMENT '作者网址',
    user_id      BIGINT COMMENT '用户ID',
    post_id      BIGINT NOT NULL COMMENT '文章ID',
    parent_id    BIGINT COMMENT '父评论ID',
    status       VARCHAR(20) DEFAULT 'PENDING' COMMENT '评论状态',
    created_at   TIMESTAMP   DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at   TIMESTAMP   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_comment_post (post_id),
    INDEX idx_comment_status (status)
) COMMENT = '评论表';
