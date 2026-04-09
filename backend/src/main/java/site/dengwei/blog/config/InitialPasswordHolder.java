package site.dengwei.blog.config;

/**
 * 初始密码持有者（线程安全）
 * 用于在应用启动后临时存储生成的初始密码
 *
 * @author dengwei
 * @since 2026-04-09
 */
public class InitialPasswordHolder {
    
    private static volatile String initialPassword = null;

    public static void setPassword(String password) {
        initialPassword = password;
    }

    public static String getPassword() {
        return initialPassword;
    }

    public static void clear() {
        initialPassword = null;
    }
}
