package community.api.repository;

import community.api.dto.PostSearchDto;
import community.api.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PostRepositoryCustom {
    Page<Post> searchPosts(
            PostSearchDto search,
            Pageable pageable
    );
}
