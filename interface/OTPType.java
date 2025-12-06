package com.example.totp.interface;

/**
 * OTP类型枚举
 * 定义支持的OTP验证方式类型
 */
public enum OTPType {
    
    /**
     * 时间型一次性密码 (Time-based One-Time Password)
     * 基于时间生成验证码，每30秒变化一次
     */
    TOTP("时间型一次性密码", "基于时间生成验证码，每30秒变化一次"),
    
    /**
     * 计数器型一次性密码 (HMAC-based One-Time Password)
     * 基于计数器生成验证码，每次验证后计数器递增
     */
    HOTP("计数器型一次性密码", "基于计数器生成验证码，每次验证后计数器递增"),
    
    /**
     * 短信验证码
     * 通过短信发送验证码
     */
    SMS_OTP("短信验证码", "通过短信发送验证码"),
    
    /**
     * 邮件验证码
     * 通过邮件发送验证码
     */
    EMAIL_OTP("邮件验证码", "通过邮件发送验证码"),
    
    /**
     * 推送验证码
     * 通过推送通知发送验证码
     */
    PUSH_OTP("推送验证码", "通过推送通知发送验证码"),
    
    /**
     * 硬件令牌
     * 使用硬件设备生成验证码
     */
    HARDWARE_TOKEN("硬件令牌", "使用硬件设备生成验证码"),
    
    /**
     * 生物特征验证
     * 使用指纹、面部识别等生物特征
     */
    BIOMETRIC("生物特征验证", "使用指纹、面部识别等生物特征");
    
    private final String displayName;
    private final String description;
    
    OTPType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    /**
     * 获取显示名称
     * @return 显示名称
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * 获取描述信息
     * @return 描述信息
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * 根据名称获取OTP类型
     * @param name 类型名称
     * @return OTP类型，如果找不到返回null
     */
    public static OTPType fromName(String name) {
        for (OTPType type : values()) {
            if (type.name().equalsIgnoreCase(name) || 
                type.getDisplayName().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }
    
    /**
     * 检查是否为软件令牌类型
     * @return true-软件令牌，false-其他类型
     */
    public boolean isSoftwareToken() {
        return this == TOTP || this == HOTP;
    }
    
    /**
     * 检查是否为外部服务类型
     * @return true-外部服务，false-内部生成
     */
    public boolean isExternalService() {
        return this == SMS_OTP || this == EMAIL_OTP || this == PUSH_OTP;
    }
    
    /**
     * 检查是否为硬件类型
     * @return true-硬件类型，false-软件类型
     */
    public boolean isHardwareBased() {
        return this == HARDWARE_TOKEN || this == BIOMETRIC;
    }
}