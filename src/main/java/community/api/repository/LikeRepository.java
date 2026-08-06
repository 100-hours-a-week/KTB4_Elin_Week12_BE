package community.api.repository;

import community.api.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LikeRepository extends JpaRepository<Like, Long> {

    boolean existsByPost_IdAndUser_Id(Long postId, Long userId);
    long countByPost_Id(Long postId);

    void deleteByPost_IdAndUser_Id(Long postId, Long userId);

    @Query("""
        select l
        from Like l
        join fetch l.post p
        join fetch p.user
        where l.user.id = :userId
          and p.deletedAt is null
        order by l.createdAt desc, l.id desc
    """)
        List<Like> findLikedPosts(
                @Param("userId") Long userId
        );

    @Query("""
        select l.post.id
        from Like l
        where l.user.id = :userId
          and l.post.id in :postIds
    """)
    List<Long> findLikedPostIds(
            @Param("userId") Long userId,
            @Param("postIds") List<Long> postIds
    );

    @Query(value = """
        select
            post_id as postId,
            count(like_id) as countValue
        from likes
        where post_id in (:postIds)
        group by post_id
    """, nativeQuery = true)
        List<PostCountProjection> countByPostIds(@Param("postIds") List<Long> postIds);

}