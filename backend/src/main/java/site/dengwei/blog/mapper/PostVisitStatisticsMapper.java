package site.dengwei.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import site.dengwei.blog.entity.PostVisitStatistics;

import java.util.Map;

/**
 * 文章访问统计 Mapper
 *
 * @author dengwei
 * @since 2026-04-30
 */
@Mapper
public interface PostVisitStatisticsMapper extends BaseMapper<PostVisitStatistics> {

    /**
     * 批量获取文章的访问量
     *
     * @return Map<postId, viewCount>
     */
    @Select("SELECT post_id, COUNT(DISTINCT visitor_id) as view_count " +
            "FROM post_visit_statistics " +
            "GROUP BY post_id")
    java.util.List<Map<String, Object>> batchCountPostVisitors();
}
