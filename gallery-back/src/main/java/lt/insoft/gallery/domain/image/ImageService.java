package lt.insoft.gallery.domain.image;

import lombok.RequiredArgsConstructor;
import lt.insoft.Image;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ImageService {
    final ImageRepository imageRepository;


    Page<Image> findPaginated(int page, int size) {
        return imageRepository.findAll(PageRequest.of(page, size));
    }

    public List<Image> getAllImages() {
        return imageRepository.findAll();
    }

    public void saveImage(Image image) {
        imageRepository.save(image);
    }

    public void deleteImage(int id) {
        imageRepository.deleteById(id);
    }

    public Image getImageById(int id) {
        return imageRepository.findById(id);
    }
}
