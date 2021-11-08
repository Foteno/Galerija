package lt.insoft.gallery.domain.tag;

import lt.insoft.gallery.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Set;

public interface TagRepository extends JpaRepository<Tag, Integer> {
    Set<Tag> getByNameIn(Collection<String> name);
}
