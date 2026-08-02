package community.api.entity;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tags", uniqueConstraints = @UniqueConstraint(name = "unique_tag_name", columnNames = "name"))

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id")
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 20)
    private String name;

    public Tag(String name) {
        this.name = name;
    }
}
