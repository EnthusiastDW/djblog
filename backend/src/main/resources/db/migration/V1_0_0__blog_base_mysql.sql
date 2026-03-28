-- 用户表
CREATE TABLE IF NOT EXISTS db_user
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username   VARCHAR(50) UNIQUE  NOT NULL COMMENT '用户名',
    email      VARCHAR(100) UNIQUE NOT NULL COMMENT '邮箱',
    password   VARCHAR(255)        NOT NULL COMMENT '密码',
    avatar_url VARCHAR(255) COMMENT '头像地址',
    bio        TEXT COMMENT '个人简介',
    role       VARCHAR(20) DEFAULT 'USER' COMMENT '角色',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_username (username)
) COMMENT = '用户表';

-- 分类表
CREATE TABLE IF NOT EXISTS category
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '分类 ID',
    name        VARCHAR(50)         NOT NULL COMMENT '分类名称',
    description TEXT COMMENT '分类描述',
    parent_id   BIGINT COMMENT '父级分类 ID',
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT = '分类表';

-- 文章表
CREATE TABLE IF NOT EXISTS post
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '文章 ID',
    title        VARCHAR(255) NOT NULL COMMENT '文章标题',
    content      TEXT         NOT NULL COMMENT '文章内容',
    slug         VARCHAR(255) UNIQUE COMMENT '文章 Slug 由标题生成',
    summary      VARCHAR(500) COMMENT '文章摘要',
    cover_image  VARCHAR(255) COMMENT '封面图片地址',
    status       VARCHAR(20) DEFAULT 'DRAFT' COMMENT '文章状态',
    author_id    BIGINT COMMENT '作者 ID',
    category_id  BIGINT COMMENT '分类 ID',
    view_count   BIGINT DEFAULT 0 COMMENT '浏览次数',
    published_at TIMESTAMP COMMENT '发布时间',
    created_at   TIMESTAMP   DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at   TIMESTAMP   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_post_status (status),
    INDEX idx_post_author (author_id),
    INDEX idx_post_category (category_id),
    INDEX idx_post_published_at (published_at)
) COMMENT = '文章表';

-- 标签表
CREATE TABLE IF NOT EXISTS tag
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '标签 ID',
    name       VARCHAR(50) NOT NULL COMMENT '标签名称',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) COMMENT = '标签表';

-- 文章-标签关联表
CREATE TABLE IF NOT EXISTS post_tag
(
    post_id BIGINT NOT NULL COMMENT '文章ID',
    tag_id  BIGINT NOT NULL COMMENT '标签ID',
    PRIMARY KEY (post_id, tag_id)
) COMMENT = '文章-标签关联表';

-- 评论表
CREATE TABLE IF NOT EXISTS comment
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