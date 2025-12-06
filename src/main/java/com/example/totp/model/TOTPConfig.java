package com.example.totp.model;

/**
 * TOTP配置数据模型
 * 用于存储TOTP相关的配置信息
 */
public class TOTPConfig {
    
    // Base32编码的密钥
    private String secretKey;
    
    // 账户名称
    private String accountName;
    
    // 发行者（应用名称）
    private String issuer;
    
    // 当前TOTP验证码
    private String currentCode;
    
    // 二维码图片的Base64数据
    private String qrCodeImage;
    
    /**
     * 默认构造函数
     */
    public TOTPConfig() {
    }
    
    /**
     * 带参数的构造函数
     * @param secretKey 密钥
     * @param accountName 账户名称
     * @param issuer 发行者
     */
    public TOTPConfig(String secretKey, String accountName, String issuer) {
        this.secretKey = secretKey;
        this.accountName = accountName;
        this.issuer = issuer;
    }
    
    // Getter和Setter方法
    
    public String getSecretKey() {
        return secretKey;
    }
    
    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }
    
    public String getAccountName() {
        return accountName;
    }
    
    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }
    
    public String getIssuer() {
        return issuer;
    }
    
    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }
    
    public String getCurrentCode() {
        return currentCode;
    }
    
    public void setCurrentCode(String currentCode) {
        this.currentCode = currentCode;
    }
    
    public String getQrCodeImage() {
        return qrCodeImage;
    }
    
    public void setQrCodeImage(String qrCodeImage) {
        this.qrCodeImage = qrCodeImage;
    }
    
    /**
     * 生成配置信息的字符串表示
     * @return 配置信息字符串
     */
    public String getConfigInfo() {
        return String.format("账户: %s, 发行者: %s, 密钥: %s", accountName, issuer, secretKey);
    }
    
    @Override
    public String toString() {
        return "TOTPConfig{" +
               "secretKey='" + secretKey + '\'' +
               ", accountName='" + accountName + '\'' +
               ", issuer='" + issuer + '\'' +
               ", currentCode='" + currentCode + '\'' +
               '}';
    }
}