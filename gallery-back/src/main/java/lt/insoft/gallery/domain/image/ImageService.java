package lt.insoft.gallery.domain.image;

import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import lt.insoft.Image;
import lt.insoft.ImagePreviewDto;
import lt.insoft.ImageFullDto;
import lt.insoft.Tag;
import lt.insoft.TagDto;
import lt.insoft.gallery.domain.TagRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
@CommonsLog
class ImageService implements IImageService {
    private static final String IMAGE_PATH = "C:\\Users\\patrikas.styra\\Desktop\\Images\\";
    private final ImageRepository imageRepository;
    private final TagRepository tagRepository;

    @Override
    @Transactional
    public Page<ImagePreviewDto> findPaginated(int page, int size) throws IllegalArgumentException { // FIXME: kada reikalingas throws?
        Page<Image> imagePage = imageRepository.findAll(PageRequest.of(page, size));
        return imagePage.map(this::convertToImageDto);
    }

    @Transactional // FIXME: čia nėra @Override, kitur yra. Kaip turėtų būti?
    public ImageFullDto findByUuid(String uuid) {
        Image image = imageRepository.findByUuid(uuid);
        Set<TagDto> set = new HashSet<>();
        for (Tag tag : image.getTags()) {
            set.add(new TagDto(tag.getName()));
        }
        return new ImageFullDto(image.getName(), image.getDate(),
                image.getDescription(), uuid, set);
    }

    @Override
    @Transactional
    public void saveImage(ImageFullDto imageDto) {
        Set<Tag> tags = new HashSet<>();
        for (TagDto tag: imageDto.getTags()) {
            String name = tag.getName();
            Tag tagCurrent = tagRepository.getByName(name); // FIXME: kokios čia galimos problemos?
            tags.add(Objects.requireNonNullElseGet(tagCurrent, () -> new Tag(name)));
        }

        // FIXME: nelabai aišku, ką reiškia image.date, bet data turėtų būti current date arba gaunama per REST
        Image image = new Image(imageDto.getName(), "1990-11-11 10:48", imageDto.getDescription(), imageDto.getUuid(), tags);
        imageRepository.save(image);
    }


    @Override
    @Transactional
    public void deleteImage(int id) {
        Image imageToDelete = getImageById(id);
        imageRepository.deleteById(id);
        try {
            File imageFile = new File(IMAGE_PATH + imageToDelete.getUuid());
            File imageThumbnailFile = new File(IMAGE_PATH + imageToDelete.getUuid() + "small");
            if (imageFile.canWrite() && imageThumbnailFile.canWrite()) {
                if (imageFile.delete()) {
                    log.info("deleted");
                    System.out.println("imageFile deleted"); // FIXME: vietoj println būtų gerai naudoti normalią Logging implemnetaciją
                } else {
                    throw new RuntimeException();
                }
                if (imageThumbnailFile.delete()) { // FIXME: labai daug dublikuoto kodo, galbūt galima kažkaip supaprastinti?
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

    @Override
    public Image getImageById(int id) { // FIXME: "public" reikalingas?
        return imageRepository.findById(id);
    }
}
