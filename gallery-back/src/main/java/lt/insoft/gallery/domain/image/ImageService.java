package lt.insoft.gallery.domain.image;

import lt.insoft.Image;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ImageService {
    final
    ImageRepository imageRepository;

    public ImageService(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
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
