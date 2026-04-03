-- Add contact_info column to user table
ALTER TABLE blog.db_user ADD COLUMN contact_info VARCHAR(500) DEFAULT NULL COMMENT '联系方式';
