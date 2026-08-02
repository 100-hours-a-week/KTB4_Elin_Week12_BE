package community.api.dto;

import lombok.Getter;

@Getter
public class LikeResponseDto {

    private Long postId;
    private Long userId;
    private int likeCount;

    public LikeResponseDto(Long postId, Long userId, int likeCount) {
        this.postId = postId;
        this.userId = userId;
        this.likeCount = likeCount;
    }
}