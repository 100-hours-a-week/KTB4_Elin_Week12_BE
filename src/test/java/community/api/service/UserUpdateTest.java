package community.api.service;

import community.api.auth.JwtProvider;
import community.api.dto.UserRequestDto;
import community.api.dto.UserResponseDto;
import community.api.entity.User;
import community.api.exception.ConflictException;
import community.api.exception.NotFoundException;
import community.api.exception.UnauthorizedException;
import community.api.repository.RefreshTokenRepository;
import community.api.repository.UserRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserUpdateTest {

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
    @DisplayName("회원정보 수정 성공 시 닉네임과 프로필 이미지를 수정한다")
    void updateProfile_UpdateNicknameAndProfileImage() {
        UserRequestDto.UpdateProfile request = updateProfileRequest(
                "엘린수정",
                "profile2.png"
        );

        User user = user(1L);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        UserResponseDto.UpdateProfile result =
                userService.updateProfile(1L, request);

        assertEquals("엘린수정", user.getNickname());
        assertEquals("profile2.png", user.getProfileImage());

        assertEquals(1L, result.getUserId());
        assertEquals("엘린수정", result.getNickname());
        assertEquals("profile2.png", result.getProfileImage());
    }

    @Test
    @DisplayName("이미 존재하는 닉네임으로 회원정보 수정하면 예외가 발생한다")
    void updateProfile_DuplicatedNickname_ThrowConflictException() {
        UserRequestDto.UpdateProfile request = updateProfileRequest(
                "이미있는닉네임",
                "profile2.png"
        );

        User user = user(1L);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(userRepository.existsByNickname("이미있는닉네임"))
                .thenReturn(true);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> userService.updateProfile(1L, request)
        );

        assertEquals("duplicated_nickname", exception.getCode());

        assertEquals("엘린", user.getNickname());
        assertEquals("profile1.png", user.getProfileImage());
    }

    @Test
    @DisplayName("비밀번호 변경 시 8자 미만이면 변경 실패한다")
    void updatePassword_LessThan8() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

        UserRequestDto.UpdatePassword request =
                updatePasswordRequest("UserP1!");

        Set<ConstraintViolation<UserRequestDto.UpdatePassword>> violations =
                validator.validate(request);

        assertTrue(violations.size() > 0);
    }

    @Test
    @DisplayName("비밀번호 변경 시 20자 초과이면 변경 실패한다")
    void updatePassword_MoreThan20() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

        UserRequestDto.UpdatePassword request =
                updatePasswordRequest("UserPassword12345678!");

        Set<ConstraintViolation<UserRequestDto.UpdatePassword>> violations =
                validator.validate(request);

        assertTrue(violations.size() > 0);
    }

    @Test
    @DisplayName("비밀번호 변경 시 새 비밀번호를 암호화하여 저장한다")
    void updatePassword_EncodeNewPassword() {
        UserRequestDto.UpdatePassword request =
                updatePasswordRequest("NewPassword1!");

        User user = user(1L);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.encode("NewPassword1!"))
                .thenReturn("encodedNewPassword");

        UserResponseDto.UpdatePassword result =
                userService.updatePassword(1L, request);

        verify(passwordEncoder).encode("NewPassword1!");

        assertEquals("encodedNewPassword", user.getPassword());
        assertEquals(1L, result.getUserId());
    }

    @Test
    @DisplayName("회원 탈퇴 시 사용자를 삭제하고 refreshToken도 삭제한다")
    void deleteUser_DeleteUserAndRefreshToken() {
        User user = user(1L);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        userService.deleteUser(1L);

        assertNotNull(user.getDeletedAt());

        verify(refreshTokenRepository).deleteByUserId(1L);
    }

    @Test
    @DisplayName("유저 아이디가 없으면 회원정보 수정 시 예외가 발생한다")
    void updateProfile_NullUserId_ThrowUnauthorizedException() {
        UserRequestDto.UpdateProfile request = updateProfileRequest(
                "엘린수정",
                "profile2.png"
        );

        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> userService.updateProfile(null, request)
        );

        assertEquals("unauthorized_error", exception.getCode());

        verify(userRepository, never()).findById(any());
    }

    @Test
    @DisplayName("존재하지 않는 회원이면 회원정보 수정 시 예외가 발생한다")
    void updateProfile_NotFoundUser_ThrowNotFoundException() {
        UserRequestDto.UpdateProfile request = updateProfileRequest(
                "엘린수정",
                "profile2.png"
        );

        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.updateProfile(1L, request)
        );

        assertEquals("user_not_found", exception.getCode());
    }

    @Test
    @DisplayName("탈퇴한 사용자가 회원정보를 수정하면 예외가 발생한다")
    void updateProfile_DeletedUser_ThrowNotFoundException() {
        UserRequestDto.UpdateProfile request = updateProfileRequest(
                "엘린수정",
                "profile2.png"
        );

        User user = user(1L);
        user.delete();

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.updateProfile(1L, request)
        );

        assertEquals("user_not_found", exception.getCode());

        assertEquals("엘린", user.getNickname());
        assertEquals("profile1.png", user.getProfileImage());
    }

    @Test
    @DisplayName("유저 아이디가 없으면 비밀번호 변경 시 예외가 발생한다")
    void updatePassword_NullUserId_ThrowUnauthorizedException() {
        UserRequestDto.UpdatePassword request =
                updatePasswordRequest("NewPassword1!");

        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> userService.updatePassword(null, request)
        );

        assertEquals("unauthorized_error", exception.getCode());

        verify(userRepository, never()).findById(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    @DisplayName("존재하지 않는 회원이면 비밀번호 변경 시 예외가 발생한다")
    void updatePassword_NotFoundUser_ThrowNotFoundException() {
        UserRequestDto.UpdatePassword request =
                updatePasswordRequest("NewPassword1!");

        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.updatePassword(1L, request)
        );

        assertEquals("user_not_found", exception.getCode());

        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    @DisplayName("탈퇴한 사용자가 비밀번호를 변경하면 예외가 발생한다")
    void updatePassword_DeletedUser_ThrowNotFoundException() {
        UserRequestDto.UpdatePassword request =
                updatePasswordRequest("NewPassword1!");

        User user = user(1L);
        user.delete();

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.updatePassword(1L, request)
        );

        assertEquals("user_not_found", exception.getCode());

        assertEquals("encodedPassword", user.getPassword());

        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    @DisplayName("유저 아이디가 없으면 회원 탈퇴 시 예외가 발생한다")
    void deleteUser_NullUserId_ThrowUnauthorizedException() {
        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> userService.deleteUser(null)
        );

        assertEquals("unauthorized_error", exception.getCode());

        verify(userRepository, never()).findById(any());
        verify(refreshTokenRepository, never()).deleteByUserId(any());
    }

    @Test
    @DisplayName("존재하지 않는 회원이면 회원 탈퇴 시 예외가 발생한다")
    void deleteUser_NotFoundUser_ThrowNotFoundException() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.deleteUser(1L)
        );

        assertEquals("user_not_found", exception.getCode());

        verify(refreshTokenRepository, never()).deleteByUserId(any());
    }

    @Test
    @DisplayName("탈퇴한 사용자를 다시 탈퇴시키면 예외가 발생한다")
    void deleteUser_DeletedUser_ThrowNotFoundException() {
        User user = user(1L);
        user.delete();

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.deleteUser(1L)
        );

        assertEquals("user_not_found", exception.getCode());

        verify(refreshTokenRepository, never()).deleteByUserId(any());
    }

    private UserRequestDto.UpdateProfile updateProfileRequest(
            String nickname,
            String profileImage
    ) {
        UserRequestDto.UpdateProfile request =
                new UserRequestDto.UpdateProfile();

        ReflectionTestUtils.setField(request, "nickname", nickname);
        ReflectionTestUtils.setField(request, "profileImage", profileImage);

        return request;
    }

    private UserRequestDto.UpdatePassword updatePasswordRequest(String password) {
        UserRequestDto.UpdatePassword request =
                new UserRequestDto.UpdatePassword();

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