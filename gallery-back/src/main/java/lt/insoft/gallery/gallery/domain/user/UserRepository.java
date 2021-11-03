package lt.insoft.gallery.gallery.domain.user;

import lt.insoft.gallery.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
    User findById(int id);
}
