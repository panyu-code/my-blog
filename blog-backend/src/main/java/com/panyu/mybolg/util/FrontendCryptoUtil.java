package com.panyu.mybolg.util;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 前端密码解密工具类
 * 与前端crypto.js中的SimpleCrypto对应
 */
public class FrontendCryptoUtil {

    private static final String KEY = "myBlogSecretKey";

    /**
     * 解密前端传来的加密密码
     * 
     * @param encryptedData 前端加密后的数据（Base64格式）
     * @return 解密后的原始密码
     */
    public static String decrypt(String encryptedData) {
        if (encryptedData == null || encryptedData.isEmpty()) {
            return "";
        }
        
        try {
            // Base64解码
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedData);
            String decoded = new String(decodedBytes, StandardCharsets.UTF_8);
            
            // URL解码
            String urlDecoded = URLDecoder.decode(decoded, StandardCharsets.UTF_8);
            
            // XOR解密
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < urlDecoded.length(); i++) {
                char c = (char) (urlDecoded.charAt(i) ^ KEY.charAt(i % KEY.length()));
                result.append(c);
            }
            
            return result.toString();
        } catch (Exception e) {
            // 如果解密失败，返回原始字符串（兼容未加密的情况）
            return encryptedData;
        }
    }
}
