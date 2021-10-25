package lt.insoft.gallery.domain; // FIXME: kodėl ne po image/tag package?

import lt.insoft.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Integer> {
    Tag getByName(String name);
}
