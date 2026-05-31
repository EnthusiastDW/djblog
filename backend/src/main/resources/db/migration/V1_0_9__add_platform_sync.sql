-- 文章平台同步记录表
CREATE TABLE IF NOT EXISTS post_platform_sync (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    post_id         BIGINT NOT NULL COMMENT '文章ID',
    platform_code   VARCHAR(50) NOT NULL COMMENT '平台代码',
    sync_status     VARCHAR(20) DEFAULT 'PENDING' COMMENT '同步状态: PENDING/SYNCING/SUCCESS/FAILED',
    external_url    VARCHAR(500) COMMENT '外部平台文章URL',
    error_message   TEXT COMMENT '错误信息',
    synced_at       TIMESTAMP NULL COMMENT '同步完成时间',
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_post_platform (post_id, platform_code),
    INDEX idx_post_id (post_id),
    INDEX idx_platform_code (platform_code)
) COMMENT = '文章平台同步记录表';


-- 文章表增加已同步平台字段
ALTER TABLE post ADD COLUMN sync_platforms VARCHAR(200) DEFAULT NULL COMMENT '已同步的平台列表(逗号分隔，如"JUEJIN,CSDN")';
