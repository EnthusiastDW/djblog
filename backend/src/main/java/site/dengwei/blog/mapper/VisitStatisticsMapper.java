package site.dengwei.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import site.dengwei.blog.entity.VisitStatistics;

/**
 * 访问统计 Mapper
 *
 * @author dengwei
 * @since 2026-04-03
 */
@Mapper
public interface VisitStatisticsMapper extends BaseMapper<VisitStatistics> {

    /**
     * 获取今日访问人数
     *
     * @return 今日访问人数
     */
    @Select("SELECT COUNT(*) FROM visit_statistics WHERE visit_date = CURDATE()")
    Long countTodayVisitors();

    /**
     * 获取总访问人数
     *
     * @return 总访问人数
     */
    @Select("SELECT COUNT(DISTINCT ip) FROM visit_statistics")
    Long countTotalVisitors();
}
