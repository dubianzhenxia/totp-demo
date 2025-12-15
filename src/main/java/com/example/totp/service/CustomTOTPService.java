package com.example.totp.service;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;

/**
 * 自定义TOTP服务类
 * 手动实现TOTP算法，支持可配置的编码算法和哈希算法
 */
public class CustomTOTPService {
    
    // 默认配置
    private static final String DEFAULT_HASH_ALGORITHM = "HmacSHA1";
    private static final String DEFAULT_ENCODING_ALGORITHM = "Base32";
    private static final int DEFAULT_CODE_LENGTH = 6;
    private static final int DEFAULT_TIME_STEP = 30; // 30秒
    
    // 当前配置
    private String hashAlgorithm;
    private String encodingAlgorithm;
    private int codeLength;
    private int timeStep;
    
    /**
     * 默认构造函数，使用默认配置
     */
    public CustomTOTPService() {
        this(DEFAULT_HASH_ALGORITHM, DEFAULT_ENCODING_ALGORITHM, DEFAULT_CODE_LENGTH, DEFAULT_TIME_STEP);
    }
    
    /**
     * 可配置的构造函数
     * @param hashAlgorithm 哈希算法（HmacSHA1, HmacSHA256, HmacSHA512）
     * @param encodingAlgorithm 编码算法（Base32, Base64）
     * @param codeLength 验证码长度（6或8）
     * @param timeStep 时间步长（秒）
     */
    public CustomTOTPService(String hashAlgorithm, String encodingAlgorithm, int codeLength, int timeStep) {
        this.hashAlgorithm = hashAlgorithm;
        this.encodingAlgorithm = encodingAlgorithm;
        this.codeLength = codeLength;
        this.timeStep = timeStep;
    }
    
    /**
     * 生成新的TOTP密钥
     * @return 编码后的密钥字符串
     */
    public String generateSecretKey() {
        try {
            // 根据哈希算法确定密钥长度
            int keyLength = getKeyLengthForAlgorithm(hashAlgorithm);
            
            // 创建密钥生成器
            KeyGenerator keyGenerator = KeyGenerator.getInstance(hashAlgorithm);
            keyGenerator.init(keyLength);
            
            // 生成密钥
            SecretKey secretKey = keyGenerator.generateKey();
            
            // 根据配置的编码算法进行编码
            return encodeKey(secretKey.getEncoded());
            
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("生成密钥失败，不支持的算法: " + hashAlgorithm, e);
        }
    }
    
    /**
     * 根据编码算法编码密钥
     * @param keyBytes 原始密钥字节数组
     * @return 编码后的密钥字符串
     */
    private String encodeKey(byte[] keyBytes) {
        switch (encodingAlgorithm.toLowerCase()) {
            case "base32":
                return Base32.encode(keyBytes);//.replace("=", ""); // 移除填充字符
            case "base64":
                return java.util.Base64.getEncoder().encodeToString(keyBytes);
            default:
                throw new IllegalArgumentException("不支持的编码算法: " + encodingAlgorithm);
        }
    }
    
    /**
     * 解码密钥字符串
     * @param encodedKey 编码后的密钥字符串
     * @return 原始密钥字节数组
     */
    private byte[] decodeKey(String encodedKey) {
        switch (encodingAlgorithm.toLowerCase()) {
            case "base32":
                return Base32.decode(encodedKey);
            case "base64":
                return java.util.Base64.getDecoder().decode(encodedKey);
            default:
                throw new IllegalArgumentException("不支持的编码算法: " + encodingAlgorithm);
        }
    }
    
    /**
     * 从编码字符串恢复密钥对象
     * @param encodedKey 编码后的密钥字符串
     * @return 密钥对象
     */
    public Key getKeyFromEncoded(String encodedKey) {
        try {
            byte[] keyBytes = decodeKey(encodedKey);
            return new SecretKeySpec(keyBytes, hashAlgorithm);
        } catch (Exception e) {
            throw new RuntimeException("恢复密钥失败", e);
        }
    }
    
    /**
     * 生成当前时间点的TOTP验证码
     * @param encodedKey 编码后的密钥字符串
     * @return TOTP验证码
     */
    public String generateTOTP(String encodedKey) {
        try {
            Key key = getKeyFromEncoded(encodedKey);
            long timeCounter = getTimeCounter();
            byte[] hash = generateHash(key, timeCounter);
            int otp = truncateHash(hash);
            return formatCode(otp);
        } catch (Exception e) {
            throw new RuntimeException("生成TOTP验证码失败", e);
        }
    }
    
    /**
     * 获取当前时间计数器（时间步长数）
     * @return 时间计数器
     */
    private long getTimeCounter() {
        long currentTime = System.currentTimeMillis() / 1000; // 转换为秒
        return currentTime / timeStep;
    }
    
    /**
     * 生成HMAC哈希
     * @param key 密钥
     * @param timeCounter 时间计数器
     * @return HMAC哈希字节数组
     */
    private byte[] generateHash(Key key, long timeCounter) {
        try {
            Mac mac = Mac.getInstance(hashAlgorithm);
            mac.init(key);
            
            // 将时间计数器转换为8字节数组（大端序）
            byte[] timeBytes = new byte[8];
            for (int i = 7; i >= 0; i--) {
                timeBytes[i] = (byte) (timeCounter & 0xFF);
                timeCounter >>= 8;
            }
            
            return mac.doFinal(timeBytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("生成HMAC哈希失败", e);
        }
    }
    
    /**
     * 截断哈希值生成OTP
     * @param hash HMAC哈希字节数组
     * @return 截断后的OTP值
     */
    private int truncateHash(byte[] hash) {
        // 获取动态二进制码（DT）
        int offset = hash[hash.length - 1] & 0xF;
        int binary = ((hash[offset] & 0x7F) << 24) |
                     ((hash[offset + 1] & 0xFF) << 16) |
                     ((hash[offset + 2] & 0xFF) << 8) |
                     (hash[offset + 3] & 0xFF);
        
        // 根据验证码长度截断
        int modulus = (int) Math.pow(10, codeLength);
        return binary % modulus;
    }
    
    /**
     * 格式化验证码（前面补零）
     * @param otp 原始OTP值
     * @return 格式化后的验证码字符串
     */
    private String formatCode(int otp) {
        return String.format("%0" + codeLength + "d", otp);
    }
    
    /**
     * 验证用户输入的TOTP验证码
     * @param encodedKey 编码后的密钥字符串
     * @param userInput 用户输入的验证码
     * @return 验证结果：true-验证成功，false-验证失败
     */
    public boolean verifyTOTP(String encodedKey, String userInput) {
        try {
            String expectedCode = generateTOTP(encodedKey);
            return expectedCode.equals(userInput);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 验证用户输入的TOTP验证码，允许时间窗口偏移
     * @param encodedKey 编码后的密钥字符串
     * @param userInput 用户输入的验证码
     * @param windowSize 时间窗口大小（前后各多少个时间步长）
     * @return 验证结果：true-验证成功，false-验证失败
     */
    public boolean verifyTOTPWithWindow(String encodedKey, String userInput, int windowSize) {
        try {
            // 检查当前时间窗口
            if (verifyTOTP(encodedKey, userInput)) {
                return true;
            }
            
            // 检查前后时间窗口（包括未来时间窗口）
            for (int i = -windowSize; i <= windowSize; i++) {
                if (i != 0) {
                    String windowCode = generateTOTPForTimeWindow(encodedKey, i);
                    if (windowCode.equals(userInput)) {
                        return true;
                    }
                }
            }
            
            return false;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 为指定时间窗口生成TOTP验证码
     * @param encodedKey 编码后的密钥字符串
     * @param windowOffset 时间窗口偏移量
     * @return TOTP验证码
     */
    private String generateTOTPForTimeWindow(String encodedKey, int windowOffset) {
        try {
            Key key = getKeyFromEncoded(encodedKey);
            long timeCounter = getTimeCounter() + windowOffset;
            byte[] hash = generateHash(key, timeCounter);
            int otp = truncateHash(hash);
            return formatCode(otp);
        } catch (Exception e) {
            throw new RuntimeException("生成时间窗口TOTP验证码失败", e);
        }
    }
    
    /**
     * 根据哈希算法获取推荐的密钥长度
     * @param algorithm 哈希算法
     * @return 推荐的密钥长度（位）
     */
    private int getKeyLengthForAlgorithm(String algorithm) {
        switch (algorithm.toUpperCase()) {
            case "HMACSHA1":
                return 160; // 20字节
            case "HMACSHA256":
                return 256; // 32字节
            case "HMACSHA512":
                return 512; // 64字节
            default:
                return 160; // 默认使用160位
        }
    }
    
    /**
     * 获取TOTP配置信息
     * @return 配置信息字符串
     */
    public String getTOTPInfo() {
        return String.format("哈希算法: %s, 编码算法: %s, 验证码长度: %d位, 时间步长: %d秒", 
                           hashAlgorithm, encodingAlgorithm, codeLength, timeStep);
    }
    
    // Getters and Setters
    public String getHashAlgorithm() { return hashAlgorithm; }
    public void setHashAlgorithm(String hashAlgorithm) { this.hashAlgorithm = hashAlgorithm; }
    
    public String getEncodingAlgorithm() { return encodingAlgorithm; }
    public void setEncodingAlgorithm(String encodingAlgorithm) { this.encodingAlgorithm = encodingAlgorithm; }
    
    public int getCodeLength() { return codeLength; }
    public void setCodeLength(int codeLength) { this.codeLength = codeLength; }
    
    public int getTimeStep() { return timeStep; }
    public void setTimeStep(int timeStep) { this.timeStep = timeStep; }
}