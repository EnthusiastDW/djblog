package site.dengwei.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import site.dengwei.blog.entity.VisitStatistics;

/**
 * 访问统计服务接口
 *
 * @author dengwei
 * @since 2026-04-03
 */
public interface VisitStatisticsService extends IService<VisitStatistics> {

    /**
     * 记录访问（使用设备指纹去重）
     *
     * @param visitorId 访客设备指纹ID
     * @param ip        客户端 IP（保留用于辅助分析）
     * @param userAgent 用户代理
     */
    void recordVisit(String visitorId, String ip, String userAgent);

    /**
     * 获取今日访问人数
     *
     * @return 今日访问人数
     */
    Long getTodayVisitCount();

    /**
     * 获取总访问人数
     *
     * @return 总访问人数
     */
    Long getTotalVisitCount();
}
