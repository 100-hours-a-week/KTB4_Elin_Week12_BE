package community.api.controller;

import community.api.dto.LikeResponseDto;
import community.api.response.ApiResponse;
import community.api.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/posts/{postId}/likes")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @PostMapping
    public ResponseEntity<ApiResponse<LikeResponseDto>> addLike(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long postId
    ) {
        LikeResponseDto response = likeService.addLike(userId, postId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of("like_create_success", response));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteLike(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long postId
    ) {
        likeService.deleteLike(userId, postId);

        return ResponseEntity.noContent().build();
    }
}