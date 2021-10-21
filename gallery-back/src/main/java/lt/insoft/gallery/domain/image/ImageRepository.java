package lt.insoft.gallery.domain.image;

import lt.insoft.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ImageRepository extends JpaRepository<Image, Integer> {
    Image findById(int id);
    Image findByUuid(String uuid);
}
