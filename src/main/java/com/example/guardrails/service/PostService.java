package com.example.guardrails.service;

import com.example.guardrails.dto.CreateCommentReqDTO;
import com.example.guardrails.dto.CreatePostReq;
import com.example.guardrails.entity.AuthorType;
import com.example.guardrails.entity.Comment;
import com.example.guardrails.entity.Post;
import com.example.guardrails.exceptions.PostNotFound;
import com.example.guardrails.repo.CommentRepo;
import com.example.guardrails.repo.PostRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PostService {
    @Autowired
    PostRepo postRepo;
    @Autowired
    CommentRepo commentRepo;

    public Post createPost(CreatePostReq request) {
        Post post = new Post();
        post.setAuthorId(request.getAuthorId());
        post.setAuthorType(AuthorType.valueOf(request.getAuthorType()));
        post.setContent(request.getContent());

        return postRepo.save(post);
    }

    public Comment addComment(Long id,  CreateCommentReqDTO request) {
        Post post = postRepo.findById(id).orElseThrow(() -> new PostNotFound("Post not Found"));
        Mapper.toEntity(post);

        return null;
    }

    public String likePost(Long id) {
        if(!postRepo.existsById(id)){
            throw new RuntimeException("Post not Found");
        }
        return "Post Liked" + id;
    }
}
