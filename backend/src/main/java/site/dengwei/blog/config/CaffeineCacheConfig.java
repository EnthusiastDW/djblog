package site.dengwei.blog.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Caffeine 本地缓存配置类
 *
 * @author dengwei
 * @since 2026-03-30
 */
@EnableCaching
@Configuration
public class CaffeineCacheConfig {

    /**
     * 配置 Caffeine 缓存管理器
     *
     * @return CacheManager 实例
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        
        // 配置 Caffeine 缓存参数
        cacheManager.setCaffeine(Caffeine.newBuilder()
                // 设置最后一次写入或访问后经过固定时间过期
                .expireAfterWrite(1, TimeUnit.HOURS)
                // 设置缓存初始容量
                .initialCapacity(100)
                // 设置缓存最大数量
                .maximumSize(500));
        
        return cacheManager;
    }
}
