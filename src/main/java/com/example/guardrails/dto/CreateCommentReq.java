package com.example.guardrails.dto;

import lombok.Data;

@Data
public class CreateCommentReq {

    private Long authorId;
    private String authorType;
    private String content;
    private int depthLevel;

}
