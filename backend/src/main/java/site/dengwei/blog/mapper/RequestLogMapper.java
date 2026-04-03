package site.dengwei.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import site.dengwei.blog.entity.RequestLog;

/**
 * Request log mapper interface.
 *
 * @author dengwei
 * @since 2026-04-03
 */
@Mapper
public interface RequestLogMapper extends BaseMapper<RequestLog> {
}