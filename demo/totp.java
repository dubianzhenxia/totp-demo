import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * TOTP算法手动实现（遵循RFC 6238标准）
 * 核心流程：时间戳处理 → HMAC-SHA1哈希 → 截断哈希结果 → 取模生成6位码
 */
public class TotpAlgorithm {
    // 时间步长（默认30秒，与Google Authenticator一致）
    private static final int TIME_STEP = 30;
    // 动态码长度（6位）
    private static final int CODE_DIGITS = 6;
    // HMAC算法（默认SHA1，也可替换为SHA256/SHA512）
    private static final String HMAC_ALGORITHM = "HmacSHA1";

    /**
     * 生成TOTP动态口令
     * @param base32Secret 基于Base32编码的共享密钥（TOTP标准用Base32存储密钥）
     * @param timestamp 当前时间戳（毫秒），如System.currentTimeMillis()
     * @return 6位动态口令
     * @throws NoSuchAlgorithmException 算法不支持
     * @throws InvalidKeyException 密钥无效
     */
    public static String generateTotp(String base32Secret, long timestamp) throws NoSuchAlgorithmException, InvalidKeyException {
        // 步骤1：将时间戳转换为30秒步长的时间窗口值（Unix时间戳秒数 / 30）
        long timeWindow = timestamp / 1000 / TIME_STEP;
        // 步骤2：将时间窗口值转换为8字节大端序二进制（RFC要求）
        byte[] timeWindowBytes = ByteBuffer.allocate(8)
                .order(ByteOrder.BIG_ENDIAN)
                .putLong(timeWindow)
                .array();

        // 步骤3：解码Base32格式的共享密钥（转为原始二进制）
        byte[] secretBytes = decodeBase32(base32Secret);

        // 步骤4：计算HMAC-SHA1哈希
        Mac hmac = Mac.getInstance(HMAC_ALGORITHM);
        SecretKeySpec keySpec = new SecretKeySpec(secretBytes, HMAC_ALGORITHM);
        hmac.init(keySpec);
        byte[] hmacResult = hmac.doFinal(timeWindowBytes); // 20字节的SHA1哈希结果

        // 步骤5：截断哈希结果（核心步骤）
        int offset = hmacResult[hmacResult.length - 1] & 0x0F; // 取最后1字节的低4位作为偏移量
        // 从偏移量开始取4字节，转换为32位无符号整数（去掉最高位避免负数）
        int truncatedHash = ((hmacResult[offset] & 0x7F) << 24)
                | ((hmacResult[offset + 1] & 0xFF) << 16)
                | ((hmacResult[offset + 2] & 0xFF) << 8)
                | (hmacResult[offset + 3] & 0xFF);

        // 步骤6：取模生成固定长度的数字码
        int totpCode = truncatedHash % (int) Math.pow(10, CODE_DIGITS);
        // 补前导0（确保6位，如123 → 000123）
        return String.format("%0" + CODE_DIGITS + "d", totpCode);
    }

    /**
     * 验证TOTP动态口令
     * @param base32Secret 共享密钥（Base32）
     * @param inputCode 用户输入的动态码
     * @param timestamp 当前时间戳（毫秒）
     * @param window 容错窗口（如1表示允许±1个时间步长，即前后30秒）
     * @return 是否验证通过
     */
    public static boolean verifyTotp(String base32Secret, String inputCode, long timestamp, int window) {
        // 遍历容错窗口内的所有时间步长，只要有一个匹配则验证通过
        for (int i = -window; i <= window; i++) {
            long adjustedTimestamp = timestamp + (i * TIME_STEP * 1000);
            try {
                String generatedCode = generateTotp(base32Secret, adjustedTimestamp);
                if (generatedCode.equals(inputCode)) {
                    return true;
                }
            } catch (Exception e) {
                continue;
            }
        }
        return false;
    }

    /**
     * Base32解码（TOTP密钥通常用Base32编码存储，Java自带库无Base32，手动实现极简版）
     * 注：生产环境可使用Apache Commons Codec的Base32类，此处为演示核心逻辑
     */
    private static byte[] decodeBase32(String base32) {
        // Base32编码表（RFC 4648标准）
        String base32Table = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
        base32 = base32.toUpperCase().replaceAll("=", ""); // 移除填充符=

        int bitLength = base32.length() * 5;
        int byteLength = bitLength / 8;
        ByteBuffer buffer = ByteBuffer.allocate(byteLength);
        int bufferBits = 0;
        int bufferValue = 0;

        for (char c : base32.toCharArray()) {
            int value = base32Table.indexOf(c);
            if (value == -1) {
                throw new IllegalArgumentException("无效的Base32字符：" + c);
            }
            // 逐位填充
            bufferValue = (bufferValue << 5) | value;
            bufferBits += 5;
            // 凑够8位则写入字节
            if (bufferBits >= 8) {
                bufferBits -= 8;
                buffer.put((byte) (bufferValue >> bufferBits));
            }
        }
        return buffer.array();
    }

    /**
     * 生成Base32格式的共享密钥（用于初始化TOTP绑定）
     * 注：生产环境建议用SecureRandom生成16字节随机数，再转Base32
     */
    public static String generateBase32Secret() {
        // 简化示例：生成16字节随机数（符合RFC推荐的密钥长度）
        byte[] secret = new byte[16];
        new java.security.SecureRandom().nextBytes(secret);
        // Base32编码（极简版，生产用Apache Commons Codec）
        String base32Table = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
        StringBuilder sb = new StringBuilder();
        int bits = 0, value = 0;
        for (byte b : secret) {
            value = (value << 8) | (b & 0xFF);
            bits += 8;
            while (bits >= 5) {
                bits -= 5;
                sb.append(base32Table.charAt((value >> bits) & 0x1F));
            }
        }
        // 补填充符=，使长度为8的倍数
        while (sb.length() % 8 != 0) {
            sb.append('=');
        }
        return sb.toString();
    }

    // 测试示例
    public static void main(String[] args) throws Exception {
        // 1. 生成共享密钥（Base32格式）
        String base32Secret = generateBase32Secret();
        System.out.println("Base32共享密钥：" + base32Secret);

        // 2. 生成当前时间的TOTP码
        long now = System.currentTimeMillis();
        String totpCode = generateTotp(base32Secret, now);
        System.out.println("当前TOTP码：" + totpCode);

        // 3. 验证TOTP码（容错窗口1，即±30秒）
        boolean isValid = verifyTotp(base32Secret, totpCode, now, 1);
        System.out.println("验证结果：" + isValid); // 输出true

        // 验证错误码
        boolean isInvalid = verifyTotp(base32Secret, "123456", now, 1);
        System.out.println("错误码验证结果：" + isInvalid); // 输出false
    }
}

/**
 * 产环境优化建议
 * 替换 Base32 实现：示例中手动实现的 Base32 解码 / 编码仅作演示，生产环境建议使用org.apache.commons.codec.binary.Base32（Apache Commons Codec），避免手动实现的 bug；
 * 异常处理：补充IllegalArgumentException（无效密钥）、NoSuchAlgorithmException（算法不支持）的全局捕获；
 * 算法扩展：如需支持 SHA256/SHA512，只需修改HMAC_ALGORITHM为HmacSHA256/HmacSHA512，并调整哈希结果长度（SHA256 是 32 字节，SHA512 是 64 字节）；
 * 密钥安全：Base32 密钥需加密存储（如 AES），禁止明文入库；
 * 防重验证：验证通过后，需将「用户名 + 验证码 + 时间窗口」存入 Redis 标记为已使用（过期时间 60 秒），避免重放攻击。
 */