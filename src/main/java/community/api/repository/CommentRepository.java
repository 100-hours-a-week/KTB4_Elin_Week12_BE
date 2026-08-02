package community.api.repository;

import community.api.entity.Comment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @EntityGraph(attributePaths = "user")
    List<Comment> findAllByPost_IdAndDeletedAtIsNull(Long postId);
    Optional<Comment> findByIdAndDeletedAtIsNull(Long commentId);
    long countByPost_IdAndDeletedAtIsNull(Long postId);

    @Query(value = """
    select
            post_id as postId,
            count(comment_id) as countValue
        from comments
        where post_id in (:postIds)
        and deleted_at is null
        group by post_id
    """, nativeQuery = true)
        List<PostCountProjection> countByPostIds(@Param("postIds") List<Long> postIds);
}