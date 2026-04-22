package com.example.guardrails.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // =============================
    // 🔥 VIRALITY SCORE
    // =============================
    public Long incrementVirality(Long postId, int points) {
        String key = "post:" + postId + ":virality_score";
        return redisTemplate.opsForValue().increment(key, points);
    }

    // =============================
    // 🤖 BOT COUNT (Atomic)
    // =============================
    public Long incrementBotCount(Long postId) {
        String key = "post:" + postId + ":bot_count";
        return redisTemplate.opsForValue().increment(key);
    }

    // =============================
    // ⏱ COOLDOWN
    // =============================
    public boolean isCooldownActive(Long botId, Long humanId) {
        String key = "cooldown:bot_" + botId + ":human_" + humanId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public void setCooldown(Long botId, Long humanId) {
        String key = "cooldown:bot_" + botId + ":human_" + humanId;
        redisTemplate.opsForValue().set(key, "1", Duration.ofMinutes(10));
    }
}
