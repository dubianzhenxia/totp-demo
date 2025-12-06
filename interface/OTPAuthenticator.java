package com.example.totp.interface;

import java.util.Map;

/**
 * OTP验证器接口
 * 定义所有OTP验证方式需要实现的方法
 */
public interface OTPAuthenticator {
    
    /**
     * 生成OTP配置
     * @param userId 用户ID
     * @param params 配置参数（不同类型OTP有不同的参数）
     * @return OTP配置对象
     */
    OTPConfig generateConfig(String userId, Map<String, Object> params);
    
    /**
     * 验证OTP验证码
     * @param userId 用户ID
     * @param code 用户输入的验证码
     * @param params 验证参数（如时间窗口、计数器等）
     * @return 验证结果：true-验证成功，false-验证失败
     */
    boolean verify(String userId, String code, Map<String, Object> params);
    
    /**
     * 获取OTP类型
     * @return OTP类型枚举
     */
    OTPType getType();
    
    /**
     * 是否支持该用户
     * @param userId 用户ID
     * @return 是否支持该用户使用此OTP方式
     */
    boolean supports(String userId);
    
    /**
     * 获取OTP配置信息（用于显示给用户）
     * @param userId 用户ID
     * @return 配置信息字符串
     */
    String getConfigInfo(String userId);
}