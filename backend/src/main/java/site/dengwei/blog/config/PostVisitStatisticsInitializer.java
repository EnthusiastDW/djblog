package site.dengwei.blog.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import site.dengwei.blog.service.PostVisitStatisticsService;

/**
 * 应用启动后初始化文章访问统计缓存
 *
 * @author dengwei
 * @since 2026-04-30
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PostVisitStatisticsInitializer implements ApplicationRunner {

    private final PostVisitStatisticsService postVisitStatisticsService;

    @Override
    public void run(ApplicationArguments args) {
        log.info("应用启动完成，开始初始化文章访问统计缓存...");
        postVisitStatisticsService.initCache();
    }
}
