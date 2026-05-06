package site.dengwei.blog.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ThreadLocalRandom;
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
                // 设置缓存初始容量
                .initialCapacity(100)
                // 设置缓存最大数量
                .maximumSize(500)
                .expireAfter(new Expiry<>() {
                    @Override
                    public long expireAfterCreate(Object key, Object value, long currentTime) {
                        // 基础 1 小时 + 随机 0-10 分钟
                        long baseTime = TimeUnit.HOURS.toNanos(1);
                        long jitter = ThreadLocalRandom.current().nextLong(0, TimeUnit.MINUTES.toNanos(10));
                        return baseTime + jitter;
                    }

                    @Override
                    public long expireAfterUpdate(Object key, Object value, long currentTime, long currentDuration) {
                        return currentDuration;
                    }

                    @Override
                    public long expireAfterRead(Object key, Object value, long currentTime, long currentDuration) {
                        return currentDuration;
                    }
                }));

        return cacheManager;
    }
}
