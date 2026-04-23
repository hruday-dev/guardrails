package com.example.guardrails.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@Service
public class NotificationRedisService {

    private final StringRedisTemplate redis;

    public NotificationRedisService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public boolean hasRecentNotification(Long userId) {
        return Boolean.TRUE.equals(redis.hasKey("user:" + userId + ":notif_cooldown"));
    }

    public void setNotificationCooldown(Long userId) {
        redis.opsForValue().set(
                "user:" + userId + ":notif_cooldown",
                "1",
                Duration.ofMinutes(15)
        );
    }

    public void queueNotification(Long userId, String message) {
        String listKey = "user:" + userId + ":pending_notifs";

        redis.opsForList().rightPush(listKey, message);

        // track users with pending notifications
        redis.opsForSet().add("notif:users_with_pending", userId.toString());
    }

    public List<String> getNotifications(Long userId) {
        return redis.opsForList().range("user:" + userId + ":pending_notifs", 0, -1);
    }

    public void clearNotifications(Long userId) {
        redis.delete("user:" + userId + ":pending_notifs");
    }

    public Set<String> getUsersWithPending() {
        return redis.opsForSet().members("notif:users_with_pending");
    }

    public void removeUser(Long userId) {
        redis.opsForSet().remove("notif:users_with_pending", userId.toString());
    }
}