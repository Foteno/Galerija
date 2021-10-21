package lt.insoft.gallery.domain.image;

import lt.insoft.Image;
import lt.insoft.ImageFullDto;
import lt.insoft.ImagePreviewDto;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

public interface IImageService {
    @Transactional
    Page<ImagePreviewDto> findPaginated(int page, int size) throws IllegalArgumentException;

    default ImagePreviewDto convertToImageDto(Image image) {
        return new ImagePreviewDto(image.getName(), image.getDescription(), image.getUuid());
    }

    void saveImage(ImageFullDto imageDto);

    @Transactional
    void deleteImage(int id);

    Image getImageById(int id);
}
