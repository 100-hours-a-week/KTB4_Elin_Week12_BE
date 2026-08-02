package community.api.service;

import community.api.auth.JwtProvider;
import community.api.dto.LoginResult;
import community.api.dto.TokenInfo;
import community.api.dto.TokenResult;
import community.api.dto.UserRequestDto;
import community.api.dto.UserResponseDto;
import community.api.entity.RefreshToken;
import community.api.entity.User;
import community.api.exception.ConflictException;
import community.api.exception.NotFoundException;
import community.api.exception.UnauthorizedException;
import community.api.repository.RefreshTokenRepository;
import community.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponseDto.Register register(UserRequestDto.Register request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("duplicated_email");
        }

        if (userRepository.existsByNickname(request.getNickname())) {
            throw new ConflictException("duplicated_nickname");
        }

        User user = new User(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getNickname(),
                request.getProfileImage()
        );

        User savedUser = userRepository.save(user);

        return UserResponseDto.Register.from(savedUser);
    }

    @Transactional
    public LoginResult login(UserRequestDto.Login request) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(request.getEmail());

        if (user == null) {
            throw new UnauthorizedException("login_failed");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("login_failed");
        }

        String accessToken = jwtProvider.createAccessToken(
                user.getId(),
                user.getEmail(),
                user.getNickname()
        );

        String refreshToken = jwtProvider.createRefreshToken(user.getId());

        refreshTokenRepository.deleteByUserId(user.getId());

        refreshTokenRepository.save(
                new RefreshToken(
                        refreshToken,
                        user.getId(),
                        LocalDateTime.now().plusDays(14)
                )
        );

        return new LoginResult(
                UserResponseDto.Login.from(
                        user,
                        accessToken,
                        jwtProvider.getAccessTokenValidityInMilliseconds()
                ),
                refreshToken
        );
    }

    @Transactional
    public TokenResult refreshAccessToken(String refreshToken) {
        if (refreshToken == null) {
            throw new UnauthorizedException("unauthorized_error");
        }

        RefreshToken savedRefreshToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new UnauthorizedException("unauthorized_error"));

        if (savedRefreshToken.isExpired()) {
            refreshTokenRepository.delete(savedRefreshToken);
            throw new UnauthorizedException("unauthorized_error");
        }

        User user = userRepository.findById(savedRefreshToken.getUserId())
                .filter(foundUser -> foundUser.getDeletedAt() == null)
                .orElseThrow(() -> new UnauthorizedException("unauthorized_error"));

        String newAccessToken = jwtProvider.createAccessToken(
                user.getId(),
                user.getEmail(),
                user.getNickname()
        );

        String newRefreshToken = jwtProvider.createRefreshToken(user.getId());

        refreshTokenRepository.delete(savedRefreshToken);

        refreshTokenRepository.save(
                new RefreshToken(
                        newRefreshToken,
                        user.getId(),
                        LocalDateTime.now().plusDays(14)
                )
        );

        return new TokenResult(
                new TokenInfo(
                        newAccessToken,
                        jwtProvider.getAccessTokenValidityInMilliseconds()
                ),
                newRefreshToken
        );
    }

    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken == null) {
            return;
        }

        refreshTokenRepository.findByToken(refreshToken)
                .ifPresent(refreshTokenRepository::delete);
    }

    public UserResponseDto.Profile getProfile(Long userId) {
        if (userId == null) {
            throw new UnauthorizedException("unauthorized_error");
        }

        User user = userRepository.findById(userId)
                .filter(foundUser -> foundUser.getDeletedAt() == null)
                .orElseThrow(() -> new NotFoundException("user_not_found"));

        return UserResponseDto.Profile.from(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        if (userId == null) {
            throw new UnauthorizedException("unauthorized_error");
        }

        User user = userRepository.findById(userId)
                .filter(foundUser -> foundUser.getDeletedAt() == null)
                .orElseThrow(() -> new NotFoundException("user_not_found"));

        user.delete();

        refreshTokenRepository.deleteByUserId(user.getId());
    }

    @Transactional
    public UserResponseDto.UpdateProfile updateProfile(
            Long userId,
            UserRequestDto.UpdateProfile request
    ) {
        if (userId == null) {
            throw new UnauthorizedException("unauthorized_error");
        }

        User user = userRepository.findById(userId)
                .filter(foundUser -> foundUser.getDeletedAt() == null)
                .orElseThrow(() -> new NotFoundException("user_not_found"));

        if (request.getNickname() != null) {
            if (!request.getNickname().equals(user.getNickname())
                    && userRepository.existsByNickname(request.getNickname())) {
                throw new ConflictException("duplicated_nickname");
            }
        }

        user.updateProfile(
                request.getNickname(),
                request.getProfileImage()
        );

        return UserResponseDto.UpdateProfile.from(user);
    }

    @Transactional
    public UserResponseDto.UpdatePassword updatePassword(
            Long userId,
            UserRequestDto.UpdatePassword request
    ) {
        if (userId == null) {
            throw new UnauthorizedException("unauthorized_error");
        }

        User user = userRepository.findById(userId)
                .filter(foundUser -> foundUser.getDeletedAt() == null)
                .orElseThrow(() -> new NotFoundException("user_not_found"));

        user.updatePassword(
                passwordEncoder.encode(request.getPassword())
        );

        return UserResponseDto.UpdatePassword.from(user);
    }
}
