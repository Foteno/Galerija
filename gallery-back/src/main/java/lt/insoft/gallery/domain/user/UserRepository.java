package lt.insoft.gallery.domain.user;

import lt.insoft.gallery.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Boolean existsByUsername(String username);
    User findByUsername(String username);
}
