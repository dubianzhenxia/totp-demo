package com.example.totp.model;

/**
 * API响应数据模型
 * 用于统一API接口的响应格式
 */
public class ApiResponse {
    
    // 响应状态：true-成功，false-失败
    private boolean success;
    
    // 响应消息
    private String message;
    
    // 响应数据
    private Object data;
    
    /**
     * 默认构造函数
     */
    public ApiResponse() {
    }
    
    /**
     * 带参数的构造函数
     * @param success 响应状态
     * @param message 响应消息
     * @param data 响应数据
     */
    public ApiResponse(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }
    
    // Getter和Setter方法
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public Object getData() {
        return data;
    }
    
    public void setData(Object data) {
        this.data = data;
    }
    
    /**
     * 创建成功响应
     * @param message 成功消息
     * @param data 响应数据
     * @return 成功响应对象
     */
    public static ApiResponse success(String message, Object data) {
        return new ApiResponse(true, message, data);
    }
    
    /**
     * 创建成功响应（无数据）
     * @param message 成功消息
     * @return 成功响应对象
     */
    public static ApiResponse success(String message) {
        return new ApiResponse(true, message, null);
    }
    
    /**
     * 创建失败响应
     * @param message 失败消息
     * @return 失败响应对象
     */
    public static ApiResponse error(String message) {
        return new ApiResponse(false, message, null);
    }
    
    /**
     * 创建失败响应（带数据）
     * @param message 失败消息
     * @param data 响应数据
     * @return 失败响应对象
     */
    public static ApiResponse error(String message, Object data) {
        return new ApiResponse(false, message, data);
    }
    
    @Override
    public String toString() {
        return "ApiResponse{" +
               "success=" + success +
               ", message='" + message + '\'' +
               ", data=" + data +
               '}';
    }
}