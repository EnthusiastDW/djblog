package site.dengwei.blog.util;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * LambdaQueryWrapper 工具类
 * 用于根据实体对象的非空属性动态构建查询条件
 *
 * @author dengwei
 * @since 2026-03-24
 */
@Slf4j
public class LambdaQueryUtils {

    private LambdaQueryUtils() {
    }

    /**
     * 根据实体对象的非空属性构建 LambdaQueryWrapper
     *
     * @param entity 实体对象
     * @param <T>    实体类型
     * @return LambdaQueryWrapper
     */
    public static <T> LambdaQueryWrapper<T> buildFromEntity(T entity) {
        LambdaQueryWrapper<T> wrapper = new LambdaQueryWrapper<>();
        if (entity == null) {
            return wrapper;
        }

        Map<String, Object> nonNullFields = getNonNullFields(entity);
        for (Map.Entry<String, Object> entry : nonNullFields.entrySet()) {
            String fieldName = entry.getKey();
            Object value = entry.getValue();
            // 使用字段名构建查询条件，通过列名映射保证类型安全
            wrapper.eq(getColumnLambda(entity.getClass(), fieldName), value);
        }

        return wrapper;
    }

    /**
     * 获取实体对象的非空属性
     * 只获取实体类自身声明的字段，排除来自 Model 父类的字段
     */
    private static <T> Map<String, Object> getNonNullFields(T entity) {
        Map<String, Object> result = new HashMap<>();
        Class<?> clazz = entity.getClass();
        
        // 只遍历实体类自身声明的字段，不遍历父类字段
        // 因为父类 Model 中的 entityClass 等字段不是数据库字段
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            // 跳过静态字段
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            // 跳过 transient 字段
            if (Modifier.isTransient(field.getModifiers())) {
                continue;
            }
            try {
                field.setAccessible(true);
                Object value = field.get(entity);
                if (value != null) {
                    result.put(field.getName(), value);
                }
            } catch (IllegalAccessException e) {
                log.warn("无法访问字段: {}", field.getName(), e);
            }
        }
        
        return result;
    }

    /**
     * 根据字段名获取对应的 Lambda 表达式
     * 使用反射动态构建 SFunction
     */
    @SuppressWarnings("unchecked")
    private static <T> SFunction<T, Object> getColumnLambda(Class<?> entityClass, String fieldName) {
        try {
            // 获取字段类型
            Field field = getField(entityClass, fieldName);
            if (field == null) {
                throw new RuntimeException("字段不存在: " + fieldName);
            }
            
            // 构建 getter 方法名
            final String getterName = buildGetterName(field.getType(), fieldName);
            
            // 获取 getter 方法
            Method getter = entityClass.getMethod(getterName);
            
            // 返回 SFunction
            return entity -> {
                try {
                    return getter.invoke(entity);
                } catch (Exception e) {
                    throw new RuntimeException("调用 getter 方法失败: " + getterName, e);
                }
            };
        } catch (Exception e) {
            throw new RuntimeException("获取字段 Lambda 表达式失败: " + fieldName, e);
        }
    }

    /**
     * 构建 getter 方法名
     */
    private static String buildGetterName(Class<?> fieldType, String fieldName) {
        if (fieldType == boolean.class || fieldType == Boolean.class) {
            return "is" + capitalize(fieldName);
        }
        return "get" + capitalize(fieldName);
    }

    /**
     * 获取字段（包括父类，但排除 Model 类）
     */
    private static Field getField(Class<?> clazz, String fieldName) {
        while (clazz != null && clazz != Object.class && clazz != Model.class) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        return null;
    }

    /**
     * 首字母大写
     */
    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
