package com.example.demo.model;

import jakarta.persistence.*;

/** A comment belongs to one post; this child entity makes the detail fetch-join example concrete. */
@Entity
@Table(name = "post_comments")
public class PostComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1000)
    private String body;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    protected PostComment() {
    }

    public PostComment(String body, Post post) {
        this.body = body;
        this.post = post;
    }

    public Long getId() {
        return id;
    }

    public String getBody() {
        return body;
    }

    public Post getPost() {
        return post;
    }
}