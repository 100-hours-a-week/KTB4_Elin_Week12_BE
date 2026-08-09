package community.api.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import community.api.dto.PostSearchDto;
import community.api.entity.Category;
import community.api.entity.Post;
import community.api.entity.PostSort;
import community.api.entity.QLike;
import community.api.entity.QPostTag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static community.api.entity.QPost.post;

@Repository
@RequiredArgsConstructor
public class PostRepositoryCustomImpl
        implements PostRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Post> searchPosts(
            PostSearchDto search,
            Pageable pageable
    ) {
        PostSearchDto condition =
                search == null
                        ? new PostSearchDto()
                        : search;

        JPAQuery<Post> query = queryFactory
                .selectFrom(post)
                .where(
                        post.deletedAt.isNull(),
                        keywordContains(condition.getKeyword()),
                        categoryEq(condition.getCategory()),
                        tagContainsAny(condition.getTags())
                );

        Long total = queryFactory
                .select(post.id.count())
                .from(post)
                .where(
                        post.deletedAt.isNull(),
                        keywordContains(condition.getKeyword()),
                        categoryEq(condition.getCategory()),
                        tagContainsAny(condition.getTags())
                )
                .fetchOne();

        long totalElements =
                total == null ? 0L : total;

        PostSort sort = condition.getSort() == null
                ? PostSort.LATEST
                : condition.getSort();

        List<Post> content;

        if (sort == PostSort.LIKE_COUNT) {
            QLike postLike = QLike.like;

            content = query
                    .leftJoin(postLike)
                    .on(postLike.post.eq(post))
                    .groupBy(post)
                    .orderBy(
                            postLike.id.count().desc(),
                            post.id.desc()
                    )
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize())
                    .fetch();
        } else {
            content = query
                    .orderBy(
                            orderBy(sort),
                            post.id.desc()
                    )
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize())
                    .fetch();
        }

        return new PageImpl<>(
                content,
                pageable,
                totalElements
        );
    }

    private BooleanExpression keywordContains(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }

        String value = keyword.trim();

        return post.title.containsIgnoreCase(value)
                .or(post.content.containsIgnoreCase(value));
    }

    private BooleanExpression categoryEq(Category category) {
        return category == null
                ? null
                : post.category.eq(category);
    }

    private BooleanExpression tagContainsAny(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return null;
        }

        List<String> normalizedTags = tags.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(StringUtils::hasText)
                .map(tag -> tag.toLowerCase(Locale.ROOT))
                .distinct()
                .toList();

        if (normalizedTags.isEmpty()) {
            return null;
        }

        QPostTag postTag = QPostTag.postTag;

        return JPAExpressions
                .selectOne()
                .from(postTag)
                .where(
                        postTag.post.eq(post),
                        postTag.tag.name.in(normalizedTags)
                )
                .exists();
    }

    private OrderSpecifier<?> orderBy(PostSort sort) {
        return switch (sort) {
            case LATEST -> post.createdAt.desc();
            case OLDEST -> post.createdAt.asc();
            case VIEW_COUNT -> post.viewCount.desc();

            case LIKE_COUNT -> post.createdAt.desc();
        };
    }
}