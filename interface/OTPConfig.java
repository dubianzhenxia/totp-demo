package com.example.totp.interface;

import java.util.Date;
import java.util.Map;

/**
 * OTP配置接口
 * 定义OTP配置的通用属性和方法
 */
public interface OTPConfig {
    
    /**
     * 获取用户ID
     * @return 用户ID
     */
    String getUserId();
    
    /**
     * 获取OTP类型
     * @return OTP类型枚举
     */
    OTPType getType();
    
    /**
     * 获取配置数据
     * @return 配置数据映射
     */
    Map<String, Object> getConfigData();
    
    /**
     * 是否激活
     * @return true-激活，false-禁用
     */
    boolean isActive();
    
    /**
     * 设置激活状态
     * @param active 激活状态
     */
    void setActive(boolean active);
    
    /**
     * 获取创建时间
     * @return 创建时间
     */
    Date getCreatedAt();
    
    /**
     * 获取更新时间
     * @return 更新时间
     */
    Date getUpdatedAt();
    
    /**
     * 获取配置信息（用于显示）
     * @return 配置信息字符串
     */
    String getConfigInfo();
    
    /**
     * 验证配置是否有效
     * @return true-有效，false-无效
     */
    boolean isValid();
}