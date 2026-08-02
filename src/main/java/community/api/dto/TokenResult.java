package community.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokenResult {
    private TokenInfo token;
    private String newRefreshToken;
}