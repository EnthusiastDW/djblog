package site.dengwei.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import site.dengwei.blog.entity.PostPlatformSync;

/**
 * 文章平台同步记录 Mapper
 *
 * @author dengwei
 * @since 2026-05-29
 */
@Mapper
public interface PostPlatformSyncMapper extends BaseMapper<PostPlatformSync> {
}
