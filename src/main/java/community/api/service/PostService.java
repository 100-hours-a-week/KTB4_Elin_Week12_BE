package community.api.service;

import community.api.dto.PageResponseDto;
import community.api.dto.PostRequestDto;
import community.api.dto.PostResponseDto;
import community.api.dto.PostSearchDto;
import community.api.entity.*;
import community.api.exception.BusinessException;
import community.api.exception.ForbiddenException;
import community.api.exception.NotFoundException;
import community.api.exception.UnauthorizedException;
import community.api.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;
    private final TagRepository tagRepository;
    private final PostTagRepository postTagRepository;

    @Transactional
    public PostResponseDto createPost(
            Long userId,
            PostRequestDto request
    ) {
        if (userId == null) {
            throw new UnauthorizedException(
                    "unauthorized_error"
            );
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "user_not_found"
                        )
                );

        List<String> tagNames =
                validateTags(request.getTags());

        Post post = new Post(
                user,
                request.getTitle(),
                request.getContent(),
                request.getContentImage(),
                request.getCategory()
        );

        Post savedPost =
                postRepository.save(post);

        savePostTags(savedPost, tagNames);

        return toResponseDto(savedPost, userId);
    }

    public PageResponseDto<PostResponseDto> getPosts(
            Long userId,
            PostSearchDto search,
            Pageable pageable
    ) {
        if (userId == null) {
            throw new UnauthorizedException(
                    "unauthorized_error"
            );
        }

        Pageable safePageable = PageRequest.of(
                pageable.getPageNumber(),
                Math.min(pageable.getPageSize(), 50)
        );

        Page<Post> postPage =
                postRepository.searchPosts(
                        search,
                        safePageable
                );

        List<PostResponseDto> content =
                toResponseDtos(
                        postPage.getContent(),
                        userId
                );

        return new PageResponseDto<>(
                content,
                postPage.getNumber(),
                postPage.getSize(),
                postPage.getTotalElements(),
                postPage.getTotalPages(),
                postPage.hasNext()
        );
    }

    public List<PostResponseDto> getLikedPosts(
            Long userId
    ) {
        if (userId == null) {
            throw new UnauthorizedException(
                    "unauthorized_error"
            );
        }

        List<Post> posts =
                likeRepository.findLikedPosts(userId)
                        .stream()
                        .map(Like::getPost)
                        .toList();

        return toResponseDtos(posts, userId);
    }

    private List<PostResponseDto> toResponseDtos(
            List<Post> posts,
            Long userId
    ) {
        if (posts.isEmpty()) {
            return List.of();
        }

        List<Long> postIds = posts.stream()
                .map(Post::getId)
                .toList();

        Map<Long, Long> likeCountMap =
                likeRepository.countByPostIds(postIds)
                        .stream()
                        .collect(Collectors.toMap(
                                PostCountProjection::getPostId,
                                PostCountProjection::getCountValue
                        ));

        Map<Long, Long> commentCountMap =
                commentRepository.countByPostIds(postIds)
                        .stream()
                        .collect(Collectors.toMap(
                                PostCountProjection::getPostId,
                                PostCountProjection::getCountValue
                        ));

        Set<Long> likedPostIds = new HashSet<>(
                likeRepository.findLikedPostIds(
                        userId,
                        postIds
                )
        );

        Map<Long, List<String>> tagNamesMap =
                postTagRepository.findAllWithTagsByPostIds(postIds)
                        .stream()
                        .collect(Collectors.groupingBy(
                                postTag -> postTag.getPost().getId(),
                                Collectors.mapping(
                                        postTag ->
                                                postTag.getTag().getName(),
                                        Collectors.toList()
                                )
                        ));

        return posts.stream()
                .map(post -> {
                    int likeCount = likeCountMap
                            .getOrDefault(
                                    post.getId(),
                                    0L
                            )
                            .intValue();

                    int commentCount =
                            commentCountMap
                                    .getOrDefault(
                                            post.getId(),
                                            0L
                                    )
                                    .intValue();

                    boolean isLiked =
                        likedPostIds.contains(post.getId());

                    List<String> tags =
                            tagNamesMap.getOrDefault(
                                    post.getId(),
                                    List.of()
                            );

                    return PostResponseDto.from(
                            post,
                            post.getUser(),
                            likeCount,
                            commentCount,
                            isLiked,
                            post.getCategory(),
                            tags
                    );
                })
                .toList();
    }

    private List<String> getTagNames(
            Long postId
    ) {
        return postTagRepository
                .findAllByPost_Id(postId)
                .stream()
                .map(postTag ->
                        postTag.getTag().getName()
                )
                .toList();
    }

    @Transactional
    public PostResponseDto getPost(
            Long userId,
            Long postId
    ) {
        if (userId == null) {
            throw new UnauthorizedException(
                    "unauthorized_error"
            );
        }

        int updatedCount =
                postRepository.increaseViewCount(postId);

        if (updatedCount == 0) {
            throw new NotFoundException(
                    "post_not_found"
            );
        }

        Post post = postRepository
                .findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "post_not_found"
                        )
                );

        return toResponseDto(post, userId);
    }

    @Transactional
    public PostResponseDto updatePost(
            Long userId,
            Long postId,
            PostRequestDto request
    ) {
        if (userId == null) {
            throw new UnauthorizedException(
                    "unauthorized_error"
            );
        }

        Post post = postRepository
                .findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "post_not_found"
                        )
                );

        if (!post.getUserId().equals(userId)) {
            throw new ForbiddenException(
                    "forbidden_error"
            );
        }

        List<String> tagNames =
                validateTags(request.getTags());

        post.update(
                request.getTitle(),
                request.getContent(),
                request.getContentImage(),
                request.getCategory()
        );

        postTagRepository.deleteAllByPost_Id(
                postId
        );
        postTagRepository.flush();

        savePostTags(post, tagNames);

        postRepository.flush();

        return toResponseDto(post, userId);
    }

    @Transactional
    public void deletePost(
            Long userId,
            Long postId
    ) {
        if (userId == null) {
            throw new UnauthorizedException(
                    "unauthorized_error"
            );
        }

        Post post = postRepository
                .findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "post_not_found"
                        )
                );

        if (!post.getUserId().equals(userId)) {
            throw new ForbiddenException(
                    "forbidden_error"
            );
        }

        post.delete();
    }

    private PostResponseDto toResponseDto(
            Post post,
            Long currentUserId
    ) {
        User user = post.getUser();

        int likeCount =
                (int) likeRepository
                        .countByPost_Id(post.getId());

        int commentCount =
                (int) commentRepository
                        .countByPost_IdAndDeletedAtIsNull(
                                post.getId()
                        );

        boolean isLiked =
                currentUserId != null
                        && likeRepository
                        .existsByPost_IdAndUser_Id(
                                post.getId(),
                                currentUserId
                        );

        Category category =
                post.getCategory();

        List<String> tags =
                getTagNames(post.getId());

        return PostResponseDto.from(
                post,
                user,
                likeCount,
                commentCount,
                isLiked,
                category,
                tags
        );
    }

    private List<String> validateTags(
            List<String> tags
    ) {
        if (tags == null) {
            return List.of();
        }

        List<String> normalizedTags =
                tags.stream()
                        .map(String::trim)
                        .map(String::toLowerCase)
                        .toList();

        if (new HashSet<>(normalizedTags).size()
                != normalizedTags.size()) {
            throw new BusinessException(
                    "duplicated_tag",
                    HttpStatus.BAD_REQUEST
            );
        }

        return normalizedTags;
    }

    private void savePostTags(
            Post post,
            List<String> tagNames
    ) {
        for (String tagName : tagNames) {
            Tag tag = tagRepository
                    .findByName(tagName)
                    .orElseGet(() ->
                            tagRepository.save(
                                    new Tag(tagName)
                            )
                    );

            PostTag postTag =
                    new PostTag(post, tag);

            postTagRepository.save(postTag);
        }
    }
}