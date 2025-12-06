package com.example.totp.service;

import com.eatthepath.otp.TimeBasedOneTimePasswordGenerator;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;

/**
 * TOTP服务类
 * 负责生成密钥、生成TOTP验证码、验证TOTP验证码
 */
public class TOTPService {
    
    // TOTP生成器实例，使用HmacSHA1算法，6位验证码，30秒时间窗口
    private final TimeBasedOneTimePasswordGenerator totpGenerator;
    
    /**
     * 构造函数，初始化TOTP生成器
     */
    public TOTPService() {
        try {
            // 创建TOTP生成器，使用HmacSHA1算法，生成6位验证码，时间窗口为30秒
            /*
            // 当前默认30秒
            this.totpGenerator = new TimeBasedOneTimePasswordGenerator();

            // 修改为60秒（或其他值）
            this.totpGenerator = new TimeBasedOneTimePasswordGenerator(Duration.ofSeconds(60));
            - TOTP标准 ：30秒是TOTP协议的标准时间间隔，大多数TOTP应用（如Google Authenticator）都使用这个值
            - 兼容性 ：如果修改时间间隔，需要确保所有使用该TOTP的应用都使用相同的时间间隔
            - 安全性 ：较短的时间间隔（如15秒）会增加安全性但用户体验较差；较长的时间间隔（如60秒）会降低安全性

             */
            this.totpGenerator = new TimeBasedOneTimePasswordGenerator();
        } catch (Exception e) {
            throw new RuntimeException("初始化TOTP生成器失败", e);
        }
    }
    
    /**
     * 生成新的TOTP密钥
     * @return Base32编码的密钥字符串
     */
    public String generateSecretKey() {
        try {
            // 创建密钥生成器，使用HmacSHA1算法
            KeyGenerator keyGenerator = KeyGenerator.getInstance(totpGenerator.getAlgorithm());
            
            // 设置密钥长度（对于HmacSHA1，推荐使用160位）
            keyGenerator.init(160);
            
            // 生成密钥
            SecretKey secretKey = keyGenerator.generateKey();
            
            // 将密钥转换为Base32编码的字符串
            return Base32.encode(secretKey.getEncoded()).replace("=", ""); // 移除填充字符
            
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("生成密钥失败", e);
        }
    }
    
    /**
     * 从Base32字符串恢复密钥对象
     * @param base32Key Base32编码的密钥字符串
     * @return 密钥对象
     */
    public Key getKeyFromBase32(String base32Key) {
        try {
            // 解码Base32字符串
            byte[] keyBytes = Base32.decode(base32Key);
            
            // 创建密钥对象
            // 创建密钥对象
            return new javax.crypto.spec.SecretKeySpec(keyBytes, totpGenerator.getAlgorithm());
            
        } catch (Exception e) {
            throw new RuntimeException("恢复密钥失败", e);
        }
    }
    
    /**
     * 将Date转换为Instant（Java 8兼容方法）
     * @param date 要转换的Date对象
     * @return 对应的Instant对象
     */
    private java.time.Instant toInstant(Date date) {
        return java.time.Instant.ofEpochMilli(date.getTime());
    }
    
    /**
     * 生成当前时间点的TOTP验证码
     * @param base32Key Base32编码的密钥字符串
     * @return 6位TOTP验证码
     */
    public String generateTOTP(String base32Key) {
        try {
            // 从Base32字符串恢复密钥
            Key key = getKeyFromBase32(base32Key);
            
            // 获取当前时间并转换为Instant
            Date now = new Date();
            java.time.Instant instant = toInstant(now);
            
            // 生成TOTP验证码
            int otp = totpGenerator.generateOneTimePassword(key, instant);
            
            // 格式化为6位数字（前面补零）
            return String.format("%06d", otp);
            
        } catch (InvalidKeyException e) {
            throw new RuntimeException("生成TOTP验证码失败，密钥无效", e);
        }
    }
    
    /**
     * 验证用户输入的TOTP验证码是否正确
     * @param base32Key Base32编码的密钥字符串
     * @param userInput 用户输入的验证码
     * @return 验证结果：true-验证成功，false-验证失败
     */
    public boolean verifyTOTP(String base32Key, String userInput) {
        try {
            // 从Base32字符串恢复密钥
            Key key = getKeyFromBase32(base32Key);
            
            // 获取当前时间并转换为Instant
            Date now = new Date();
            java.time.Instant instant = toInstant(now);
            
            // 生成当前时间点的TOTP验证码
            int expectedOtp = totpGenerator.generateOneTimePassword(key, instant);
            
            // 将用户输入转换为整数
            int userOtp;
            try {
                userOtp = Integer.parseInt(userInput);
            } catch (NumberFormatException e) {
                return false; // 输入不是有效数字
            }
            
            // 比较验证码
            return userOtp == expectedOtp;
            
        } catch (InvalidKeyException e) {
            throw new RuntimeException("验证TOTP验证码失败，密钥无效", e);
        }
    }
    
    /**
     * 验证用户输入的TOTP验证码，允许时间窗口偏移（前后一个时间窗口）
     * @param base32Key Base32编码的密钥字符串
     * @param userInput 用户输入的验证码
     * @return 验证结果：true-验证成功，false-验证失败
     */
    public boolean verifyTOTPWithWindow(String base32Key, String userInput) {
        try {
            // 从Base32字符串恢复密钥
            Key key = getKeyFromBase32(base32Key);
            
            // 获取当前时间并转换为Instant
            Date now = new Date();
            java.time.Instant instant = toInstant(now);
            
            // 检查当前时间窗口
            int currentOtp = totpGenerator.generateOneTimePassword(key, instant); //当前时间生成出的验证码
            
            // 检查前一个时间窗口（30秒前）
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(now);
            calendar.add(Calendar.SECOND, -30);//当前时间前30秒生成出的验证码
            Date previousWindow = calendar.getTime();
            java.time.Instant previousInstant = toInstant(previousWindow);
            int previousOtp = totpGenerator.generateOneTimePassword(key, previousInstant);
            
            // 检查后一个时间窗口（30秒后）
            calendar.setTime(now);
            calendar.add(Calendar.SECOND, 30);//当前时间后30秒生成出的验证码
            Date nextWindow = calendar.getTime();
            java.time.Instant nextInstant = toInstant(nextWindow);
            int nextOtp = totpGenerator.generateOneTimePassword(key, nextInstant);
            
            // 将用户输入转换为整数
            int userOtp;
            try {
                userOtp = Integer.parseInt(userInput);
            } catch (NumberFormatException e) {
                return false; // 输入不是有效数字
            }
            
            // 比较验证码（允许前后一个时间窗口的偏差）
            return userOtp == currentOtp || userOtp == previousOtp || userOtp == nextOtp; //三个验证码任意一个符合即可
            
        } catch (InvalidKeyException e) {
            throw new RuntimeException("验证TOTP验证码失败，密钥无效", e);
        }
    }
    
    /**
     * 获取TOTP配置信息
     * @return 包含算法、位数、时间窗口的配置信息
     */
    public String getTOTPInfo() {
        return String.format("算法: %s, 位数: %d, 时间窗口: %d秒", 
                           totpGenerator.getAlgorithm(), 
                           totpGenerator.getPasswordLength(), 
                           totpGenerator.getTimeStep().getSeconds());
    }
    
    /**
     * 简单的Base32编码实现（用于Google Authenticator兼容）
     */
    private static class Base32 {
        private static final String BASE32_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
        
        public static String encode(byte[] data) {
            StringBuilder result = new StringBuilder();
            int buffer = 0;
            int bitsLeft = 0;
            
            for (byte b : data) {
                buffer = (buffer << 8) | (b & 0xFF);
                bitsLeft += 8;
                
                while (bitsLeft >= 5) {
                    int index = (buffer >> (bitsLeft - 5)) & 0x1F;
                    result.append(BASE32_CHARS.charAt(index));
                    bitsLeft -= 5;
                }
            }
            
            if (bitsLeft > 0) {
                int index = (buffer << (5 - bitsLeft)) & 0x1F;
                result.append(BASE32_CHARS.charAt(index));
            }
            
            // 添加填充字符
            while (result.length() % 8 != 0) {
                result.append('=');
            }
            
            return result.toString();
        }
        
        public static byte[] decode(String base32) {
            // 移除填充字符和空格
            String cleanBase32 = base32.replace("=", "").replace(" ", "").toUpperCase();
            
            if (cleanBase32.isEmpty()) {
                return new byte[0];
            }
            
            int buffer = 0;
            int bitsLeft = 0;
            int byteCount = (cleanBase32.length() * 5 + 7) / 8;
            byte[] result = new byte[byteCount];
            int byteIndex = 0;
            
            for (int i = 0; i < cleanBase32.length(); i++) {
                char c = cleanBase32.charAt(i);
                int value = BASE32_CHARS.indexOf(c);
                
                if (value == -1) {
                    throw new IllegalArgumentException("无效的Base32字符: " + c);
                }
                
                buffer = (buffer << 5) | value;
                bitsLeft += 5;
                
                if (bitsLeft >= 8) {
                    result[byteIndex++] = (byte) (buffer >> (bitsLeft - 8));
                    bitsLeft -= 8;
                }
            }
            
            return result;
        }
    }
}