package com.example.totp.service;

/**
 * Base32编码器实现
 * 支持RFC 4648标准的Base32编码
 */
public class Base32 {
    
    private static final String BASE32_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    private static final char PADDING_CHAR = '=';
    
    /**
     * 将字节数组编码为Base32字符串
     * @param data 原始字节数组
     * @return Base32编码字符串
     */
    public static String encode(byte[] data) {
        if (data == null || data.length == 0) {
            return "";
        }
        
        StringBuilder result = new StringBuilder();
        int buffer = 0;
        int bufferLength = 0;
        
        for (byte b : data) {
            // 将字节添加到缓冲区
            buffer = (buffer << 8) | (b & 0xFF);
            bufferLength += 8;
            
            // 每次处理5位
            while (bufferLength >= 5) {
                int index = (buffer >> (bufferLength - 5)) & 0x1F;
                result.append(BASE32_CHARS.charAt(index));
                bufferLength -= 5;
            }
        }
        
        // 处理剩余的位
        if (bufferLength > 0) {
            int index = (buffer << (5 - bufferLength)) & 0x1F;
            result.append(BASE32_CHARS.charAt(index));
        }
        
        // 添加填充字符
        int paddingLength = (8 - (result.length() % 8)) % 8;
        for (int i = 0; i < paddingLength; i++) {
            result.append(PADDING_CHAR);
        }
        
        return result.toString();
    }
    
    /**
     * 将Base32字符串解码为字节数组
     * @param encoded Base32编码字符串
     * @return 原始字节数组
     */
    public static byte[] decode(String encoded) {
        if (encoded == null || encoded.isEmpty()) {
            return new byte[0];
        }
        
        // 移除填充字符和空白字符
        String cleanEncoded = encoded.replace(String.valueOf(PADDING_CHAR), "").trim();
        
        if (cleanEncoded.isEmpty()) {
            return new byte[0];
        }
        
        // 计算输出字节数组长度
        int outputLength = (cleanEncoded.length() * 5) / 8;
        byte[] result = new byte[outputLength];
        
        int buffer = 0;
        int bufferLength = 0;
        int outputIndex = 0;
        
        for (int i = 0; i < cleanEncoded.length(); i++) {
            char c = cleanEncoded.charAt(i);
            int value = BASE32_CHARS.indexOf(c);
            
            if (value == -1) {
                throw new IllegalArgumentException("无效的Base32字符: " + c);
            }
            
            // 将5位值添加到缓冲区
            buffer = (buffer << 5) | value;
            bufferLength += 5;
            
            // 每次处理8位
            while (bufferLength >= 8) {
                result[outputIndex++] = (byte) ((buffer >> (bufferLength - 8)) & 0xFF);
                bufferLength -= 8;
            }
        }
        
        return result;
    }
    
    /**
     * 验证字符串是否为有效的Base32编码
     * @param encoded 待验证的字符串
     * @return true-有效，false-无效
     */
    public static boolean isValidBase32(String encoded) {
        if (encoded == null) {
            return false;
        }
        
        String cleanEncoded = encoded.replace(String.valueOf(PADDING_CHAR), "").trim();
        
        if (cleanEncoded.isEmpty()) {
            return false;
        }
        
        for (int i = 0; i < cleanEncoded.length(); i++) {
            char c = cleanEncoded.charAt(i);
            if (BASE32_CHARS.indexOf(c) == -1) {
                return false;
            }
        }
        
        return true;
    }
}