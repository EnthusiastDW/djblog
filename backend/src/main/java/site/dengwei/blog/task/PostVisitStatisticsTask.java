package site.dengwei.blog.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import site.dengwei.blog.service.PostVisitStatisticsService;

/**
 * 文章访问统计定时任务
 *
 * @author dengwei
 * @since 2026-04-30
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PostVisitStatisticsTask {

    private final PostVisitStatisticsService postVisitStatisticsService;

    /**
     * 每天凌晨2点同步浏览量到文章表
     * cron表达式：秒 分 时 日 月 周
     * 0 0 2 * * ? 表示每天凌晨2点执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void syncViewCountToPostTable() {
        log.debug("执行浏览量同步任务...");
        postVisitStatisticsService.syncViewCountToPostTable();
    }
}
