package site.dengwei.utils.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Slug格式校验注解
 * 只能包含字母、数字、连字符和下划线
 *
 * @author dengwei
 * @since 2025-09-08
 */
@Documented
@Constraint(validatedBy = SlugValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Slug {
    
    String message() default "别名格式不正确，只能包含字母、数字、连字符和下划线";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}
