package community.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

public class UserRequestDto {
    @Getter
    public static class Register {
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        private String email;

        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(min = 8, max = 20)
        private String password;

        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(max = 10)
        private String nickname;

        @NotBlank(message = "프로필 이미지는 필수입니다.")
        private String profileImage;
    }

    @Getter
    public static class Login {
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        private String email;

        @NotBlank(message = "비밀번호는 필수입니다.")
        private String password;
    }

    @Getter
    public static class UpdateProfile {
        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(max = 10)
        private String nickname;

        private String profileImage;
    }

    @Getter
    public static class UpdatePassword {
        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(min = 8, max = 20)
        private String password;
    }
}
