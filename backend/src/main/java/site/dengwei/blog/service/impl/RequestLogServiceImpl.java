package site.dengwei.blog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import site.dengwei.blog.entity.RequestLog;
import site.dengwei.blog.mapper.RequestLogMapper;
import site.dengwei.blog.service.RequestLogService;

/**
 * Request log service implementation.
 *
 * @author dengwei
 * @since 2026-04-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RequestLogServiceImpl extends ServiceImpl<RequestLogMapper, RequestLog> implements RequestLogService {

    @Override
    @Async
    public void saveLog(RequestLog requestLog) {
        try {
            save(requestLog);
        } catch (Exception e) {
            log.error("Failed to save request log", e);
        }
    }
}