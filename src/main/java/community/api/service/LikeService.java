package community.api.service;

import community.api.dto.LikeResponseDto;
import community.api.entity.Like;
import community.api.entity.Post;
import community.api.entity.User;
import community.api.exception.ConflictException;
import community.api.exception.NotFoundException;
import community.api.exception.UnauthorizedException;
import community.api.repository.LikeRepository;
import community.api.repository.PostRepository;
import community.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LikeService {

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    public LikeResponseDto addLike(Long userId, Long postId) {
        if (userId == null) {
            throw new UnauthorizedException("unauthorized_error");
        }

        Post post = postRepository.findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() -> new NotFoundException("post_not_found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("user_not_found"));

        if (likeRepository.existsByPost_IdAndUser_Id(postId, userId)) {
            throw new ConflictException("already_liked");
        }
        Like like = new Like(post, user);
        likeRepository.save(like);

        int likeCount = (int) likeRepository.countByPost_Id(postId);

        return new LikeResponseDto(postId, userId, likeCount);
    }
    @Transactional
    public void deleteLike(Long userId, Long postId) {
        if (userId == null) {
            throw new UnauthorizedException("unauthorized_error");
        }
        postRepository.findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() -> new NotFoundException("post_not_found"));
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("user_not_found"));

        if (!likeRepository.existsByPost_IdAndUser_Id(postId, userId)) {
            throw new NotFoundException("like_not_found");
        }

        likeRepository.deleteByPost_IdAndUser_Id(postId, userId);
    }
}