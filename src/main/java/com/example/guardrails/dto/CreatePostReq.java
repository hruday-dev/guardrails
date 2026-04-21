package com.example.guardrails.dto;

import lombok.Data;

@Data
public class CreatePostReq {
    private Long authorId;
    private String authorType;
    private String content;
}
