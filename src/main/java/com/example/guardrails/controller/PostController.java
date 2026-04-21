package com.example.guardrails.controller;

import com.example.guardrails.dto.CreateCommentReqDTO;
import com.example.guardrails.dto.CreatePostReq;
import com.example.guardrails.entity.Comment;
import com.example.guardrails.entity.Post;
import com.example.guardrails.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    @Autowired
    PostService service;

    @PostMapping
    public Post createNewPost(@RequestBody CreatePostReq request) {
        return service.createPost(request);
    }

    @PostMapping("/{postId}/comments")
    public Comment addComment(@PathVariable Long postId, @RequestBody CreateCommentReqDTO request) {
        return service.addComment(postId, request);
    }

    @PostMapping("/{postId}/like")
    public String likePost(@PathVariable Long id){
        return service.likePost(id);
    }
}
