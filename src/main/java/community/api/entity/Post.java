package community.api.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseEntity{

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private Category category;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "title", nullable = false, length = 26)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "MEDIUMTEXT")
    private String content;

    @Column(name = "content_image", length = 500)
    private String contentImage;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "view_count", nullable = false)
    private int viewCount;

    public Post(User user, String title, String content, String contentImage, Category category) {
        this.user = user;
        this.title = title;
        this.content = content;
        this.contentImage = contentImage;
        this.viewCount = 0;
        this.category = category;
    }

    public void update(String title, String content, String contentImage,Category category) {
        this.title = title;
        this.content = content;
        this.contentImage = contentImage;
        this.category = category;
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }

    public Long getUserId() {
        return user.getId();
    }
}
