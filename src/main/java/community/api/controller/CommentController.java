package community.api.controller;

import community.api.dto.CommentRequestDto;
import community.api.dto.CommentResponseDto;
import community.api.response.ApiResponse;
import community.api.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;

@RestController
@RequestMapping("/posts/{postId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<ApiResponse<CommentResponseDto>> createComment(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long postId,
            @Valid @RequestBody CommentRequestDto request
    ) {
        CommentResponseDto response = commentService.createComment(userId, postId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of("comment_create_success", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CommentResponseDto>>> getComments(
            @PathVariable Long postId
    ) {
        List<CommentResponseDto> response = commentService.getComments(postId);

        return ResponseEntity.ok(
                ApiResponse.of("comment_list_success", response)
        );
    }

    @PatchMapping("/{commentId}")
    public ResponseEntity<ApiResponse<CommentResponseDto>> updateComment(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @Valid @RequestBody CommentRequestDto request
    ) {
        CommentResponseDto response = commentService.updateComment(
                userId,
                postId,
                commentId,
                request
        );

        return ResponseEntity.ok(
                ApiResponse.of("comment_update_success", response)
        );
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long postId,
            @PathVariable Long commentId
    ) {
        commentService.deleteComment(userId, postId, commentId);

        return ResponseEntity.noContent().build();
    }
}