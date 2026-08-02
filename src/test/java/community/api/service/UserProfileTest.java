package community.api.service;

import community.api.auth.JwtProvider;
import community.api.dto.UserResponseDto;
import community.api.entity.User;
import community.api.exception.NotFoundException;
import community.api.exception.UnauthorizedException;
import community.api.repository.RefreshTokenRepository;
import community.api.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfileTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtProvider jwtProvider;

    @Test
    @DisplayName("회원정보 조회 성공 시 사용자 정보를 반환한다")
    void profile_ReturnUserInfo() {
        User user = new User(
                "elin@example.com",
                "encodedPassword",
                "엘린",
                "profile1.png"
        );

        ReflectionTestUtils.setField(user, "id", 1L);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        UserResponseDto.Profile result = userService.getProfile(1L);

        assertEquals(1L, result.getUserId());
        assertEquals("elin@example.com", result.getEmail());
        assertEquals("엘린", result.getNickname());
        assertEquals("profile1.png", result.getProfileImage());
    }

    @Test
    @DisplayName("유저 아이디가 없으면 회원정보 조회 시 예외가 발생한다")
    void profile_NullUserId_ThrowUnauthorizedException() {
        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> userService.getProfile(null)
        );

        assertEquals("unauthorized_error", exception.getCode());

        verify(userRepository, never()).findById(any());
    }

    @Test
    @DisplayName("존재하지 않는 회원이면 회원정보 조회 시 예외가 발생한다")
    void profile_NotFoundUser_ThrowNotFoundException() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.getProfile(1L)
        );

        assertEquals("user_not_found", exception.getCode());
    }
    @Test
    @DisplayName("탈퇴한 회원이면 회원정보 조회 시 예외가 발생한다")
    void profile_DeletedUser_ThrowNotFoundException() {
        User user = new User(
                "elin@example.com",
                "encodedPassword",
                "엘린",
                "profile1.png"
        );

        ReflectionTestUtils.setField(user, "id", 1L);
        user.delete();

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.getProfile(1L)
        );

        assertEquals("user_not_found", exception.getCode());
    }
}