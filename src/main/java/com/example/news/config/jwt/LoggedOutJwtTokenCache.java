package com.example.news.config.jwt;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.jodah.expiringmap.ExpiringMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@Getter
public class LoggedOutJwtTokenCache {

    private final ExpiringMap<String, String> tokenBlackList;
    private final JWTUtil jwtUtil;

    @Autowired
    public LoggedOutJwtTokenCache(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
        this.tokenBlackList = ExpiringMap.builder()
                .variableExpiration()
                .maxSize(1000)
                .build();
    }

    public void putTokenInBlackList(String token) {
        if (tokenBlackList.containsKey(token)) {
            log.info(String.format("Log out token for user [%s] is already present in the cache", jwtUtil.extractUsername(token)));

        } else {
            Date tokenExpiryDate = jwtUtil.extractExpiration(token);
            long ttlForToken = getTTLForToken(tokenExpiryDate);
            log.info("Logout token cached " + jwtUtil.extractUsername(token), ttlForToken, tokenExpiryDate);
            tokenBlackList.put(token, jwtUtil.extractUsername(token), ttlForToken, TimeUnit.SECONDS);
        }
    }


    private long getTTLForToken(Date date) {
        long secondAtExpiry = date.toInstant().getEpochSecond();
        long secondAtLogout = Instant.now().getEpochSecond();
        return Math.max(0, secondAtExpiry - secondAtLogout);
    }
}