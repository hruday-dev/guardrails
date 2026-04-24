package com.example.guardrails.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class NotificationScheduler {

    private final NotificationRedisService redisService;

    public NotificationScheduler(NotificationRedisService redisService) {
        this.redisService = redisService;
    }

    @Scheduled(fixedRate = 300000,initialDelay = 10000) // 5 minutes
    public void processNotifications() {

        Set<String> users = redisService.getUsersWithPending();

        if (users == null || users.isEmpty()) return;

        for (String userIdStr : users) {

            Long userId = Long.parseLong(userIdStr);

            List<String> messages = redisService.getNotifications(userId);

            if (messages == null || messages.isEmpty()) continue;

            int count = messages.size();

            String summary =
                    "Summarized Push Notification: "
                            + messages.get(0)
                            + " and " + (count - 1)
                            + " others interacted with your posts.";

            System.out.println(summary);

            // cleanup
            redisService.clearNotifications(userId);
            redisService.removeUser(userId);
        }
    }
}
