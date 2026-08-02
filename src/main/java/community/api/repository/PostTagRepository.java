package community.api.repository;

import community.api.entity.PostTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostTagRepository extends JpaRepository<PostTag, Long> {

    List<PostTag> findAllByPost_Id(Long postId);

    void deleteAllByPost_Id(Long postId);
}