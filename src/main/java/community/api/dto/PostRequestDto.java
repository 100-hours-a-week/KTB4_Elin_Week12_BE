package community.api.dto;

import community.api.entity.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.List;

@Getter
public class PostRequestDto {
    @Size(max = 26)
    @NotBlank(message = "제목은 필수입니다.")
    private String title;

    @NotBlank(message = "내용은 필수입니다.")
    private String content;

    private String contentImage;

    @NotNull(message = "카테고리를 선택해주세요.")
    private Category category;

    @Size(max = 5, message = "태그는 최대 5개까지 입력할 수 있습니다.")
    private List<@NotBlank(message = "태그는 빈 값일 수 없습니다.")
        @Size(max = 20, message = "태그는 20자 이하여야 합니다.") String> tags;

}