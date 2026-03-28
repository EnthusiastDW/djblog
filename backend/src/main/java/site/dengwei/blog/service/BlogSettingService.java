package site.dengwei.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import site.dengwei.blog.entity.BlogSetting;

public interface BlogSettingService extends IService<BlogSetting> {
    String getValue(String key);
    void setValue(String key, String value);
}