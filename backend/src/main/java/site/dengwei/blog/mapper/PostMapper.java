package site.dengwei.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import site.dengwei.blog.entity.Post;
import site.dengwei.blog.entity.Tag;

import java.util.List;
import java.util.Map;

/**
 * (Post)表数据库访问层
 *
 * @author makejava
 * @since 2025-09-08 11:33:25
 */
@Mapper
public interface PostMapper extends BaseMapper<Post> {


    /**
     * 根据标签 ID 查询文章 ID 列表
     * @param tagId 标签 ID
     * @return 文章 ID 列表
     */
    @Select("SELECT post_id FROM post_tag WHERE tag_id = #{tagId}")
    List<Long> selectPostIdsByTagId(@Param("tagId") Long tagId);

    /**
     * 根据文章ID查询关联的标签列表
     * @param postId 文章ID
     * @return 标签列表
     */
    @Select("SELECT t.* FROM tag t JOIN post_tag pt ON t.id = pt.tag_id WHERE pt.post_id = #{postId}")
    List<Tag> selectTagsByPostId(@Param("postId") Long postId);

    /**
     * 查询文章归档（按年月分组）
     * @return 归档列表
     */
    @Select("SELECT DATE_FORMAT(published_at, '%Y-%m') as archiveMonth, COUNT(*) as count " +
            "FROM post WHERE status = 'PUBLISHED' GROUP BY archiveMonth ORDER BY archiveMonth DESC")
    List<Map<String, Object>> selectPostArchives();

    /**
     * 根据年月查询文章列表
     * @param yearMonth 年月格式：yyyy-MM
     * @param page 分页对象
     * @return 文章列表
     */
    @Select("SELECT * FROM post WHERE status = 'PUBLISHED' AND DATE_FORMAT(published_at, '%Y-%m') = #{yearMonth} ORDER BY published_at DESC")
    Page<Post> selectPostsByArchive(@Param("yearMonth") String yearMonth, Page<Post> page);

    @Select("SELECT COALESCE(SUM(view_count), 0) FROM post WHERE status = 'PUBLISHED'")
    Long selectTotalViewCount();

    @Select("SELECT COUNT(*) FROM post WHERE category_id = #{categoryId} AND status = 'PUBLISHED'")
    Long countByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * 根据分类 ID 列表查询已发布文章数量（用于子分类文章数汇总）
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM post WHERE status = 'PUBLISHED' AND category_id IN " +
            "<foreach collection='categoryIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    Long countByCategoryIds(@Param("categoryIds") List<Long> categoryIds);

    @Select("SELECT COUNT(*) FROM post_tag WHERE tag_id = #{tagId}")
    Long countByTagId(@Param("tagId") Long tagId);

    /**
     * 查询热门文章（按浏览量降序）
     * @param limit 数量限制
     * @return 热门文章列表
     */
    @Select("SELECT * FROM post WHERE status = 'PUBLISHED' ORDER BY view_count DESC LIMIT #{limit}")
    List<Post> selectPopularPosts(@Param("limit") int limit);

    /**
     * 批量更新文章浏览量
     * @param postId 文章ID
     * @param viewCount 浏览量
     */
    @org.apache.ibatis.annotations.Update("UPDATE post SET view_count = #{viewCount} WHERE id = #{postId}")
    void updateViewCount(@Param("postId") Long postId, @Param("viewCount") Long viewCount);

    /**
     * 递增文章浏览量（原子操作）
     * @param postId 文章ID
     */
    @org.apache.ibatis.annotations.Update("UPDATE post SET view_count = view_count + 1 WHERE id = #{postId}")
    void incrementViewCount(@Param("postId") Long postId);
}