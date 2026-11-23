package ru.mirea.sport.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
    String secretBase64,
    String issuer,
    long accessMinutes,
    long refreshDays
) {}