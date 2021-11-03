package lt.insoft.gallery.gallery.domain.image;

import lt.insoft.gallery.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;


@Repository
public interface ImageRepository extends JpaRepository<Image, Integer>, JpaSpecificationExecutor<Image> {
    Image findById(int id);
    Image findByUuid(String uuid);
}
