package site.dengwei.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import site.dengwei.blog.entity.PostVisitStatistics;

/**
 * 文章访问统计服务接口
 *
 * @author dengwei
 * @since 2026-04-30
 */
public interface PostVisitStatisticsService extends IService<PostVisitStatistics> {

    /**
     * 记录文章访问（使用设备指纹去重）
     * 如果成功新增访问记录，则立即递增 post 表的浏览量
     *
     * @param postId    文章ID
     * @param visitorId 访客设备指纹ID
     */
    void recordPostVisit(Long postId, String visitorId);

    /**
     * 同步浏览量到文章表
     * 从 post_visit_statistics 表统计并更新到 post 表的 view_count 字段
     */
    void syncViewCountToPostTable();
}
