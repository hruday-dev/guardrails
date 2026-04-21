package com.example.guardrails.dto;

import lombok.Data;

@Data
public class CreateCommentReqDTO{

    private Long authorId;
    private String authorType;
    private String content;
    private int depthLevel;

}
