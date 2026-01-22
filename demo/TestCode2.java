package org.ayame.testcode;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.regex.Pattern;

public class TestCode2 {
    //测试提交时转换行符  

    // 手机号正则表达式（简单版）
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    // 邮箱正则表达式（简单版） 
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^\\w+([-+.']\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$");

    /**
     * 用户实体类（内部静态类）
     */
    public static class User {
        private Long id;                 // 用户ID
        private String username;         // 用户名
        private String phone;            // 手机号
        private String email;            // 邮箱
        private LocalDateTime createTime;// 创建时间

        // 全参构造器
        public User(Long id, String username, String phone, String email) {
            this.id = id;
            this.username = username;
            this.phone = phone;
            this.email = email;
            this.createTime = LocalDateTime.now(); // 初始化创建时间为当前时间
        }

        // Getter & Setter
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public LocalDateTime getCreateTime() {
            return createTime;
        }

        // 重写toString：格式化用户信息
        @Override
        public String toString() {
            return "用户信息：\n" +
                    "ID：" + id + "\n" +
                    "用户名：" + username + "\n" +
                    "手机号：" + phone + "\n" +
                    "邮箱：" + email + "\n" +
                    "创建时间：" + createTime;
        }

        // 重写equals和hashCode：基于ID判断用户是否相同
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            User user = (User) o;
            return Objects.equals(id, user.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
    }

    /**
     * 校验用户信息合法性
     * @param user 待校验的用户对象
     * @return 校验结果（true=合法，false=非法）
     */
    public static boolean validateUser(User user) {
        // 非空校验
        if (user == null) {
            System.out.println("错误：用户对象不能为空");
            return false;
        }
        if (user.getId() == null || user.getId() <= 0) {
            System.out.println("错误：用户ID必须为正整数");
            return false;
        }
        if (user.getUsername() == null || user.getUsername().trim().length() < 2) {
            System.out.println("错误：用户名不能为空且长度至少2位");
            return false;
        }
        // 手机号格式校验
        if (user.getPhone() != null && !PHONE_PATTERN.matcher(user.getPhone()).matches()) {
            System.out.println("错误：手机号格式不正确");
            return false;
        }
        // 邮箱格式校验（非空时才校验）
        if (user.getEmail() != null && !EMAIL_PATTERN.matcher(user.getEmail()).matches()) {
            System.out.println("错误：邮箱格式不正确");
            return false;
        }
        return true;
    }

    /**
     * 格式化用户名（首字母大写，其余小写）
     * @param username 原始用户名
     * @return 格式化后的用户名
     */
    public static String formatUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return "";
        }
        String trimName = username.trim();
        return trimName.substring(0, 1).toUpperCase() + trimName.substring(1).toLowerCase();
    }

    // 测试主方法
    public static void main(String[] args) {
        // 1. 创建用户对象
        User validUser = new User(1001L, "zhangsan", "13812345678", "zhangsan@example.com");
        User invalidUser = new User(-1L, "zs", "12345678900", "zhangsan.example.com");

        // 2. 校验用户信息
        System.out.println("=== 校验合法用户 ===");
        boolean validResult = validateUser(validUser);
        System.out.println("校验结果：" + (validResult ? "通过" : "不通过"));

        System.out.println("\n=== 校验非法用户 ===");
        boolean invalidResult = validateUser(invalidUser);
        System.out.println("校验结果：" + (invalidResult ? "通过" : "不通过"));

        // 3. 格式化用户名
        System.out.println("\n=== 格式化用户名 ===");
        String rawName = "liSi";
        String formattedName = formatUsername(rawName);
        System.out.println("原始用户名：" + rawName + " → 格式化后：" + formattedName);

        // 4. 打印用户完整信息
        System.out.println("\n=== 合法用户完整信息 ===");
        System.out.println(validUser);
    }

}
