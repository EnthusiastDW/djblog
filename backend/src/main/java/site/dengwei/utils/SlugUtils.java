package site.dengwei.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

/**
 * Slug工具类，用于生成和验证slug
 *
 * @author dengwei
 */
public class SlugUtils {
    
    /**
     * 生成slug
     * @param name 原始名称
     * @return 生成的slug
     */
    public static String generateSlug(String name) {
        if (!StringUtils.isNotBlank(name)) {
            return "";
        }
        // 转换为小写，空格替换为连字符，移除特殊字符
        return name.toLowerCase()
                .replaceAll("\\s+", "-")
                .replaceAll("[^a-z0-9\\-_]", "");
    }
    
    /**
     * 验证slug格式
     * @param slug 待验证的slug
     * @return 是否有效
     */
    public static boolean isValidSlug(String slug) {
        if (!StringUtils.isNotBlank(slug)) {
            return false;
        }
        // 只允许字母、数字、连字符和下划线
        Pattern pattern = Pattern.compile("^[a-z0-9\\-_]+$");
        return pattern.matcher(slug).matches();
    }
}