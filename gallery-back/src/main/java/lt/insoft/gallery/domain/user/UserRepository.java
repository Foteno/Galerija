package lt.insoft.gallery.domain.user;

import lt.insoft.gallery.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    User findById(int id); // FIXME: never used. visada žiūrėk, ką sako IDE su savo papilkinimais/pabraukimais/paryškinimais
    Boolean existsByUsername(String username);
    User findByUsername(String username);
}
