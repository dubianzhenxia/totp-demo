package com.example.totp.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 自定义二维码生成器
 * 替换ZXing javase依赖，支持可配置的二维码样式
 */
public class CustomQRCodeGenerator {
    
    // 默认配置
    private static final int DEFAULT_SIZE = 200;
    private static final Color DEFAULT_COLOR = Color.BLACK;
    private static final Color DEFAULT_BACKGROUND = Color.WHITE;
    private static final int DEFAULT_MARGIN = 1;
    
    // 当前配置
    private int size;
    private Color color;
    private Color backgroundColor;
    private int margin;
    private BufferedImage logo;
    
    /**
     * 默认构造函数，使用默认配置
     */
    public CustomQRCodeGenerator() {
        this(DEFAULT_SIZE, DEFAULT_COLOR, DEFAULT_BACKGROUND, DEFAULT_MARGIN, null);
    }
    
    /**
     * 可配置的构造函数
     * @param size 二维码图片大小（像素）
     * @param color 二维码颜色
     * @param backgroundColor 背景颜色
     * @param margin 边距
     * @param logo 中心logo图片（可为null）
     */
    public CustomQRCodeGenerator(int size, Color color, Color backgroundColor, int margin, BufferedImage logo) {
        this.size = size;
        this.color = color;
        this.backgroundColor = backgroundColor;
        this.margin = margin;
        this.logo = logo;
    }
    
    /**
     * 生成二维码图片并转换为Base64字符串
     * @param content 二维码内容
     * @return Base64编码的图片字符串
     */
    public String generateQRCodeBase64(String content) {
        try {
            BufferedImage qrCodeImage = generateQRCodeImage(content);
            return imageToBase64(qrCodeImage);
        } catch (Exception e) {
            throw new RuntimeException("生成二维码失败", e);
        }
    }
    
    /**
     * 生成二维码图片
     * @param content 二维码内容
     * @return 二维码图片对象
     */
    public BufferedImage generateQRCodeImage(String content) {
        try {
            // 创建二维码写入器
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            
            // 设置编码提示
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.MARGIN, margin);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            
            // 生成二维码位矩阵
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, size, size, hints);
            
            // 将位矩阵转换为图片
            BufferedImage qrCodeImage = matrixToImage(bitMatrix);
            
            // 添加logo（如果提供）
            if (logo != null) {
                qrCodeImage = addLogoToQRCode(qrCodeImage, logo);
            }
            
            return qrCodeImage;
            
        } catch (WriterException e) {
            throw new RuntimeException("生成二维码失败", e);
        }
    }
    
    /**
     * 将位矩阵转换为图片
     * @param matrix 位矩阵
     * @return 图片对象
     */
    private BufferedImage matrixToImage(BitMatrix matrix) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        
        // 创建图形上下文
        Graphics2D graphics = image.createGraphics();
        
        // 设置背景色
        graphics.setColor(backgroundColor);
        graphics.fillRect(0, 0, width, height);
        
        // 绘制二维码点
        graphics.setColor(color);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (matrix.get(x, y)) {
                    graphics.fillRect(x, y, 1, 1);
                }
            }
        }
        
        graphics.dispose();
        return image;
    }
    
    /**
     * 添加logo到二维码中心
     * @param qrCodeImage 二维码图片
     * @param logo logo图片
     * @return 带logo的二维码图片
     */
    private BufferedImage addLogoToQRCode(BufferedImage qrCodeImage, BufferedImage logo) {
        int qrWidth = qrCodeImage.getWidth();
        int qrHeight = qrCodeImage.getHeight();
        
        // 计算logo大小（二维码大小的1/5）
        int logoSize = Math.min(qrWidth, qrHeight) / 5;
        
        // 缩放logo图片
        BufferedImage scaledLogo = scaleImage(logo, logoSize, logoSize);
        
        // 创建新的图片（带logo）
        BufferedImage combinedImage = new BufferedImage(qrWidth, qrHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = combinedImage.createGraphics();
        
        // 绘制二维码
        graphics.drawImage(qrCodeImage, 0, 0, null);
        
        // 绘制logo（居中）
        int x = (qrWidth - logoSize) / 2;
        int y = (qrHeight - logoSize) / 2;
        graphics.drawImage(scaledLogo, x, y, null);
        
        graphics.dispose();
        return combinedImage;
    }
    
    /**
     * 缩放图片
     * @param originalImage 原始图片
     * @param targetWidth 目标宽度
     * @param targetHeight 目标高度
     * @return 缩放后的图片
     */
    private BufferedImage scaleImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        BufferedImage scaledImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = scaledImage.createGraphics();
        
        // 设置高质量缩放
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        graphics.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        graphics.dispose();
        
        return scaledImage;
    }
    
    /**
     * 将图片转换为Base64字符串
     * @param image 图片对象
     * @return Base64编码的图片字符串
     */
    private String imageToBase64(BufferedImage image) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "PNG", baos);
            byte[] imageBytes = baos.toByteArray();
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
        } catch (IOException e) {
            throw new RuntimeException("图片转换为Base64失败", e);
        }
    }
    
    /**
     * 从文件加载logo图片
     * @param logoPath logo图片文件路径
     * @return logo图片对象
     */
    public static BufferedImage loadLogoFromFile(String logoPath) {
        try {
            return ImageIO.read(new java.io.File(logoPath));
        } catch (IOException e) {
            throw new RuntimeException("加载logo图片失败: " + logoPath, e);
        }
    }
    
    /**
     * 从字节数组加载logo图片
     * @param logoBytes logo图片字节数组
     * @return logo图片对象
     */
    public static BufferedImage loadLogoFromBytes(byte[] logoBytes) {
        try {
            return ImageIO.read(new java.io.ByteArrayInputStream(logoBytes));
        } catch (IOException e) {
            throw new RuntimeException("从字节数组加载logo图片失败", e);
        }
    }
    
    // Getters and Setters
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
    
    public Color getColor() { return color; }
    public void setColor(Color color) { this.color = color; }
    
    public Color getBackgroundColor() { return color; }
    public void setBackgroundColor(Color backgroundColor) { this.backgroundColor = backgroundColor; }
    
    public int getMargin() { return margin; }
    public void setMargin(int margin) { this.margin = margin; }
    
    public BufferedImage getLogo() { return logo; }
    public void setLogo(BufferedImage logo) { this.logo = logo; }
    
    /**
     * 获取当前配置信息
     * @return 配置信息字符串
     */
    public String getQRCodeInfo() {
        return String.format("二维码大小: %dpx, 颜色: RGB(%d,%d,%d), 背景色: RGB(%d,%d,%d), 边距: %d, Logo: %s", 
                           size, 
                           color.getRed(), color.getGreen(), color.getBlue(),
                           backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue(),
                           margin, 
                           logo != null ? "已设置" : "未设置");
    }
}