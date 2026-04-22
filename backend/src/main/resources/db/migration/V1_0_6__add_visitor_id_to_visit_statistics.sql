-- 为访问统计表添加 visitor_id 字段（设备指纹ID）
ALTER TABLE visit_statistics 
ADD COLUMN visitor_id VARCHAR(64) COMMENT '访客设备指纹ID' AFTER id;

-- 创建新的唯一索引（visitor_id + visit_date）
ALTER TABLE visit_statistics 
DROP INDEX uk_ip_date,
ADD UNIQUE KEY uk_visitor_date (visitor_id, visit_date);

-- 保留 IP 字段的普通索引用于辅助分析
CREATE INDEX idx_ip ON visit_statistics(ip);

-- 历史数据兼容：将现有记录的 visitor_id 设置为 ip（临时方案）
UPDATE visit_statistics SET visitor_id = ip WHERE visitor_id IS NULL;
