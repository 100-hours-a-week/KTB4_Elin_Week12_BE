package community.api.controller;

import community.api.dto.LoginResult;
import community.api.dto.TokenInfo;
import community.api.dto.TokenResult;
import community.api.dto.UserRequestDto;
import community.api.dto.UserResponseDto;
import community.api.response.ApiResponse;
import community.api.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Value("${app.cookie.secure}")
    private boolean cookieSecure;

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponseDto.Register>> register(
            @Valid @RequestBody UserRequestDto.Register request
    ) {
        UserResponseDto.Register response = userService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of("register_success", response));
    }

    @PostMapping("/sessions")
    public ResponseEntity<ApiResponse<UserResponseDto.Login>> login(
            @Valid @RequestBody UserRequestDto.Login request,
            HttpServletResponse httpResponse
    ) {
        LoginResult result = userService.login(request);

        ResponseCookie refreshCookie = ResponseCookie
                .from("refreshToken", result.getRefreshToken())
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(14 * 24 * 60 * 60)
                .sameSite("Lax")
                .build();

        httpResponse.addHeader(
                HttpHeaders.SET_COOKIE,
                refreshCookie.toString()
        );

        return ResponseEntity.ok(
                ApiResponse.of("login_success", result.getResponse())
        );
    }

    @PostMapping("/token/refresh")
    public ResponseEntity<ApiResponse<TokenInfo>> refreshAccessToken(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse httpResponse
    ) {
        TokenResult result = userService.refreshAccessToken(refreshToken);

        ResponseCookie refreshCookie = ResponseCookie
                .from("refreshToken", result.getNewRefreshToken())
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(14 * 24 * 60 * 60)
                .sameSite("Lax")
                .build();

        httpResponse.addHeader(
                HttpHeaders.SET_COOKIE,
                refreshCookie.toString()
        );

        return ResponseEntity.ok(
                ApiResponse.of("token_refresh_success", result.getToken())
        );
    }

    @DeleteMapping("/sessions")
    public ResponseEntity<Void> logout(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse httpResponse
    ) {
        userService.logout(refreshToken);

        ResponseCookie expiredRefreshCookie = ResponseCookie
                .from("refreshToken", "")
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        httpResponse.addHeader(
                HttpHeaders.SET_COOKIE,
                expiredRefreshCookie.toString()
        );

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponseDto.Profile>> getProfile(
            @AuthenticationPrincipal Long userId
    ) {
        UserResponseDto.Profile response =
                userService.getProfile(userId);

        return ResponseEntity.ok(
                ApiResponse.of("profile_get_success", response)
        );
    }

    @DeleteMapping("/profile")
    public ResponseEntity<Void> deleteUser(
            @AuthenticationPrincipal Long userId
    ) {
        userService.deleteUser(userId);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponseDto.UpdateProfile>> updateProfile(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody UserRequestDto.UpdateProfile request
    ) {
        UserResponseDto.UpdateProfile response =
                userService.updateProfile(userId, request);

        return ResponseEntity.ok(
                ApiResponse.of("profile_update_success", response)
        );
    }

    @PatchMapping("/password")
    public ResponseEntity<ApiResponse<UserResponseDto.UpdatePassword>> updatePassword(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody UserRequestDto.UpdatePassword request
    ) {
        UserResponseDto.UpdatePassword response =
                userService.updatePassword(userId, request);

        return ResponseEntity.ok(
                ApiResponse.of("password_update_success", response)
        );
    }
}
