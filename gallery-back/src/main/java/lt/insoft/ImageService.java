package lt.insoft;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ImageService {
    final
    ImageRepository imageRepository;

    public ImageService(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    public List<Image> getAllImages() {
        List<Image> images = new ArrayList<>();
        imageRepository.findAll().forEach(images::add);
        return images;
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
