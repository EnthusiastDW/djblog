package site.dengwei.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import site.dengwei.blog.entity.BlogSetting;
import site.dengwei.blog.mapper.BlogSettingMapper;
import site.dengwei.blog.service.BlogSettingService;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlogSettingServiceImpl extends ServiceImpl<BlogSettingMapper, BlogSetting> implements BlogSettingService {

    @Override
    public String getValue(String key) {
        return baseMapper.getValueByKey(key);
    }

    @Override
    public void setValue(String key, String value) {
        BlogSetting setting = getOne(new LambdaQueryWrapper<BlogSetting>().eq(BlogSetting::getSettingKey, key));
        if (setting == null) {
            setting = new BlogSetting();
            setting.setSettingKey(key);
            setting.setSettingValue(value);
            save(setting);
        } else {
            setting.setSettingValue(value);
            updateById(setting);
        }
        log.info("更新设置: {} = {}", key, value);
    }
}