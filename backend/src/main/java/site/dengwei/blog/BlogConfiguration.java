package site.dengwei.blog;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

/**
 * 配置类
 *
 * @author dengwei
 * @email 569490941@qq.com
 * @since 2025-09-04
 */
@EnableCaching
@EnableAsync
@Configuration
public class BlogConfiguration {

    /**
     * 配置验证器
     *
     * @return LocalValidatorFactoryBean 实例
     */
    @Bean
    public LocalValidatorFactoryBean validator() {
        return new LocalValidatorFactoryBean();
    }

    /**
     * 配置 MyBatis Plus 分页插件
     *
     * @return MybatisPlusInterceptor 实例
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 添加分页插件，指定数据库类型为 MySQL
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        // 设置最大单页限制数量，-1 不受限制
        paginationInterceptor.setMaxLimit(-1L);
        interceptor.addInnerInterceptor(paginationInterceptor);
        return interceptor;
    }
}
