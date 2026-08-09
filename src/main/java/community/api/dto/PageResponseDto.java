package community.api.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class PageResponseDto<T> {

    private final List<T> content;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;
    private final boolean hasNext;

    public PageResponseDto(
            List<T> content,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean hasNext
    ) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.hasNext = hasNext;
    }
}
