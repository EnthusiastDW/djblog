package site.dengwei.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import site.dengwei.blog.entity.Category;

/**
 * (Category)表数据库访问层
 *
 * @author makejava
 * @since 2025-09-08 11:33:25
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {

}