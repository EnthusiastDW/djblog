package site.dengwei.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import site.dengwei.blog.dto.RegisterRequest;
import site.dengwei.blog.dto.request.*;
import site.dengwei.blog.entity.User;
import site.dengwei.blog.exception.BusinessException;
import site.dengwei.blog.mapper.UserMapper;
import site.dengwei.blog.service.UserService;

/**
 * 用户服务实现类
 *
 * @author dengwei
 * @since 2025-09-08 11:56:31
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final PasswordEncoder passwordEncoder;

    @Override
    public User getByIdOrThrow(Long id) {
        User user = getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }

    @Override
    public User registerUser(RegisterRequest request) {
        log.info("正在注册新用户: {}", request.getUsername());

        // 检查用户名是否已存在
        User existingUser = getOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, request.getUsername())
        );
        if (existingUser != null) {
            log.warn("用户注册失败，用户名已被占用: {}", request.getUsername());
            throw new BusinessException("用户名已被占用");
        }

        // 检查邮箱是否已存在
        existingUser = getOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getEmail, request.getEmail())
        );
        if (existingUser != null) {
            log.warn("用户注册失败，邮箱已被注册: {}", request.getEmail());
            throw new BusinessException("邮箱已被注册");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        save(user);
        log.info("用户注册成功: {}", request.getUsername());
        return user;
    }

    @Override
    public boolean createUser(CreateUserRequest request) {
        log.info("创建用户: {}", request.getUsername());
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setAvatarUrl(request.getAvatar());
        return save(user);
    }

    @Override
    public boolean updateUser(UpdateUserRequest request) {
        log.info("更新用户，ID: {}", request.getId());
        User user = getByIdOrThrow(request.getId());
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getAvatar() != null) {
            user.setAvatarUrl(request.getAvatar());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
        return updateById(user);
    }

    @Override
    public boolean deleteUsers(DeleteRequest request) {
        log.info("删除用户，ID 列表：{}", request.getIdList());
        return removeByIds(request.getIdList());
    }

    @Override
    public User findByUsername(String username) {
        return getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
    }

    @Override
    public User getPublicUser(Long id) {
        User user = getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        User publicUser = new User();
        publicUser.setId(user.getId());
        publicUser.setUsername(user.getUsername());
        publicUser.setAvatarUrl(user.getAvatarUrl());
        publicUser.setBio(user.getBio());
        return publicUser;
    }
}