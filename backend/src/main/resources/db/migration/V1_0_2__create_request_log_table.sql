-- 请求日志表
CREATE TABLE IF NOT EXISTS request_log
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    ip            VARCHAR(50) COMMENT '客户端IP地址',
    method        VARCHAR(10) NOT NULL COMMENT '请求方法',
    uri           VARCHAR(500) NOT NULL COMMENT '请求URI',
    params        TEXT COMMENT '请求参数',
    request_body  TEXT COMMENT '请求体（仅记录前2KB）',
    user_agent    VARCHAR(500) COMMENT '用户代理',
    status_code   INT COMMENT '响应状态码',
    error_message TEXT COMMENT '错误信息',
    duration      BIGINT COMMENT '请求耗时（毫秒）',
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_request_log_uri (uri),
    INDEX idx_request_log_method (method),
    INDEX idx_request_log_status_code (status_code),
    INDEX idx_request_log_created_at (created_at)
) COMMENT = '请求日志表';
