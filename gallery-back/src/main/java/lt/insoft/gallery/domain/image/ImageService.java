package lt.insoft.gallery.domain.image;

import lombok.RequiredArgsConstructor;
import lt.insoft.Image;
import lt.insoft.ImageDto;
import lt.insoft.Tag;
import lt.insoft.TagDto;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ImageService {
    private static final String IMAGE_PATH = "C:\\Users\\patrikas.styra\\Desktop\\Images\\";
    private final ImageRepository imageRepository;

    @Transactional
    Page<ImageDto> findPaginated(int page, int size) throws IllegalArgumentException {
        Page<Image> imagePage = imageRepository.findAll(PageRequest.of(page, size));
        return imagePage.map(this::convertToImageDto);
    }

    public void saveImage(Image image) {

        imageRepository.save(image);
    }


    private ImageDto convertToImageDto (Image image) {
        Set<Tag> tags = image.getTags();
        Set<TagDto> tagDtos = new HashSet<>();
        for (Tag tag: tags) {
            tagDtos.add(new TagDto(tag.getId(), tag.getName()));
        }
        return new ImageDto(image.getName(), image.getDate(),
                image.getDescription(), image.getUuid(), tagDtos);
    }

    @Transactional
    public void deleteImage(int id) {
        Image imageToDelete = getImageById(id);
        imageRepository.deleteById(id);
        try {
            File imageFile = new File(IMAGE_PATH + imageToDelete.getUuid());
            File imageThumbnailFile = new File(IMAGE_PATH + imageToDelete.getUuid() + "small");
            if (imageFile.canWrite() && imageThumbnailFile.canWrite()) {
                if (imageFile.delete()) {
                    System.out.println("imageFile deleted");
                } else {
                    throw new RuntimeException();
                }
                if (imageThumbnailFile.delete()) {
                    System.out.println("imageThumbnailFile deleted");
                } else {
                    throw new RuntimeException();
                }

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
