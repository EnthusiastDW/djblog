package site.dengwei.blog.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import site.dengwei.blog.dto.request.*;
import site.dengwei.blog.entity.User;
import site.dengwei.blog.service.UserService;
import site.dengwei.blog.util.LambdaQueryUtils;
import site.dengwei.common.beans.Response;

/**
 * 用户控制器
 *
 * @author dengwei
 * @since 2025-09-08 11:56:30
 */
@RestController
@RequestMapping("user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 分页查询所有数据
     */
    @GetMapping
    public Response<Page<User>> selectAll(Page<User> page, User user) {
        return Response.success(userService.page(page, LambdaQueryUtils.buildFromEntity(user)));
    }

    /**
     * 根据ID查询用户
     */
    @GetMapping("{id}")
    public Response<User> selectOne(@PathVariable Long id) {
        return Response.success(userService.getByIdOrThrow(id));
    }

    /**
     * 创建用户
     */
    @PostMapping
    public Response<Boolean> insert(@Valid @RequestBody CreateUserRequest request) {
        return Response.success(userService.createUser(request));
    }

    /**
     * 更新用户
     */
    @PutMapping
    public Response<Boolean> update(@Valid @RequestBody UpdateUserRequest request) {
        return Response.success(userService.updateUser(request));
    }

    /**
     * 删除用户
     */
    @DeleteMapping
    public Response<Boolean> delete(@Valid @RequestBody DeleteRequest request) {
        return Response.success(userService.deleteUsers(request));
    }
}
