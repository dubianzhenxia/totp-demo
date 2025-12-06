package com.example.totp.interface;

import java.util.*;
import java.util.stream.Collectors;

/**
 * OTP管理器
 * 统一管理所有OTP验证器，提供统一的API接口
 */
public class OTPManager {
    
    private final Map<OTPType, OTPAuthenticator> authenticators = new HashMap<>();
    
    /**
     * 注册OTP验证器
     * @param authenticator OTP验证器实例
     */
    public void registerAuthenticator(OTPAuthenticator authenticator) {
        if (authenticator != null) {
            authenticators.put(authenticator.getType(), authenticator);
            System.out.println("注册OTP验证器: " + authenticator.getType().getDisplayName());
        }
    }
    
    /**
     * 注销OTP验证器
     * @param type OTP类型
     */
    public void unregisterAuthenticator(OTPType type) {
        OTPAuthenticator removed = authenticators.remove(type);
        if (removed != null) {
            System.out.println("注销OTP验证器: " + type.getDisplayName());
        }
    }
    
    /**
     * 生成OTP配置
     * @param userId 用户ID
     * @param type OTP类型
     * @param params 配置参数
     * @return OTP配置对象
     * @throws UnsupportedOTPTypeException 不支持的OTP类型
     */
    public OTPConfig generateConfig(String userId, OTPType type, Map<String, Object> params) {
        OTPAuthenticator authenticator = authenticators.get(type);
        if (authenticator == null) {
            throw new UnsupportedOTPTypeException("不支持的OTP类型: " + type.getDisplayName());
        }
        
        if (!authenticator.supports(userId)) {
            throw new UnsupportedUserException("用户 " + userId + " 不支持使用 " + type.getDisplayName());
        }
        
        return authenticator.generateConfig(userId, params);
    }
    
    /**
     * 验证OTP验证码
     * @param userId 用户ID
     * @param code 验证码
     * @param type OTP类型
     * @param params 验证参数
     * @return 验证结果
     */
    public boolean verify(String userId, String code, OTPType type, Map<String, Object> params) {
        OTPAuthenticator authenticator = authenticators.get(type);
        if (authenticator == null) {
            System.err.println("不支持的OTP类型: " + type.getDisplayName());
            return false;
        }
        
        if (!authenticator.supports(userId)) {
            System.err.println("用户 " + userId + " 不支持使用 " + type.getDisplayName());
            return false;
        }
        
        return authenticator.verify(userId, code, params);
    }
    
    /**
     * 获取用户支持的OTP类型列表
     * @param userId 用户ID
     * @return 支持的OTP类型列表
     */
    public List<OTPType> getSupportedTypes(String userId) {
        return authenticators.values().stream()
            .filter(auth -> auth.supports(userId))
            .map(OTPAuthenticator::getType)
            .collect(Collectors.toList());
    }
    
    /**
     * 获取所有已注册的OTP类型
     * @return 已注册的OTP类型列表
     */
    public List<OTPType> getRegisteredTypes() {
        return new ArrayList<>(authenticators.keySet());
    }
    
    /**
     * 检查是否支持指定的OTP类型
     * @param type OTP类型
     * @return true-支持，false-不支持
     */
    public boolean supportsType(OTPType type) {
        return authenticators.containsKey(type);
    }
    
    /**
     * 获取OTP验证器
     * @param type OTP类型
     * @return OTP验证器实例，如果不存在返回null
     */
    public OTPAuthenticator getAuthenticator(OTPType type) {
        return authenticators.get(type);
    }
    
    /**
     * 获取用户可用的OTP验证器列表
     * @param userId 用户ID
     * @return 可用的OTP验证器列表
     */
    public List<OTPAuthenticator> getAvailableAuthenticators(String userId) {
        return authenticators.values().stream()
            .filter(auth -> auth.supports(userId))
            .collect(Collectors.toList());
    }
    
    /**
     * 获取OTP配置信息
     * @param userId 用户ID
     * @param type OTP类型
     * @return 配置信息字符串
     */
    public String getConfigInfo(String userId, OTPType type) {
        OTPAuthenticator authenticator = authenticators.get(type);
        if (authenticator != null && authenticator.supports(userId)) {
            return authenticator.getConfigInfo(userId);
        }
        return "配置信息不可用";
    }
    
    /**
     * 批量验证（支持多种OTP方式）
     * @param userId 用户ID
     * @param code 验证码
     * @param types OTP类型列表
     * @param params 验证参数
     * @return 第一个验证成功的OTP类型，如果都失败返回null
     */
    public OTPType batchVerify(String userId, String code, List<OTPType> types, Map<String, Object> params) {
        for (OTPType type : types) {
            if (verify(userId, code, type, params)) {
                return type;
            }
        }
        return null;
    }
    
    /**
     * 获取管理器状态信息
     * @return 状态信息字符串
     */
    public String getStatusInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("OTP管理器状态:\n");
        sb.append("已注册验证器数量: ").append(authenticators.size()).append("\n");
        
        for (OTPType type : authenticators.keySet()) {
            sb.append("- ").append(type.getDisplayName())
              .append(" (").append(type.name()).append(")\n");
        }
        
        return sb.toString();
    }
}