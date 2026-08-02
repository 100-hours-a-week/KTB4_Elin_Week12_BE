package community.api.repository;

import community.api.dto.PostSearchDto;
import community.api.entity.Post;

import java.util.List;

public interface PostRepositoryCustom {
    List<Post> searchPosts(PostSearchDto search);
}
