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
    public void recordVisit(String ip, String userAgent) {
        LocalDate today = LocalDate.now();

        // 检查今日是否已记录该 IP
        LambdaQueryWrapper<VisitStatistics> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VisitStatistics::getIp, ip)
                .eq(VisitStatistics::getVisitDate, today);

        Long count = count(wrapper);
        if (count == 0) {
            // 未记录，则新增
            VisitStatistics visitStatistics = new VisitStatistics();
            visitStatistics.setIp(ip);
            visitStatistics.setVisitDate(today);
            visitStatistics.setUserAgent(userAgent);
            try {
                save(visitStatistics);
                log.debug("记录访问：IP={}, Date={}", ip, today);
            } catch (DuplicateKeyException e) {
                // 防止并发重复插入，忽略即可
                log.debug("访问已记录：IP={}, Date={}", ip, today);
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
