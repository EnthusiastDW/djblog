package site.dengwei.blog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.dengwei.blog.entity.PostVisitStatistics;
import site.dengwei.blog.mapper.PostMapper;
import site.dengwei.blog.mapper.PostVisitStatisticsMapper;
import site.dengwei.blog.service.PostVisitStatisticsService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 文章访问统计服务实现类
 *
 * @author dengwei
 * @since 2026-04-30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostVisitStatisticsServiceImpl extends ServiceImpl<PostVisitStatisticsMapper, PostVisitStatistics> implements PostVisitStatisticsService {

    private final PostVisitStatisticsMapper postVisitStatisticsMapper;
    private final PostMapper postMapper;

    @Async
    @Override
    public void recordPostVisit(Long postId, String visitorId) {
        if (postId == null || visitorId == null || visitorId.isEmpty()) {
            return;
        }
        LocalDate today = LocalDate.now();
        try {
            // 插入访问记录（唯一索引保证同一天同一访客只记录一次）
            PostVisitStatistics statistics = new PostVisitStatistics();
            statistics.setPostId(postId);
            statistics.setVisitorId(visitorId);
            statistics.setVisitDate(today);
            save(statistics);

            // 成功新增，立即递增 post 表的浏览量
            postMapper.incrementViewCount(postId);
            log.debug("记录访问成功并递增浏览量：PostId={}, VisitorId={}", postId, visitorId);
        } catch (DuplicateKeyException e) {
            // 防止并发重复插入，忽略即可（数据库唯一索引已保证去重）
            log.debug("访问已记录（并发或重复）：PostId={}, VisitorId={}", postId, visitorId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncViewCountToPostTable() {
        log.info("开始同步浏览量到文章表...");
        
        try {
            // 从统计表重新计算每篇文章的总访问量
            List<Map<String, Object>> results = postVisitStatisticsMapper.batchCountPostVisitors();
            
            int updatedCount = 0;
            for (Map<String, Object> row : results) {
                Long postId = ((Number) row.get("post_id")).longValue();
                Long viewCount = ((Number) row.get("view_count")).longValue();
                
                // 更新 post 表的 view_count 字段
                postMapper.updateViewCount(postId, viewCount);
                updatedCount++;
            }
            
            log.info("同步浏览量到文章表完成，更新了 {} 篇文章", updatedCount);
        } catch (Exception e) {
            log.error("同步浏览量到文章表失败", e);
            throw e; // 抛出异常以便事务回滚
        }
    }
}
