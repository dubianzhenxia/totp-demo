package com.example.totp.interface;

/**
 * OTP异常基类
 */
public class OTPException extends RuntimeException {
    
    private final String errorCode;
    
    public OTPException(String message) {
        super(message);
        this.errorCode = "OTP_ERROR";
    }
    
    public OTPException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public OTPException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "OTP_ERROR";
    }
    
    public OTPException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}

/**
 * 不支持的OTP类型异常
 */
class UnsupportedOTPTypeException extends OTPException {
    
    public UnsupportedOTPTypeException(String message) {
        super(message, "UNSUPPORTED_OTP_TYPE");
    }
    
    public UnsupportedOTPTypeException(String message, Throwable cause) {
        super(message, "UNSUPPORTED_OTP_TYPE", cause);
    }
}

/**
 * 不支持的用户异常
 */
class UnsupportedUserException extends OTPException {
    
    public UnsupportedUserException(String message) {
        super(message, "UNSUPPORTED_USER");
    }
    
    public UnsupportedUserException(String message, Throwable cause) {
        super(message, "UNSUPPORTED_USER", cause);
    }
}

/**
 * OTP配置异常
 */
class OTPConfigException extends OTPException {
    
    public OTPConfigException(String message) {
        super(message, "CONFIG_ERROR");
    }
    
    public OTPConfigException(String message, Throwable cause) {
        super(message, "CONFIG_ERROR", cause);
    }
}

/**
 * OTP验证异常
 */
class OTPVerificationException extends OTPException {
    
    public OTPVerificationException(String message) {
        super(message, "VERIFICATION_ERROR");
    }
    
    public OTPVerificationException(String message, Throwable cause) {
        super(message, "VERIFICATION_ERROR", cause);
    }
}

/**
 * OTP密钥异常
 */
class OTPSecretException extends OTPException {
    
    public OTPSecretException(String message) {
        super(message, "SECRET_ERROR");
    }
    
    public OTPSecretException(String message, Throwable cause) {
        super(message, "SECRET_ERROR", cause);
    }
}

/**
 * OTP超时异常
 */
class OTPTimeoutException extends OTPException {
    
    public OTPTimeoutException(String message) {
        super(message, "TIMEOUT_ERROR");
    }
    
    public OTPTimeoutException(String message, Throwable cause) {
        super(message, "TIMEOUT_ERROR", cause);
    }
}

/**
 * OTP重试次数超限异常
 */
class OTPRetryLimitExceededException extends OTPException {
    
    private final int maxRetries;
    private final int currentRetries;
    
    public OTPRetryLimitExceededException(String message, int maxRetries, int currentRetries) {
        super(message, "RETRY_LIMIT_EXCEEDED");
        this.maxRetries = maxRetries;
        this.currentRetries = currentRetries;
    }
    
    public OTPRetryLimitExceededException(String message, int maxRetries, int currentRetries, Throwable cause) {
        super(message, "RETRY_LIMIT_EXCEEDED", cause);
        this.maxRetries = maxRetries;
        this.currentRetries = currentRetries;
    }
    
    public int getMaxRetries() {
        return maxRetries;
    }
    
    public int getCurrentRetries() {
        return currentRetries;
    }
}

/**
 * OTP已使用异常（用于实现"用过即失效"功能）
 */
class OTPAlreadyUsedException extends OTPException {
    
    private final String code;
    private final long usedAt;
    
    public OTPAlreadyUsedException(String message, String code, long usedAt) {
        super(message, "CODE_ALREADY_USED");
        this.code = code;
        this.usedAt = usedAt;
    }
    
    public OTPAlreadyUsedException(String message, String code, long usedAt, Throwable cause) {
        super(message, "CODE_ALREADY_USED", cause);
        this.code = code;
        this.usedAt = usedAt;
    }
    
    public String getCode() {
        return code;
    }
    
    public long getUsedAt() {
        return usedAt;
    }
}