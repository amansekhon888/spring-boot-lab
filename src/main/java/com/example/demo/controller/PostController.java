package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.PostRequestDto;
import com.example.demo.dto.PostCommentRequest;
import com.example.demo.dto.PostResponseDto;
import com.example.demo.service.PostService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** HTTP boundary for posts: it validates/routs requests and delegates business work to PostService. */
@RestController
@RequestMapping("/api/posts")
public class PostController {

    private static final Logger log = LoggerFactory.getLogger(PostController.class);
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PostResponseDto>> createPost(@Valid @RequestBody PostRequestDto dto) {
        log.info("Creating new post with title: {}", dto.getTitle());
        PostResponseDto created = postService.createPost(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Post created successfully", created));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<PostResponseDto>>> getAllPosts(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("Fetching posts with pagination and optional filters");
        Page<PostResponseDto> posts = postService.getAllPosts(title, author, pageable);
        return ResponseEntity.ok(ApiResponse.success("Posts retrieved successfully", posts));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PostResponseDto>> getPostById(@PathVariable Long id) {
        log.info("Fetching post ID: {}", id);
        PostResponseDto post = postService.getPostById(id);
        return ResponseEntity.ok(ApiResponse.success("Post retrieved successfully", post));
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<ApiResponse<Void>> addComment(
            @PathVariable Long id, @Valid @RequestBody PostCommentRequest request) {
        postService.addComment(id, request.body());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Comment added successfully", null));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // Method security protects this operation even if another route calls it later.
    public ResponseEntity<ApiResponse<Void>> deletePost(@PathVariable Long id) {
        log.info("Deleting post ID: {}", id);
        postService.deletePost(id);
        return ResponseEntity.ok(ApiResponse.success("Post deleted successfully", null));
    }
}