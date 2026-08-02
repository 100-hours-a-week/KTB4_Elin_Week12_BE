package community.api.dto;

import community.api.entity.User;
import lombok.Getter;

public class UserResponseDto {

    @Getter
    public static class Register {
        private Long userId;
        private String email;
        private String nickname;
        private String profileImage;

        public Register(Long userId, String email, String nickname, String profileImage) {
            this.userId = userId;
            this.email = email;
            this.nickname = nickname;
            this.profileImage = profileImage;
        }

        public static Register from(User user) {
            return new Register(
                    user.getId(),
                    user.getEmail(),
                    user.getNickname(),
                    user.getProfileImage()
            );
        }
    }

    @Getter
    public static class Login {

        private Long userId;
        private String accessToken;
        private long expiresIn;

        public Login(
                Long userId,
                String accessToken,
                long expiresIn
        ) {
            this.userId = userId;
            this.accessToken = accessToken;
            this.expiresIn = expiresIn;
        }

        public static Login from(
                User user,
                String accessToken,
                long expiresIn
        ) {
            return new Login(
                    user.getId(),
                    accessToken,
                    expiresIn
            );
        }
    }

    @Getter
    public static class Profile {
        private Long userId;
        private String email;
        private String nickname;
        private String profileImage;

        public Profile(
                Long userId,
                String email,
                String nickname,
                String profileImage
        ) {
            this.userId = userId;
            this.email = email;
            this.nickname = nickname;
            this.profileImage = profileImage;
        }

        public static Profile from(User user) {
            return new Profile(
                    user.getId(),
                    user.getEmail(),
                    user.getNickname(),
                    user.getProfileImage()
            );
        }
    }

    @Getter
    public static class UpdateProfile {
        private Long userId;
        private String nickname;
        private String profileImage;

        public UpdateProfile(Long userId, String nickname, String profileImage) {
            this.userId = userId;
            this.nickname = nickname;
            this.profileImage = profileImage;
        }

        public static UpdateProfile from(User user) {
            return new UpdateProfile(
                    user.getId(),
                    user.getNickname(),
                    user.getProfileImage()
            );
        }
    }

    @Getter
    public static class UpdatePassword {
        private Long userId;

        public UpdatePassword(Long userId) {
            this.userId = userId;
        }
        public static UpdatePassword from(User user) {
            return new UpdatePassword(user.getId());
        }
    }
}