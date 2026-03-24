package site.dengwei.utils.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import site.dengwei.utils.SlugUtils;

/**
 * Slug格式校验器实现
 *
 * @author dengwei
 * @since 2025-09-08
 */
public class SlugValidator implements ConstraintValidator<Slug, String> {
    
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // null值不进行校验，由 @NotBlank 等其他注解处理
        if (value == null) {
            return true;
        }
        return SlugUtils.isValidSlug(value);
    }
}
