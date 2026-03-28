package site.dengwei.blog.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新用户请求DTO
 *
 * @author dengwei
 * @since 2025-09-08
 */
@Data
public class UpdateUserRequest {

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long id;

    /**
     * 邮箱
     */
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 头像URL
     */
    @Size(max = 255, message = "头像URL长度不能超过255个字符")
    private String avatar;

    /**
     * 个人简介
     */
    @Size(max = 500, message = "个人简介长度不能超过500个字符")
    private String bio;

    /**
     * 角色
     */
    @Size(max = 20, message = "角色长度不能超过20个字符")
    private String role;
}
