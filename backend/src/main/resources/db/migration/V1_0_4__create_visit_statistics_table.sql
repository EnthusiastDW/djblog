-- 访问统计表
CREATE TABLE IF NOT EXISTS visit_statistics
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '访问统计 ID',
    ip           VARCHAR(50)  NOT NULL COMMENT '客户端 IP 地址',
    visit_date   DATE         NOT NULL COMMENT '访问日期',
    user_agent   VARCHAR(500) COMMENT '用户代理',
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_ip_date (ip, visit_date),
    INDEX idx_visit_date (visit_date)
) COMMENT = '访问统计表';
