package community.api.service;

import community.api.dto.CommentResponseDto;
import community.api.entity.Category;
import community.api.entity.Comment;
import community.api.entity.Post;
import community.api.entity.User;
import community.api.repository.CommentRepository;
import community.api.repository.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @InjectMocks
    private CommentService commentService;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @Test
    @DisplayName("댓글을 삭제하면 deletedAt이 설정된다")
    void deleteComment_Success() {
        Long userId = 1L;
        Long postId = 10L;
        Long commentId = 100L;

        User owner = user(userId);
        Post post = post(postId, owner);
        Comment comment = comment(commentId, owner, post);

        when(postRepository.findByIdAndDeletedAtIsNull(postId))
                .thenReturn(Optional.of(post));

        when(commentRepository.findByIdAndDeletedAtIsNull(commentId))
                .thenReturn(Optional.of(comment));

        assertNull(comment.getDeletedAt());

        commentService.deleteComment(userId, postId, commentId);

        assertNotNull(comment.getDeletedAt());

        verify(postRepository).findByIdAndDeletedAtIsNull(postId);
        verify(commentRepository).findByIdAndDeletedAtIsNull(commentId);
        verify(commentRepository, never()).deleteById(anyLong());
        verify(commentRepository, never()).delete(any(Comment.class));
    }

    @Test
    @DisplayName("댓글 목록 조회 시 삭제된 댓글은 제외된다")
    void getComments_ExcludeDeletedComment() {
        Long postId = 10L;
        Long commentId = 100L;

        User owner = user(1L);
        Post post = post(postId, owner);
        Comment activeComment = comment(commentId, owner, post);

        when(postRepository.findByIdAndDeletedAtIsNull(postId))
                .thenReturn(Optional.of(post));

        when(commentRepository
                .findAllByPost_IdAndDeletedAtIsNull(postId))
                .thenReturn(List.of(activeComment));

        List<CommentResponseDto> result = commentService.getComments(postId);

        assertEquals(1, result.size());

        assertEquals(
                activeComment.getId(),
                result.get(0).getCommentId()
        );

        verify(postRepository).findByIdAndDeletedAtIsNull(postId);
        verify(commentRepository).findAllByPost_IdAndDeletedAtIsNull(postId);
    }

    private User user(Long id) {
        User user = new User(
                "elin@example.com",
                "encodedPassword!",
                "엘린",
                "profile1.png"
        );

        ReflectionTestUtils.setField(user, "id", id);

        return user;
    }

    private Post post(Long id, User owner) {
        Post post = new Post(
                owner,
                "게시글 제목",
                "게시글 내용",
                "image.png",
                Category.BACKEND
        );

        ReflectionTestUtils.setField(post, "id", id);

        return post;
    }

    private Comment comment(
            Long id,
            User owner,
            Post post
    ) {
        Comment comment = new Comment(
                post,
                owner,
                "댓글 내용"
        );

        ReflectionTestUtils.setField(comment, "id", id);

        return comment;
    }
}