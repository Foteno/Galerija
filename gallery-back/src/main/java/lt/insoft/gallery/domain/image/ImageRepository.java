package lt.insoft.gallery.domain.image;

import lt.insoft.Image;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageRepository extends JpaRepository<Image, Integer> {
    List<Image> findByName(String name);
    List<Image> findAllById(int Id, Pageable pageable);
    Image findById(int id);
}
