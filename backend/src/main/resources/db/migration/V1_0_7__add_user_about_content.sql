-- 为User表添加about_content字段，用于存储"关于我"页面的Markdown内容
ALTER TABLE blog.db_user ADD COLUMN about_content TEXT COMMENT '关于我（Markdown格式）';
