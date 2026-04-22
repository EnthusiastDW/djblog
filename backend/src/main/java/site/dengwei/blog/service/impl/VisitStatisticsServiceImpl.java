package site.dengwei.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.dengwei.blog.entity.VisitStatistics;
import site.dengwei.blog.mapper.VisitStatisticsMapper;
import site.dengwei.blog.service.VisitStatisticsService;

import java.time.LocalDate;

/**
 * 访问统计服务实现类
 *
 * @author dengwei
 * @since 2026-04-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VisitStatisticsServiceImpl extends ServiceImpl<VisitStatisticsMapper, VisitStatistics> implements VisitStatisticsService {

    private final VisitStatisticsMapper visitStatisticsMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordVisit(String visitorId, String ip, String userAgent) {
        LocalDate today = LocalDate.now();

        // 优先使用 visitorId 去重，如果为空则降级使用 IP（兼容旧数据）
        String uniqueKey = (visitorId != null && !visitorId.isEmpty()) ? visitorId : ip;
        
        if (uniqueKey == null || uniqueKey.isEmpty()) {
            log.warn("无法记录访问：visitorId 和 IP 均为空");
            return;
        }

        // 检查今日是否已记录该访客
        LambdaQueryWrapper<VisitStatistics> wrapper = new LambdaQueryWrapper<>();
        if (visitorId != null && !visitorId.isEmpty()) {
            // 使用 visitorId 去重
            wrapper.eq(VisitStatistics::getVisitorId, visitorId)
                    .eq(VisitStatistics::getVisitDate, today);
        } else {
            // 降级：使用 IP 去重（兼容旧数据或指纹获取失败的情况）
            wrapper.eq(VisitStatistics::getIp, ip)
                    .eq(VisitStatistics::getVisitDate, today);
        }

        long count = count(wrapper);
        if (count == 0) {
            // 未记录，则新增
            VisitStatistics visitStatistics = new VisitStatistics();
            visitStatistics.setVisitorId(visitorId);
            visitStatistics.setIp(ip);
            visitStatistics.setVisitDate(today);
            visitStatistics.setUserAgent(userAgent);
            try {
                save(visitStatistics);
                log.debug("记录访问：VisitorId={}, IP={}, Date={}", visitorId, ip, today);
            } catch (DuplicateKeyException e) {
                // 防止并发重复插入，忽略即可
                log.debug("访问已记录：VisitorId={}, IP={}, Date={}", visitorId, ip, today);
            }
        }
    }

    @Override
    public Long getTodayVisitCount() {
        return visitStatisticsMapper.countTodayVisitors();
    }

    @Override
    public Long getTotalVisitCount() {
        return visitStatisticsMapper.countTotalVisitors();
    }
}
