package community.api.repository;

import community.api.entity.Post;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {

    @EntityGraph(attributePaths = "user")
    List<Post> findAllByDeletedAtIsNull();

    Optional<Post> findByIdAndDeletedAtIsNull(Long postId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE Post p
        SET p.viewCount = p.viewCount + 1
        WHERE p.id = :postId
          AND p.deletedAt IS NULL
    """)
    int increaseViewCount(@Param("postId") Long postId);
}