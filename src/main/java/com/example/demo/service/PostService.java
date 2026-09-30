package com.example.demo.service;

import com.example.demo.dto.PostRequestDto;
import com.example.demo.dto.PostResponseDto;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Post;
import com.example.demo.model.PostComment;
import com.example.demo.repository.PostRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/** Coordinates post use cases; Spring injects the repository so persistence stays outside the controller. */
@Service
public class PostService {

    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @CacheEvict(value = "postsById", allEntries = true)
    public PostResponseDto createPost(PostRequestDto dto) {
        Post post = new Post(dto.getTitle(), dto.getContent(), dto.getAuthor());
        Post saved = postRepository.save(post);
        return mapToDto(saved);
    }

    public Page<PostResponseDto> getAllPosts(String title, String author, Pageable pageable) {
        // Add only supplied filters, letting the database filter and page before rows reach the JVM.
        Specification<Post> filters = (root, query, builder) -> builder.conjunction();
        if (StringUtils.hasText(title)) {
            filters = filters.and((root, query, builder) ->
                    builder.like(builder.lower(root.get("title")), "%" + title.trim().toLowerCase() + "%"));
        }
        if (StringUtils.hasText(author)) {
            filters = filters.and((root, query, builder) ->
                    builder.like(builder.lower(root.get("author")), "%" + author.trim().toLowerCase() + "%"));
        }
        return postRepository.findAll(filters, pageable).map(this::mapToDto);
    }

    @Cacheable(value = "postsById", key = "#id")
    public PostResponseDto getPostById(Long id) {
        // This use case needs comments, so the repository loads them with one JOIN FETCH query.
        Post post = postRepository.findByIdWithComments(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with ID: " + id));
        return new PostResponseDto(post.getId(), post.getTitle(), post.getContent(), post.getAuthor(),
            post.getCreatedAt(), post.getComments().stream().map(comment -> comment.getBody()).toList());
    }

    @Transactional
    @CacheEvict(value = "postsById", key = "#id")
    public void addComment(Long id, String body) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with ID: " + id));
        post.getComments().add(new PostComment(body, post)); // Cascade persists the new child with its parent.
    }

    @CacheEvict(value = "postsById", key = "#id")
    public void deletePost(Long id) {
        if (!postRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cannot delete. Post not found with ID: " + id);
        }
        postRepository.deleteById(id);
    }

    private PostResponseDto mapToDto(Post post) {
        return new PostResponseDto(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getAuthor(),
                post.getCreatedAt());
    }
}