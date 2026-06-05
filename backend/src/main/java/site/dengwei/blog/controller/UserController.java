package site.dengwei.blog.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import site.dengwei.blog.dto.AuthResponse;
import site.dengwei.blog.dto.request.*;
import site.dengwei.blog.entity.User;
import site.dengwei.blog.service.UserService;
import site.dengwei.blog.util.LambdaQueryUtils;
import site.dengwei.blog.dto.Response;

/**
 * 用户控制器
 *
 * @author dengwei
 * @since 2025-09-08 11:56:30
 */
@Tag(name = "User", description = "用户管理 — 用户资料 CRUD、公开信息查询")
@RestController
@RequestMapping("user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 分页查询所有数据
     */
    @Operation(summary = "用户列表", description = "分页查询所有用户（管理员使用）")
    @GetMapping
    public Response<Page<User>> selectAll(Page<User> page, User user) {
        return Response.success(userService.page(page, LambdaQueryUtils.buildFromEntity(user)));
    }

    /**
     * 根据ID查询用户
     */
    @Operation(summary = "用户详情", description = "根据用户ID获取用户信息（管理员使用）")
    @GetMapping("{id}")
    public Response<User> selectOne(@PathVariable Long id) {
        return Response.success(userService.getByIdOrThrow(id));
    }

    /**
     * 获取公开用户资料（无需登录）
     */
    @Operation(summary = "公开用户资料", description = "获取用户的公开资料信息（无需认证）")
    @GetMapping("{id}/public")
    public Response<User> getPublicProfile(@PathVariable Long id) {
        return Response.success(userService.getPublicUser(id));
    }

    /**
     * 创建用户
     */
    @Operation(summary = "创建用户", description = "创建新用户（管理员使用）")
    @PostMapping
    public Response<Boolean> insert(@Valid @RequestBody CreateUserRequest request) {
        return Response.success(userService.createUser(request));
    }

    /**
     * 更新用户
     */
    @Operation(summary = "更新用户", description = "更新当前登录用户的个人信息")
    @PutMapping
    public Response<AuthResponse> update(@Valid @RequestBody UpdateUserRequest request) {
        return Response.success(userService.updateUser(request));
    }

    /**
     * 删除用户
     */
    @Operation(summary = "删除用户", description = "删除指定用户（管理员使用）")
    @DeleteMapping
    public Response<Boolean> delete(@Valid @RequestBody DeleteRequest request) {
        return Response.success(userService.deleteUsers(request));
    }

    /**
     * 获取博主的"关于我"内容（公开接口）
     */
    @Operation(summary = "关于我内容", description = "获取博主的\"关于我\"页面内容（无需认证）")
    @GetMapping("/about")
    public Response<String> getAboutContent() {
        return Response.success(userService.getAboutContent());
    }

    /**
     * 更新"关于我"内容（需要登录）
     */
    @Operation(summary = "更新关于我", description = "更新博主的\"关于我\"页面内容（需要登录）")
    @PutMapping("/about")
    public Response<Boolean> updateAboutContent(@Valid @RequestBody UpdateAboutRequest request) {
        return Response.success(userService.updateAboutContent(request));
    }
}
