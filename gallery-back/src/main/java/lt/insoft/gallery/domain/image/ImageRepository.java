package lt.insoft.gallery.domain.image;

import lt.insoft.Image;
import lt.insoft.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface ImageRepository extends JpaRepository<Image, Integer> {

    Image findById(int id);
}
