package site.dengwei.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 统一响应结果封装类
 *
 * @param <T> 数据泛型
 * @author dengwei
 * @since 2024/5/28 23:27
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Response<T> implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 响应码
     */
    private Integer code;
    
    /**
     * 响应消息
     */
    private String message;
    
    /**
     * 响应数据
     */
    private T data;

    /**
     * 成功响应（无数据）
     *
     * @return Response实例
     */
    public static Response<Void> success() {
        return new Response<>(200, "success", null);
    }

    /**
     * 成功响应（有数据）
     *
     * @param data 响应数据
     * @param <T>  数据类型
     * @return Response实例
     */
    public static <T> Response<T> success(T data) {
        return new Response<>(200, "success", data);
    }

    /**
     * 错误响应
     *
     * @param message 错误消息
     * @return Response实例
     */
    public static Response<Void> error(String message) {
        return new Response<>(500, message, null);
    }
    
    /**
     * 错误响应（带数据）
     *
     * @param message 错误消息
     * @param data 错误数据
     * @param <T> 数据类型
     * @return Response实例
     */
    public static <T> Response<T> error(String message, T data) {
        return new Response<>(500, message, data);
    }
}