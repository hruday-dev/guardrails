package com.example.guardrails.repo;

import com.example.guardrails.entity.Bot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BotRepo extends JpaRepository<Bot,Long> {
}
