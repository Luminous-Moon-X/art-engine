package com.art.storage;

import com.art.config.AuthConfiguration;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

/**
 * OSS Secret Key 加解密工具。
 *
 * <p>Secret Key 使用 AES/GCM 加密后入库，接口响应中不会回显。</p>
 *
 * @author Luminous.X
 * @since 1.3.0
 */
@Component
public class OssSecretCrypto {

    /**
     * 加密内容前缀
     */
    private static final String ENCRYPT_PREFIX = "enc:";

    /**
     * GCM IV长度
     */
    private static final int IV_LENGTH = 12;

    /**
     * 认证配置
     */
    private final AuthConfiguration authConfiguration;

    /**
     * 构造函数。
     *
     * @param authConfiguration 认证配置
     */
    public OssSecretCrypto(AuthConfiguration authConfiguration) {
        this.authConfiguration = authConfiguration;
    }

    /**
     * 加密明文。
     *
     * @param plainText 明文
     * @return 密文
     */
    public String encrypt(String plainText) {
        if (plainText == null || plainText.isBlank()) {
            return "";
        }
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, buildKey());
            byte[] iv = cipher.getIV();
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            ByteBuffer buffer = ByteBuffer.allocate(iv.length + encrypted.length);
            buffer.put(iv);
            buffer.put(encrypted);
            return ENCRYPT_PREFIX + Base64.getEncoder().encodeToString(buffer.array());
        } catch (Exception e) {
            throw new IllegalStateException("OSS Secret Key 加密失败", e);
        }
    }

    /**
     * 解密存储值。
     *
     * @param storedValue 存储值
     * @return 明文
     */
    public String decrypt(String storedValue) {
        if (storedValue == null || storedValue.isBlank()) {
            return "";
        }
        if (!storedValue.startsWith(ENCRYPT_PREFIX)) {
            return storedValue;
        }
        try {
            byte[] payload = Base64.getDecoder().decode(storedValue.substring(ENCRYPT_PREFIX.length()));
            byte[] iv = Arrays.copyOfRange(payload, 0, IV_LENGTH);
            byte[] encrypted = Arrays.copyOfRange(payload, IV_LENGTH, payload.length);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, buildKey(), new GCMParameterSpec(128, iv));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("OSS Secret Key 解密失败", e);
        }
    }

    /**
     * 根据平台密钥派生AES密钥。
     *
     * @return AES密钥
     * @throws Exception 密钥派生异常
     */
    private SecretKeySpec buildKey() throws Exception {
        byte[] keyBytes = MessageDigest.getInstance("SHA-256")
                .digest(authConfiguration.getSecretKey().getBytes(StandardCharsets.UTF_8));
        return new SecretKeySpec(keyBytes, "AES");
    }
}
