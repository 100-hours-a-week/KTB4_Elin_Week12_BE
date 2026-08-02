package community.api.service;

import community.api.auth.JwtProvider;
import community.api.dto.LoginResult;
import community.api.dto.TokenResult;
import community.api.dto.UserRequestDto;
import community.api.entity.RefreshToken;
import community.api.entity.User;
import community.api.exception.UnauthorizedException;
import community.api.repository.RefreshTokenRepository;
import community.api.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserLoginTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("로그인 성공 시 accessToken, refreshToken을 반환한다")
    void login_ReturnToken() {
        UserRequestDto.Login request = validLoginRequest();

        stubSuccessfulLogin();

        LoginResult result = userService.login(request);

        assertEquals("accessToken", result.getResponse().getAccessToken());
        assertEquals("refreshToken", result.getRefreshToken());
        assertEquals(3600000L, result.getResponse().getExpiresIn());
    }

    @Test
    @DisplayName("저장된 refreshToken이 없으면 재발급에 실패한다")
    void refresh_NotFoundRefreshToken_ThrowUnauthorizedException() {
        when(refreshTokenRepository.findByToken("refreshToken"))
                .thenReturn(Optional.empty());

        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> userService.refreshAccessToken("refreshToken")
        );

        assertEquals("unauthorized_error", exception.getCode());

        verify(userRepository, never()).findById(any());
        verify(jwtProvider, never()).createAccessToken(any(), any(), any());
        verify(jwtProvider, never()).createRefreshToken(any());
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("로그아웃 시 저장된 refreshToken을 삭제한다")
    void logout_DeleteRefreshToken() {
        RefreshToken refreshToken = new RefreshToken(
                "refreshToken",
                1L,
                LocalDateTime.now().plusDays(14)
        );

        when(refreshTokenRepository.findByToken("refreshToken"))
                .thenReturn(Optional.of(refreshToken));

        userService.logout("refreshToken");

        verify(refreshTokenRepository).delete(refreshToken);
    }

    @Test
    @DisplayName("만료된 refreshToken이면 재발급에 실패한다")
    void refresh_ExpiredRefreshToken_ThrowUnauthorizedException() {
        RefreshToken expiredRefreshToken = new RefreshToken(
                "refreshToken",
                1L,
                LocalDateTime.now().minusDays(1)
        );

        when(refreshTokenRepository.findByToken("refreshToken"))
                .thenReturn(Optional.of(expiredRefreshToken));

        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> userService.refreshAccessToken("refreshToken")
        );

        assertEquals("unauthorized_error", exception.getCode());

        verify(refreshTokenRepository).delete(expiredRefreshToken);
        verify(userRepository, never()).findById(any());
        verify(jwtProvider, never()).createAccessToken(any(), any(), any());
        verify(jwtProvider, never()).createRefreshToken(any());
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("로그인 성공 시 기존 refreshToken을 삭제하고 새로운 refreshToken을 저장한다")
    void login_SaveRefreshToken() {
        UserRequestDto.Login request = validLoginRequest();

        stubSuccessfulLogin();

        userService.login(request);

        ArgumentCaptor<RefreshToken> refreshTokenCaptor =
                ArgumentCaptor.forClass(RefreshToken.class);

        verify(refreshTokenRepository).deleteByUserId(1L);
        verify(refreshTokenRepository).save(refreshTokenCaptor.capture());

        RefreshToken savedRefreshToken = refreshTokenCaptor.getValue();

        assertEquals("refreshToken", savedRefreshToken.getToken());
        assertEquals(1L, savedRefreshToken.getUserId());
    }

    @Test
    @DisplayName("정상 refreshToken이면 새 accessToken과 새 refreshToken을 반환한다")
    void refresh_ReturnNewRefreshToken() {
        RefreshToken savedRefreshToken = new RefreshToken(
                "oldRefreshToken",
                1L,
                LocalDateTime.now().plusDays(1)
        );

        User user = user(1L);

        when(refreshTokenRepository.findByToken("oldRefreshToken"))
                .thenReturn(Optional.of(savedRefreshToken));

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(jwtProvider.createAccessToken(1L, "elin@example.com", "엘린"))
                .thenReturn("newAccessToken");

        when(jwtProvider.createRefreshToken(1L))
                .thenReturn("newRefreshToken");

        when(jwtProvider.getAccessTokenValidityInMilliseconds())
                .thenReturn(3600000L);

        TokenResult result = userService.refreshAccessToken("oldRefreshToken");

        assertEquals("newAccessToken", result.getToken().getAccessToken());
        assertEquals(3600000L, result.getToken().getExpiresIn());
        assertEquals("newRefreshToken", result.getNewRefreshToken());

        ArgumentCaptor<RefreshToken> refreshTokenCaptor =
                ArgumentCaptor.forClass(RefreshToken.class);

        verify(refreshTokenRepository).delete(savedRefreshToken);
        verify(refreshTokenRepository).save(refreshTokenCaptor.capture());

        RefreshToken newSavedRefreshToken = refreshTokenCaptor.getValue();

        assertEquals("newRefreshToken", newSavedRefreshToken.getToken());
        assertEquals(1L, newSavedRefreshToken.getUserId());
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 로그인하면 예외가 발생한다")
    void login_NotFoundEmail_ThrowUnauthorizedException() {
        UserRequestDto.Login request = validLoginRequest();

        when(userRepository.findByEmailAndDeletedAtIsNull("elin@example.com"))
                .thenReturn(null);

        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> userService.login(request)
        );

        assertEquals("login_failed", exception.getCode());

        verify(passwordEncoder, never()).matches(any(), any());
        verify(jwtProvider, never()).createAccessToken(any(), any(), any());
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("비밀번호가 일치하지 않으면 예외가 발생한다")
    void login_WrongPassword_ThrowUnauthorizedException() {
        UserRequestDto.Login request = loginRequest(
                "elin@example.com",
                "WrongPassword00!"
        );

        User user = user(1L);

        when(userRepository.findByEmailAndDeletedAtIsNull("elin@example.com"))
                .thenReturn(user);

        when(passwordEncoder.matches("WrongPassword00!", "encodedPassword"))
                .thenReturn(false);

        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> userService.login(request)
        );

        assertEquals("login_failed", exception.getCode());

        verify(jwtProvider, never()).createAccessToken(any(), any(), any());
        verify(jwtProvider, never()).createRefreshToken(any());
        verify(refreshTokenRepository, never()).save(any());
    }

    private UserRequestDto.Login validLoginRequest() {
        return loginRequest(
                "elin@example.com",
                "UserPassword1!"
        );
    }

    private void stubSuccessfulLogin() {
        User user = user(1L);

        when(userRepository.findByEmailAndDeletedAtIsNull("elin@example.com"))
                .thenReturn(user);

        when(passwordEncoder.matches("UserPassword1!", "encodedPassword"))
                .thenReturn(true);

        when(jwtProvider.createAccessToken(1L, "elin@example.com", "엘린"))
                .thenReturn("accessToken");

        when(jwtProvider.createRefreshToken(1L))
                .thenReturn("refreshToken");

        when(jwtProvider.getAccessTokenValidityInMilliseconds())
                .thenReturn(3600000L);
    }

    private UserRequestDto.Login loginRequest(String email, String password) {
        UserRequestDto.Login request = new UserRequestDto.Login();

        ReflectionTestUtils.setField(request, "email", email);
        ReflectionTestUtils.setField(request, "password", password);

        return request;
    }

    private User user(Long id) {
        User user = new User(
                "elin@example.com",
                "encodedPassword",
                "엘린",
                "profile1.png"
        );

        ReflectionTestUtils.setField(user, "id", id);

        return user;
    }
}
