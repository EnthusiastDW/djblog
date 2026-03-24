package site.dengwei.blog;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * 博客服务启动类
 *
 * @author dengwei
 * @email 569490941@qq.com
 * @since 2025-09-04
 */
@SpringBootApplication
@EnableCaching
@MapperScan("site.dengwei.blog.mapper")
public class BlogApplication {
    public static void main(String[] args) {
        SpringApplication.run(BlogApplication.class, args);
    }
}