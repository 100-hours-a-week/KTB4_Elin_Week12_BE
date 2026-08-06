package community.api.repository;

import community.api.entity.PostTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostTagRepository
        extends JpaRepository<PostTag, Long> {

    List<PostTag> findAllByPost_Id(Long postId);

    @Query("""
        select pt
        from PostTag pt
        join fetch pt.post p
        join fetch pt.tag
        where p.id in :postIds
    """)
    List<PostTag> findAllWithTagsByPostIds(
            @Param("postIds") List<Long> postIds
    );

    void deleteAllByPost_Id(Long postId);
}