package site.dengwei.blog.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import site.dengwei.blog.mapper.UserMapper;

import java.security.SecureRandom;

/**
 * 应用启动时初始化单用户模式的初始密码
 * 当系统中没有用户时，生成随机密码并在控制台输出
 *
 * @author dengwei
 * @since 2026-04-09
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InitialPasswordGenerator implements ApplicationRunner {

    private final UserMapper userMapper;
    
    private static final int PASSWORD_LENGTH = 12;
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";

    @Override
    public void run(ApplicationArguments args) {
        long userCount = userMapper.selectCount(null);
        
        if (userCount == 0) {
            String initialPassword = generateRandomPassword();
            log.info("==========================================");
            log.info("系统检测到无用户，已生成初始密码");
            log.info("初始密码: {}", initialPassword);
            log.info("请使用此密码进行首次登录和注册");
            log.info("==========================================");
            
            // 将初始密码存储到内存中，供后续验证使用
            InitialPasswordHolder.setPassword(initialPassword);
        } else {
            log.info("系统已有 {} 个用户，跳过初始密码生成", userCount);
        }
    }

    /**
     * 生成随机密码
     */
    private String generateRandomPassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(PASSWORD_LENGTH);
        
        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            password.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        
        return password.toString();
    }
}
