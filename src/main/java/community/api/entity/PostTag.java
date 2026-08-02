package community.api.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "post_tags", uniqueConstraints = { @UniqueConstraint(name = "unique_post_tag", columnNames = {"post_id", "tag_id"})})

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostTag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_tag_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    public PostTag(Post post, Tag tag) {
        this.post = post;
        this.tag = tag;
    }
}
