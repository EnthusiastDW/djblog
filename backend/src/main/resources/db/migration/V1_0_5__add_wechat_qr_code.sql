-- 添加微信二维码字段到用户表
ALTER TABLE db_user ADD COLUMN wechat_qr_code VARCHAR(500) COMMENT '微信二维码图片URL';
