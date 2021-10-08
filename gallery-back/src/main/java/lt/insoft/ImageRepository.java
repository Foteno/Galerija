package lt.insoft;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ImageRepository extends CrudRepository<Image, Integer> {
    List<Image> findByName(String name);
    Image findById(int id);
}
