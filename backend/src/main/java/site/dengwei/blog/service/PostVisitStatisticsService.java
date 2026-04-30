package site.dengwei.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import site.dengwei.blog.entity.PostVisitStatistics;

import java.util.Map;

/**
 * 文章访问统计服务接口
 *
 * @author dengwei
 * @since 2026-04-30
 */
public interface PostVisitStatisticsService extends IService<PostVisitStatistics> {

    /**
     * 记录文章访问（使用设备指纹去重）- 同步方法
     *
     * @param postId    文章ID
     * @param visitorId 访客设备指纹ID
     */
    void recordPostVisit(Long postId, String visitorId);

    /**
     * 记录文章访问（异步执行）
     *
     * @param postId    文章ID
     * @param visitorId 访客设备指纹ID
     */
    void recordPostVisitAsync(Long postId, String visitorId);

    /**
     * 同步浏览量到文章表
     * 将统计的浏览量更新到 post 表的 view_count 字段
     */
    void syncViewCountToPostTable();

    /**
     * 初始化缓存（从数据库加载所有文章的访问量）
     */
    void initCache();
}
