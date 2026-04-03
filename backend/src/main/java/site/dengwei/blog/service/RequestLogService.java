package site.dengwei.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import site.dengwei.blog.entity.RequestLog;

/**
 * Request log service interface.
 *
 * @author dengwei
 * @since 2026-04-03
 */
public interface RequestLogService extends IService<RequestLog> {

    /**
     * Save request log asynchronously.
     *
     * @param requestLog request log to save
     */
    void saveLog(RequestLog requestLog);
}