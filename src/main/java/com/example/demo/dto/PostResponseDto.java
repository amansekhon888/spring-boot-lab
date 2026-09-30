package com.example.demo.dto;

import java.time.LocalDateTime;
import java.util.List;

/** Public post representation; comment text is populated only by the single-post fetch-join use case. */
public class PostResponseDto {
    private Long id;
    private String title;
    private String content;
    private String author;
    private LocalDateTime createdAt;
    private List<String> comments;

    public PostResponseDto(Long id, String title, String content, String author, LocalDateTime createdAt) {
        this(id, title, content, author, createdAt, List.of());
    }

    public PostResponseDto(Long id, String title, String content, String author, LocalDateTime createdAt,
                           List<String> comments) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.author = author;
        this.createdAt = createdAt;
        this.comments = comments;
    }

    // Getters
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getAuthor() { return author; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<String> getComments() { return comments; }
}