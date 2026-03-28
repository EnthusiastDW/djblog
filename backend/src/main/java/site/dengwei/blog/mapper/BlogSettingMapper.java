package site.dengwei.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import site.dengwei.blog.entity.BlogSetting;

@Mapper
public interface BlogSettingMapper extends BaseMapper<BlogSetting> {
    
    @Select("SELECT setting_value FROM blog_setting WHERE setting_key = #{key}")
    String getValueByKey(@Param("key") String key);
}