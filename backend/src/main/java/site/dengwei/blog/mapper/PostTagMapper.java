package site.dengwei.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import site.dengwei.blog.entity.Post;
import site.dengwei.blog.entity.PostTag;

import java.util.List;

/**
 * 文章-标签关联表数据库访问层
 *
 * @author dengwei
 * @since 2025-09-10
 */
@Mapper
public interface PostTagMapper extends BaseMapper<PostTag> {

    /**
     * 根据标签ID查询关联的文章列表
     * @param tagId 标签ID
     * @return 文章列表
     */
    @Select("SELECT p.* FROM post p JOIN post_tag pt ON p.id = pt.post_id WHERE pt.tag_id = #{tagId} AND p.status = 'PUBLISHED'")
    List<Post> selectPostsByTagId(@Param("tagId") Long tagId);

    /**
     * 根据文章 ID 删除所有关联的标签
     *
     * @param postId 文章 ID
     */
    @Delete("DELETE FROM post_tag WHERE post_id = #{postId}")
    void deleteByPostId(@Param("postId") Long postId);
}
