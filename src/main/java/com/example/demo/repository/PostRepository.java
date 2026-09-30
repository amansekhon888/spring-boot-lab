package com.example.demo.repository;

import com.example.demo.model.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/** Database access boundary; Spring Data generates routine CRUD implementations from this interface. */
@Repository
public interface PostRepository extends JpaRepository<Post, Long>, JpaSpecificationExecutor<Post> {

	// Fetch comments in this one detail query instead of one extra query per post (the N+1 pattern).
	@Query("select distinct p from Post p left join fetch p.comments where p.id = :id")
	Optional<Post> findByIdWithComments(@Param("id") Long id);

	// Native SQL is useful for database-specific tuning; keep the count query for correct Page metadata.
	@Query(value = "select * from posts order by created_at desc",
			countQuery = "select count(*) from posts", nativeQuery = true)
	Page<Post> findNewestPostsNative(Pageable pageable);
}