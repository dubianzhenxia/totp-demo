package com.example.totp;

import com.example.totp.model.ApiResponse;
import com.example.totp.model.TOTPConfig;
import com.example.totp.service.TOTPService;
import com.example.totp.util.JsonUtil;
import com.example.totp.util.QRCodeGenerator;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * TOTP验证程序主服务器
 * 提供Web界面和API接口
 */
public class TOTPServer {
    
    // HTTP服务器端口
    private static final int PORT = 8080;
    
    // 服务实例
    private final TOTPService totpService;
    
    // 当前配置（简化实现，实际应用中应该使用数据库）
    private TOTPConfig currentConfig;
    
    /**
     * 构造函数
     */
    public TOTPServer() {
        this.totpService = new TOTPService();
        this.currentConfig = null;
    }
    
    /**
     * 启动HTTP服务器
     */
    public void start() throws IOException {
        // 创建HTTP服务器
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        
        // 设置API路由
        server.createContext("/api/generate", new GenerateHandler());
        server.createContext("/api/verify", new VerifyHandler());
        server.createContext("/api/current-code", new CurrentCodeHandler());
        server.createContext("/", new StaticFileHandler());
        
        // 设置线程池
        server.setExecutor(null);
        
        // 启动服务器
        server.start();
        
        System.out.println("TOTP服务器已启动，访问地址: http://localhost:" + PORT);
        System.out.println("TOTP配置信息: " + totpService.getTOTPInfo());
    }
    
    /**
     * 生成TOTP配置的API处理器
     */
    private class GenerateHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // 只处理POST请求
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "Method Not Allowed");
                return;
            }
            
            try {
                // 读取请求体
                String requestBody = readRequestBody(exchange);
                
                // 解析请求参数（简化处理，实际应该使用JSON解析）
                Map<String, String> params = parseFormData(requestBody);
                String accountName = params.getOrDefault("accountName", "Demo User");
                String issuer = params.getOrDefault("issuer", "TOTP Demo App");
                
                // 生成新的密钥
                String secretKey = totpService.generateSecretKey();
                
                // 生成当前验证码
                String currentCode = totpService.generateTOTP(secretKey);
                
                // 生成二维码
                String qrCodeImage = QRCodeGenerator.generateTOTPQRCode(secretKey, accountName, issuer);
                
                // 创建配置对象
                currentConfig = new TOTPConfig(secretKey, accountName, issuer);
                currentConfig.setCurrentCode(currentCode);
                currentConfig.setQrCodeImage(qrCodeImage);
                
                // 准备响应数据
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("secretKey", secretKey);
                responseData.put("accountName", accountName);
                responseData.put("issuer", issuer);
                responseData.put("currentCode", currentCode);
                responseData.put("qrCodeImage", "data:image/png;base64," + qrCodeImage);
                responseData.put("configInfo", currentConfig.getConfigInfo());
                
                // 发送成功响应
                ApiResponse apiResponse = ApiResponse.success("TOTP配置生成成功", responseData);
                sendJsonResponse(exchange, 200, apiResponse);
                
                System.out.println("生成新的TOTP配置: " + currentConfig.getConfigInfo());
                
            } catch (Exception e) {
                // 发送错误响应
                ApiResponse apiResponse = ApiResponse.error("生成TOTP配置失败: " + e.getMessage());
                sendJsonResponse(exchange, 500, apiResponse);
                e.printStackTrace();
            }
        }
    }
    
    /**
     * 验证TOTP验证码的API处理器
     */
    private class VerifyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // 只处理POST请求
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "Method Not Allowed");
                return;
            }
            
            try {
                // 检查是否已生成配置
                if (currentConfig == null) {
                    ApiResponse apiResponse = ApiResponse.error("请先生成TOTP配置");
                    sendJsonResponse(exchange, 400, apiResponse);
                    return;
                }
                
                // 读取请求体
                String requestBody = readRequestBody(exchange);
                
                // 解析请求参数
                Map<String, String> params = parseFormData(requestBody);
                String userCode = params.get("code");
                
                if (userCode == null || userCode.trim().isEmpty()) {
                    ApiResponse apiResponse = ApiResponse.error("请输入验证码");
                    sendJsonResponse(exchange, 400, apiResponse);
                    return;
                }
                
                // 验证验证码
                boolean isValid = totpService.verifyTOTPWithWindow(currentConfig.getSecretKey(), userCode);
                
                // 准备响应数据
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("isValid", isValid);
                responseData.put("userCode", userCode);
                responseData.put("expectedCode", totpService.generateTOTP(currentConfig.getSecretKey()));
                
                // 发送响应
                String message = isValid ? "验证码正确" : "验证码错误";
                ApiResponse apiResponse = ApiResponse.success(message, responseData);
                sendJsonResponse(exchange, 200, apiResponse);
                
                System.out.println("验证TOTP验证码: 用户输入=" + userCode + ", 结果=" + (isValid ? "正确" : "错误"));
                
            } catch (Exception e) {
                ApiResponse apiResponse = ApiResponse.error("验证TOTP验证码失败: " + e.getMessage());
                sendJsonResponse(exchange, 500, apiResponse);
                e.printStackTrace();
            }
        }
    }
    
    /**
     * 获取当前验证码的API处理器
     */
    private class CurrentCodeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // 只处理GET请求
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "Method Not Allowed");
                return;
            }
            
            try {
                // 检查是否已生成配置
                if (currentConfig == null) {
                    ApiResponse apiResponse = ApiResponse.error("请先生成TOTP配置");
                    sendJsonResponse(exchange, 400, apiResponse);
                    return;
                }
                
                // 生成当前验证码
                String currentCode = totpService.generateTOTP(currentConfig.getSecretKey());
                currentConfig.setCurrentCode(currentCode);
                
                // 准备响应数据
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("currentCode", currentCode);
                responseData.put("config", currentConfig.getConfigInfo());
                
                // 发送响应
                ApiResponse apiResponse = ApiResponse.success("获取当前验证码成功", responseData);
                sendJsonResponse(exchange, 200, apiResponse);
                
            } catch (Exception e) {
                ApiResponse apiResponse = ApiResponse.error("获取当前验证码失败: " + e.getMessage());
                sendJsonResponse(exchange, 500, apiResponse);
                e.printStackTrace();
            }
        }
    }
    
    /**
     * 静态文件处理器
     */
    private class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            
            // 默认返回index.html
            if (path.equals("/") || path.isEmpty()) {
                path = "/index.html";
            }
            
            // 构建文件路径
            Path filePath = Paths.get("src/main/resources/static" + path);
            
            // 检查文件是否存在
            if (Files.exists(filePath) && !Files.isDirectory(filePath)) {
                // 设置Content-Type
                String contentType = getContentType(filePath.toString());
                exchange.getResponseHeaders().set("Content-Type", contentType);
                
                // 发送文件内容
                exchange.sendResponseHeaders(200, Files.size(filePath));
                Files.copy(filePath, exchange.getResponseBody());
            } else {
                // 文件不存在，返回404
                sendResponse(exchange, 404, "File Not Found");
            }
        }
        
        /**
         * 根据文件扩展名获取Content-Type
         */
        private String getContentType(String filename) {
            if (filename.endsWith(".html")) return "text/html";
            if (filename.endsWith(".css")) return "text/css";
            if (filename.endsWith(".js")) return "application/javascript";
            if (filename.endsWith(".png")) return "image/png";
            if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) return "image/jpeg";
            return "text/plain";
        }
    }
    
    /**
     * 读取请求体
     */
    private String readRequestBody(HttpExchange exchange) throws IOException {
        InputStream inputStream = exchange.getRequestBody();
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString(StandardCharsets.UTF_8.name());
    }
    
    /**
     * 解析表单数据
     */
    private Map<String, String> parseFormData(String formData) {
        Map<String, String> params = new HashMap<>();
        if (formData != null && !formData.isEmpty()) {
            String[] pairs = formData.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2) {
                    try {
                        String key = java.net.URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8.name());
                        String value = java.net.URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8.name());
                        params.put(key, value);
                    } catch (UnsupportedEncodingException e) {
                        // 忽略编码异常
                    }
                }
            }
        }
        return params;
    }
    
    /**
     * 发送JSON响应
     */
    private void sendJsonResponse(HttpExchange exchange, int statusCode, ApiResponse response) throws IOException {
        String jsonResponse = JsonUtil.toJson(response);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(statusCode, jsonResponse.getBytes(StandardCharsets.UTF_8).length);
        
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(jsonResponse.getBytes(StandardCharsets.UTF_8));
        }
    }
    
    /**
     * 发送普通文本响应
     */
    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.sendResponseHeaders(statusCode, response.getBytes(StandardCharsets.UTF_8).length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes(StandardCharsets.UTF_8));
        }
    }
    
    /**
     * 主方法
     */
    public static void main(String[] args) {
        try {
            TOTPServer server = new TOTPServer();
            server.start();
        } catch (Exception e) {
            System.err.println("启动服务器失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}