package site.dengwei.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import site.dengwei.blog.entity.Comment;

/**
 * (Comment)表数据库访问层
 *
 * @author makejava
 * @since 2025-09-08 11:33:26
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

}