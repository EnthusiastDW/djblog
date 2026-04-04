package site.dengwei.blog.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.dengwei.blog.dto.Response;
import site.dengwei.blog.service.VisitStatisticsService;

import java.util.HashMap;
import java.util.Map;

/**
 * 访问统计控制器
 *
 * @author dengwei
 * @since 2026-04-03
 */
@RestController
@RequestMapping("visit")
@RequiredArgsConstructor
public class VisitStatisticsController {

    private final VisitStatisticsService visitStatisticsService;

    @GetMapping("/stats")
    public Response<Map<String, Object>> getVisitStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("todayVisitors", visitStatisticsService.getTodayVisitCount());
        stats.put("totalVisitors", visitStatisticsService.getTotalVisitCount());
        return Response.success(stats);
    }
}
