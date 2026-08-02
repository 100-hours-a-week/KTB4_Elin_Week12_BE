package community.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResult {

    private UserResponseDto.Login response;
    private String refreshToken;
}