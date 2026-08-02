package community.api.repository;

import community.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UserRepository extends JpaRepository<User, Long> {

    User findByEmailAndDeletedAtIsNull(String email);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

}
