package community.api.service;

import community.api.dto.UserRequestDto;
import community.api.dto.UserResponseDto;
import community.api.entity.User;
import community.api.exception.ConflictException;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRegisterTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("회원가입 시 비밀번호를 암호화하여 저장해야 한다")
    void register_EncodedPassword() {
        UserRequestDto.Register request = validRegisterRequest();

        stubSuccessfulRegister();

        userService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        verify(passwordEncoder).encode("UserPassword1!");
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertEquals("encodedPassword", savedUser.getPassword());
        assertNotEquals("UserPassword1!", savedUser.getPassword());
    }

    @Test
    @DisplayName("회원가입 성공 시 사용자 정보를 반환한다")
    void register_ReturnUserInfo() {
        UserRequestDto.Register request = validRegisterRequest();

        stubSuccessfulRegister();

        UserResponseDto.Register result = userService.register(request);

        assertEquals("elin@example.com", result.getEmail());
        assertEquals("엘린", result.getNickname());
        assertEquals("profile1.png", result.getProfileImage());
    }

    @Test
    @DisplayName("이미 존재하는 이메일로 회원가입하면 예외가 발생한다")
    void register_DuplicatedEmail_ThrowConflictException() {
        UserRequestDto.Register request = registerRequest(
                "elin@example.com",
                null,
                null,
                null
        );

        when(userRepository.existsByEmail("elin@example.com")).thenReturn(true);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> userService.register(request)
        );

        assertEquals("duplicated_email", exception.getCode());

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("이미 존재하는 닉네임으로 회원가입하면 예외가 발생한다")
    void register_DuplicatedNickname_ThrowConflictException() {
        UserRequestDto.Register request = registerRequest(
                "elin@example.com",
                null,
                "엘린",
                null
        );

        when(userRepository.existsByNickname("엘린")).thenReturn(true);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> userService.register(request)
        );

        assertEquals("duplicated_nickname", exception.getCode());

        verify(userRepository, never()).save(any(User.class));
    }

    private UserRequestDto.Register validRegisterRequest() {
        return registerRequest(
                "elin@example.com",
                "UserPassword1!",
                "엘린",
                "profile1.png"
        );
    }

    private void stubSuccessfulRegister() {
        when(passwordEncoder.encode("UserPassword1!"))
                .thenReturn("encodedPassword");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private UserRequestDto.Register registerRequest(
            String email,
            String password,
            String nickname,
            String profileImage
    ) {
        UserRequestDto.Register request = new UserRequestDto.Register();

        ReflectionTestUtils.setField(request, "email", email);
        ReflectionTestUtils.setField(request, "password", password);
        ReflectionTestUtils.setField(request, "nickname", nickname);
        ReflectionTestUtils.setField(request, "profileImage", profileImage);

        return request;
    }
}