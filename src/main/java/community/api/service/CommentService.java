package community.api.service;

import community.api.dto.CommentRequestDto;
import community.api.dto.CommentResponseDto;
import community.api.entity.Comment;
import community.api.entity.Post;
import community.api.entity.User;
import community.api.exception.ForbiddenException;
import community.api.exception.NotFoundException;
import community.api.exception.UnauthorizedException;
import community.api.repository.CommentRepository;
import community.api.repository.PostRepository;
import community.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    public CommentResponseDto createComment(
            Long userId,
            Long postId,
            CommentRequestDto request
    ) {
        if (userId == null) {
            throw new UnauthorizedException("unauthorized_error");
        }
        Post post = postRepository.findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() -> new NotFoundException("post_not_found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("user_not_found"));

        Comment comment = new Comment(
                post,
                user,
                request.getContent()
        );

        Comment savedComment = commentRepository.save(comment);

        return CommentResponseDto.from(savedComment);
    }

    public List<CommentResponseDto> getComments(Long postId) {
        postRepository.findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() -> new NotFoundException("post_not_found"));

        return commentRepository.findAllByPost_IdAndDeletedAtIsNull(postId)
                .stream()
                .map(CommentResponseDto::from)
                .toList();
    }
    @Transactional
    public CommentResponseDto updateComment(
            Long userId,
            Long postId,
            Long commentId,
            CommentRequestDto request
    ) {
        if (userId == null) {
            throw new UnauthorizedException("unauthorized_error");
        }

        postRepository.findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() -> new NotFoundException("post_not_found"));

        Comment comment = commentRepository.findByIdAndDeletedAtIsNull(commentId)
                .orElseThrow(() -> new NotFoundException("comment_not_found"));

        if (!comment.getPostId().equals(postId)) {
            throw new NotFoundException("comment_not_found");
        }

        if (!comment.getUserId().equals(userId)) {
            throw new ForbiddenException("forbidden_error");
        }

        comment.update(request.getContent());
        commentRepository.flush();

        return CommentResponseDto.from(comment);

    }
    @Transactional
    public void deleteComment(Long userId, Long postId, Long commentId) {
        if (userId == null) {
            throw new UnauthorizedException("unauthorized_error");
        }

        postRepository.findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() -> new NotFoundException("post_not_found"));

        Comment comment = commentRepository.findByIdAndDeletedAtIsNull(commentId)
                .orElseThrow(() -> new NotFoundException("comment_not_found"));

        if (!comment.getPostId().equals(postId)) {
            throw new NotFoundException("comment_not_found");
        }

        if (!comment.getUserId().equals(userId)) {
            throw new ForbiddenException("forbidden_error");
        }

        comment.delete();
    }
}