package lt.insoft.gallery.domain.image;

import lombok.RequiredArgsConstructor;
import lt.insoft.Image;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ImageService {
    private static final String IMAGE_PATH = "C:\\Users\\patrikas.styra\\Desktop\\Images\\";
    private final ImageRepository imageRepository;


    Page<Image> findPaginated(int page, int size) throws IllegalArgumentException {
        return imageRepository.findAll(PageRequest.of(page, size));
    }

    public List<Image> getAllImages() {
        return imageRepository.findAll();
    }

    public Image getImageByUuid(String uuid) {
        return imageRepository.findByUuidName(uuid);
    }

    public void saveImage(Image image) {
        imageRepository.save(image);
    }

    @Transactional
    public void deleteImage(int id) {
        Image imageToDelete = getImageById(id);
        imageRepository.deleteById(id);
        try {
            File imageFile = new File(IMAGE_PATH + imageToDelete.getUuidName());
            File imageThumbnailFile = new File(IMAGE_PATH + imageToDelete.getUuidName() + "small");
            if (imageFile.canWrite() && imageThumbnailFile.canWrite()) {
                imageFile.delete();
                imageThumbnailFile.delete();
                System.out.println("Files deleted");
            } else {
                throw new RuntimeException();
            }
        } catch (EmptyResultDataAccessException e) {
            System.out.println("There's no such entry in database");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Nera duombazej");
        }
    }

    public Image getImageById(int id) {
        return imageRepository.findById(id);
    }
}
