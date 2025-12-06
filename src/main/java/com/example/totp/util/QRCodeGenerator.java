package com.example.totp.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import javax.imageio.ImageIO;

/**
 * 二维码生成器工具类
 * 负责生成TOTP配置的二维码图片
 */
public class QRCodeGenerator {
    
    /**
     * 生成TOTP配置的二维码图片
     * @param secretKey Base32编码的密钥
     * @param accountName 账户名称
     * @param issuer 发行者
     * @return Base64编码的二维码图片数据
     */
    public static String generateTOTPQRCode(String secretKey, String accountName, String issuer) {
        try {
            // 构建TOTP URI（Google Authenticator格式）
            String totpUri = generateTOTPUri(secretKey, accountName, issuer);
            
            // 生成二维码
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(totpUri, BarcodeFormat.QR_CODE, 200, 200);
            
            // 转换为BufferedImage
            BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
            
            // 转换为Base64编码的字符串
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(qrImage, "PNG", baos);
            byte[] imageBytes = baos.toByteArray();
            
            return Base64.getEncoder().encodeToString(imageBytes);
            
        } catch (WriterException | IOException e) {
            throw new RuntimeException("生成二维码失败", e);
        }
    }
    
    /**
     * 生成TOTP URI（用于Google Authenticator等应用扫描）
     * @param secretKey Base32编码的密钥
     * @param accountName 账户名称
     * @param issuer 发行者
     * @return TOTP URI字符串
     */
    private static String generateTOTPUri(String secretKey, String accountName, String issuer) {
        // TOTP URI格式：otpauth://totp/{issuer}:{accountName}?secret={secret}&issuer={issuer}&algorithm=SHA1&digits=6&period=30
        return String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s&algorithm=SHA1&digits=6&period=30",
                           issuer, accountName, secretKey, issuer);
    }
}