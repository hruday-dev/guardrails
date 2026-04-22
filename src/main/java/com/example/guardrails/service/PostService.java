package com.example.guardrails.service;

import com.example.guardrails.dto.CreateCommentReq;
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
    @Autowired
    RedisService redisService;

    public Post createPost(CreatePostReq request) {
        Post post = new Post();
        post.setAuthorId(request.getAuthorId());
        post.setAuthorType(AuthorType.valueOf(request.getAuthorType()));
        post.setContent(request.getContent());

        return postRepo.save(post);
    }

    public Comment addComment(Long id,  CreateCommentReq request) {
        Post post = postRepo.findById(id).orElseThrow(() -> new PostNotFound("Post not Found"));

        if (request.getDepthLevel() > 20) {
            throw new RuntimeException("Max depth exceeded");
        }
        boolean isBot = request.getAuthorType().equals("BOT");

        if (isBot) {

            Long botId = request.getAuthorId();
            Long humanId = post.getAuthorId(); // assuming post owner is human

            // 3️⃣ Cooldown check
            if (redisService.isCooldownActive(botId, humanId)) {
                throw new RuntimeException("Cooldown active");
            }

            // 4️⃣ Horizontal Cap (Atomic)
            Long count = redisService.incrementBotCount(post.getId());

            if (count > 100) {
                throw new RuntimeException("429 Too Many Bot Replies");
            }

            // 5️⃣ Set cooldown
            redisService.setCooldown(botId, humanId);
        }
        Comment comment = new Comment();
        comment.setPostId(post.getId());
        comment.setAuthorId(request.getAuthorId());
        comment.setAuthorType(AuthorType.valueOf(request.getAuthorType()));
        comment.setContent(request.getContent());
        comment.setDepthLevel(request.getDepthLevel());

        if (isBot) {
            redisService.incrementVirality(post.getId(), 1);
        } else {
            redisService.incrementVirality(post.getId(), 50);
        }

        return commentRepo.save(comment);
    }

    public Long likePost(Long id) {
        if(!postRepo.existsById(id)){
            throw new RuntimeException("Post not Found");
        }
        return redisService.incrementVirality( id, 20);
    }
}
