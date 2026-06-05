package site.dengwei.blog.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Visit", description = "访问统计 — 今天/总访问人数统计")
@RestController
@RequestMapping("visit")
@RequiredArgsConstructor
public class VisitStatisticsController {

    private final VisitStatisticsService visitStatisticsService;

    @Operation(summary = "访问统计数据", description = "获取今日访问人数和总访问人数")
    @GetMapping("/stats")
    public Response<Map<String, Object>> getVisitStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("todayVisitors", visitStatisticsService.getTodayVisitCount());
        stats.put("totalVisitors", visitStatisticsService.getTotalVisitCount());
        return Response.success(stats);
    }
}
