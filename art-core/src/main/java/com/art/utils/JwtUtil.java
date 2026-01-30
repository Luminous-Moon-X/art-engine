package com.art.utils;

import com.art.exception.ArtException;
import com.art.constants.ArtErrorMessageConstants;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.MacAlgorithm;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * JWT工具类
 *
 * @author Luminous.X
 * @since 1.0.0
 */
@Slf4j
@SuppressWarnings("unused")
public final class JwtUtil {
    /**
     * 自定义私钥
     */
    private static final String SECRET_KEY = "s7a727asd7xsd3412A!312A!312A!312A!3";

    /**
     * 初始化负载
     *
     * @param userId 用户ID
     * @return 负载Map
     */
    private static Map<String, Object> initClaims(String userId) {
        Date expiresDate;
        try {
            expiresDate = new SimpleDateFormat("yyyy-MM-dd").parse("9999-12-31");
        } catch (ParseException e) {
            throw new ArtException(String.format(ArtErrorMessageConstants.JWT_ERROR_INFO, e.getMessage()));
        }
        Map<String, Object> claims = new HashMap<>();
        claims.put("iss", "Art");
        claims.put("sub", userId);
        claims.put("iat", Instant.now().toEpochMilli());
        claims.put("exp", expiresDate);
        claims.put("aud", "Art");
        claims.put("jti", UUID.randomUUID().toString());
        return claims;
    }

    /**
     * 初始化生成Token
     *
     * @param userId 用户ID
     * @return Token
     */
    public static String generateToken(String userId) {
        // 获取负载
        Map<String, Object> claims = initClaims(userId);
        // 指定加密算法
        MacAlgorithm algorithm = Jwts.SIG.HS256;
        // 密钥实例
        SecretKey secretKey = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
        // 生成token
        return Jwts.builder().claims(claims).signWith(secretKey, algorithm).compact();
    }

}