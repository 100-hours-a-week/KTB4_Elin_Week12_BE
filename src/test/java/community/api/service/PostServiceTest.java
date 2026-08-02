package community.api.service;

import community.api.dto.PostRequestDto;
import community.api.dto.PostResponseDto;
import community.api.dto.PostSearchDto;
import community.api.entity.Category;
import community.api.entity.Post;
import community.api.entity.User;
import community.api.exception.ForbiddenException;
import community.api.exception.NotFoundException;
import community.api.repository.CommentRepository;
import community.api.repository.LikeRepository;
import community.api.repository.PostCountProjection;
import community.api.repository.PostRepository;
import community.api.repository.PostTagRepository;
import community.api.repository.TagRepository;
import community.api.repository.UserRepository;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @InjectMocks
    private PostService postService;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private PostTagRepository postTagRepository;

    @Test
    @DisplayName("다른 사용자의 게시글을 수정하면 예외가 발생한다")
    void updatePost_OtherUser_ThrowForbiddenException() {
        PostRequestDto request = postRequest(
                "제목 수정",
                "내용 수정",
                "edit_image.png",
                Category.BACKEND
        );

        User owner = user(1L);

        Post post = new Post(
                owner,
                "제목",
                "내용",
                "image.png",
                Category.BACKEND
        );

        when(postRepository.findByIdAndDeletedAtIsNull(1L))
                .thenReturn(Optional.of(post));

        ForbiddenException exception = assertThrows(
                ForbiddenException.class,
                () -> postService.updatePost(2L, 1L, request)
        );

        assertEquals("forbidden_error", exception.getCode());
        assertEquals("제목", post.getTitle());
        assertEquals("내용", post.getContent());
        assertEquals("image.png", post.getContentImage());
        assertEquals(Category.BACKEND, post.getCategory());

        verify(postTagRepository, never())
                .deleteAllByPost_Id(anyLong());
    }

    @Test
    @DisplayName("게시글을 삭제하면 deletedAt이 설정된다")
    void deletePost_Success() {
        Long userId = 1L;
        Long postId = 10L;

        User owner = user(userId);
        Post post = post(postId, owner);

        when(postRepository.findByIdAndDeletedAtIsNull(postId))
                .thenReturn(Optional.of(post));

        assertNull(post.getDeletedAt());

        postService.deletePost(userId, postId);

        assertNotNull(post.getDeletedAt());

        verify(postRepository)
                .findByIdAndDeletedAtIsNull(postId);

        verify(postRepository, never())
                .deleteById(anyLong());

        verify(postRepository, never())
                .delete(any(Post.class));
    }

    @Test
    @DisplayName("삭제된 게시글은 조회할 수 없다")
    void getPost_DeletedPost_ThrowNotFoundException() {
        Long userId = 1L;
        Long postId = 10L;

        when(postRepository.increaseViewCount(postId))
                .thenReturn(0);

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> postService.getPost(userId, postId)
        );

        assertEquals("post_not_found", exception.getCode());

        verify(postRepository)
                .increaseViewCount(postId);

        verify(postRepository, never())
                .findByIdAndDeletedAtIsNull(anyLong());
    }

    @Test
    @DisplayName("게시글 목록은 검색 Repository 결과를 반환한다")
    void getPosts_ReturnSearchResult() {
        Long userId = 1L;
        Long postId = 10L;

        PostSearchDto search = new PostSearchDto();

        User owner = user(userId);
        Post activePost = post(postId, owner);

        when(postRepository.searchPosts(search))
                .thenReturn(List.of(activePost));

        when(likeRepository.countByPostIds(List.of(postId)))
                .thenReturn(List.of());

        when(commentRepository.countByPostIds(List.of(postId)))
                .thenReturn(List.of());

        when(likeRepository.existsByPost_IdAndUser_Id(
                postId,
                userId
        )).thenReturn(false);

        when(postTagRepository.findAllByPost_Id(postId))
                .thenReturn(List.of());

        List<PostResponseDto> result =
                postService.getPosts(userId, search);

        assertEquals(1, result.size());
        assertEquals(
                activePost.getId(),
                result.get(0).getPostId()
        );

        verify(postRepository)
                .searchPosts(search);

        verify(postRepository, never())
                .findAllByDeletedAtIsNull();

        verify(postRepository, never())
                .findAll();
    }

    @Test
    @DisplayName("게시글 목록에서 삭제되지 않은 댓글 수를 반환한다")
    void getPosts_ReturnActiveCommentCount() {
        Long userId = 1L;
        Long postId = 10L;

        PostSearchDto search = new PostSearchDto();

        User owner = user(userId);
        Post post = post(postId, owner);

        PostCountProjection commentCount =
                mock(PostCountProjection.class);

        when(commentCount.getPostId())
                .thenReturn(postId);

        when(commentCount.getCountValue())
                .thenReturn(1L);

        when(postRepository.searchPosts(search))
                .thenReturn(List.of(post));

        when(likeRepository.countByPostIds(List.of(postId)))
                .thenReturn(List.of());

        when(commentRepository.countByPostIds(List.of(postId)))
                .thenReturn(List.of(commentCount));

        when(likeRepository.existsByPost_IdAndUser_Id(
                postId,
                userId
        )).thenReturn(false);

        when(postTagRepository.findAllByPost_Id(postId))
                .thenReturn(List.of());

        List<PostResponseDto> result =
                postService.getPosts(userId, search);

        assertEquals(1, result.size());
        assertEquals(
                postId,
                result.get(0).getPostId()
        );
        assertEquals(
                1,
                result.get(0).getCommentCount()
        );

        verify(postRepository)
                .searchPosts(search);

        verify(commentRepository)
                .countByPostIds(List.of(postId));
    }

    private PostRequestDto postRequest(
            String title,
            String content,
            String contentImage,
            Category category
    ) {
        PostRequestDto request = new PostRequestDto();

        ReflectionTestUtils.setField(
                request,
                "title",
                title
        );
        ReflectionTestUtils.setField(
                request,
                "content",
                content
        );
        ReflectionTestUtils.setField(
                request,
                "contentImage",
                contentImage
        );
        ReflectionTestUtils.setField(
                request,
                "category",
                category
        );
        ReflectionTestUtils.setField(
                request,
                "tags",
                List.of()
        );

        return request;
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
                "제목",
                "내용",
                "image.png",
                Category.BACKEND
        );

        ReflectionTestUtils.setField(post, "id", id);

        return post;
    }
}