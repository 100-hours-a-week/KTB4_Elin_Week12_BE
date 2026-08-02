package community.api.dto;

import community.api.entity.Comment;
import community.api.entity.User;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CommentResponseDto {

    private Long commentId;
    private Long postId;
    private Long userId;
    private String nickname;
    private String profileImage;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CommentResponseDto(
            Long commentId,
            Long postId,
            Long userId,
            String nickname,
            String profileImage,
            String content,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.commentId = commentId;
        this.postId = postId;
        this.userId = userId;
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static CommentResponseDto from(Comment comment) {
        User user = comment.getUser();
        boolean deletedUser = user.getDeletedAt() != null;

        return new CommentResponseDto(
                comment.getId(),
                comment.getPostId(),
                deletedUser ? null : user.getId(),
                deletedUser ? "탈퇴한 사용자" : user.getNickname(),
                deletedUser ? null : user.getProfileImage(),
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}