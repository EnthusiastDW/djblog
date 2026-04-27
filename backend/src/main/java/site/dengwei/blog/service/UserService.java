package site.dengwei.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import site.dengwei.blog.dto.AuthResponse;
import site.dengwei.blog.dto.RegisterRequest;
import site.dengwei.blog.dto.request.*;
import site.dengwei.blog.entity.User;

/**
 * 用户服务接口
 *
 * @author dengwei
 * @since 2025-09-08 11:56:30
 */
public interface UserService extends IService<User> {

    /**
     * 根据ID查询用户，不存在则抛出异常
     * @param id 用户ID
     * @return 用户信息
     */
    User getByIdOrThrow(Long id);

    /**
     * 注册新用户
     * @param request 注册请求
     * @return 用户实体
     */
    User registerUser(RegisterRequest request);

    /**
     * 创建用户
     * @param request 创建请求
     * @return 是否成功
     */
    boolean createUser(CreateUserRequest request);

    /**
     * 更新用户
     * @param request 更新请求
     * @return 认证响应（包含新token和用户信息）
     */
    AuthResponse updateUser(UpdateUserRequest request);

    /**
     * 删除用户
     * @param request 删除请求
     * @return 是否成功
     */
    boolean deleteUsers(DeleteRequest request);

    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户实体
     */
    User findByUsername(String username);

    /**
     * 获取公开用户资料（脱敏）
     * @param id 用户ID
     * @return 脱敏后的用户信息
     */
    User getPublicUser(Long id);

    /**
     * 获取博主的"关于我"内容
     * @return Markdown格式的内容
     */
    String getAboutContent();

    /**
     * 更新"关于我"内容
     * @param request 更新请求
     * @return 是否成功
     */
    boolean updateAboutContent(UpdateAboutRequest request);
}