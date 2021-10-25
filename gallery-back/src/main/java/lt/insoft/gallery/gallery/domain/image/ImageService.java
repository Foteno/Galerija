package lt.insoft.gallery.gallery.domain.image;

import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import lt.insoft.gallery.Image;
import lt.insoft.gallery.ImagePreviewDto;
import lt.insoft.gallery.ImageFullDto;
import lt.insoft.gallery.Tag;
import lt.insoft.gallery.TagDto;
import lt.insoft.gallery.gallery.domain.tag.TagRepository;
import lt.insoft.gallery.gallery.domain.exceptions.ImageNotDeletedRuntimeException;
import lt.insoft.gallery.gallery.domain.constants.Constants;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@CommonsLog
public class ImageService implements IImageService {
    private static final String IMAGE_PATH = Constants.IMAGE_STORAGE_PATH;
    private final ImageRepository imageRepository;
    private final TagRepository tagRepository;

    @Override
    @Transactional
    public Page<ImagePreviewDto> findPaginated(int page, int size) {
        Page<Image> imagePage = imageRepository.findAll(PageRequest.of(page, size));
        return imagePage.map(this::convertToImageDto);
    }

    @Override
    public ImagePreviewDto convertToImageDto(Image image) {
        return new ImagePreviewDto(image.getName(), image.getDescription(), image.getUuid());
    }

    @Override
    @Transactional
    public ImageFullDto findByUuid(String uuid) {
        Image image = imageRepository.findByUuid(uuid);
        if (image == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        Set<TagDto> set = getTagDtos(image);
        ImageFullDto imageFullDto = new ImageFullDto(image.getName(), image.getDate(),
                image.getDescription(), uuid, set);
        imageFullDto.setId(image.getId());
        return imageFullDto;
    }

    @Override
    @Transactional
    public int updateImage(ImageFullDto imageFullDto) {
        Set<Tag> tags = convertToTagFromTagDto(imageFullDto);

        Image image = imageRepository.getById(imageFullDto.getId());
        image.setName(imageFullDto.getName());
        image.setDate(imageFullDto.getDate());
        image.setDescription(imageFullDto.getDescription());
        image.setTags(tags);

        imageRepository.save(image);
        return 1;
    }

    @Override
    @Transactional
    public void saveImage(ImageFullDto imageFullDto) {
        Set<Tag> tags =  convertToTagFromTagDto(imageFullDto);

        Image image = new Image(imageFullDto.getName(), imageFullDto.getDate(), imageFullDto.getDescription(),
                imageFullDto.getUuid(), tags);
        imageRepository.save(image);
    }

    private Set<Tag> convertToTagFromTagDto(ImageFullDto imageFullDto) {
        List<String> tagNames = new ArrayList<>();
        for (TagDto tag : imageFullDto.getTags()) {
            tagNames.add(tag.getName());
        }
        return tagRepository.getByNameIn(tagNames);
    }


    @Override
    @Transactional
    public void deleteImage(int id) {
        try {
            Image imageToDelete = imageRepository.findById(id);
            imageRepository.deleteById(id);
            File imageFile = new File(IMAGE_PATH + imageToDelete.getUuid());
            File imageThumbnailFile = new File(IMAGE_PATH + imageToDelete.getUuid() + "small");
            if (imageFile.canWrite() && imageThumbnailFile.canWrite()) {
                deleteImageFile(imageFile, "imageFile");
                deleteImageFile(imageThumbnailFile, "imageThumbnailFile");
            } else {
                throw new ImageNotDeletedRuntimeException("Can't write to imageFile or imageThumbnailFile");
            }
        } catch (EmptyResultDataAccessException e) {
            log.error("There's no such entry in database");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Nera duombazej");
        }
    }

    private void deleteImageFile(File imageFile, String s) {
        if (imageFile.delete()) {
            log.info(s + " deleted");
        } else {
            throw new ImageNotDeletedRuntimeException(s + " not deleted");
        }
    }

    @Override
    public ImageFullDto getImageById(int id) {
        Image image = imageRepository.findById(id);
        Set<TagDto> set = getTagDtos(image);
        ImageFullDto imageFullDto = new ImageFullDto(image.getName(), image.getDate(), image.getDescription(),
                image.getUuid(), set);
        imageFullDto.setId(image.getId());
        return imageFullDto;
    }

    private Set<TagDto> getTagDtos(Image image) {
        Set<TagDto> set = new HashSet<>();
        for (Tag tag : image.getTags()) {
            set.add(new TagDto(tag.getName()));
        }
        return set;
    }
}
