-- 文章访问统计表
CREATE TABLE IF NOT EXISTS post_visit_statistics
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '访问统计 ID',
    post_id      BIGINT       NOT NULL COMMENT '文章 ID',
    visitor_id   VARCHAR(64)  NOT NULL COMMENT '访客设备指纹ID',
    visit_date   DATE         NOT NULL COMMENT '访问日期',
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_post_visitor_date (post_id, visitor_id, visit_date),
    INDEX idx_post_id (post_id),
    INDEX idx_visit_date (visit_date)
) COMMENT = '文章访问统计表';
