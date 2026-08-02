package community.api.dto;


import community.api.entity.Category;
import community.api.entity.PostSort;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PostSearchDto {
    @Size(max = 100)
    private String keyword;

    private Category category;

    @Size(max = 5)
    private List<@Size(max = 20) String> tags;

    private PostSort sort = PostSort.LATEST;
}