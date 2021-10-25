package lt.insoft.gallery.gallery.domain.image;

import lt.insoft.gallery.Image;
import lt.insoft.gallery.ImageFullDto;
import lt.insoft.gallery.ImagePreviewDto;
import org.springframework.data.domain.Page;

public interface IImageService {

    Page<ImagePreviewDto> findPaginated(int page, int size) throws IllegalArgumentException;

    ImagePreviewDto convertToImageDto(Image image);

    ImageFullDto findByUuid(String uuid);

    void saveImage(ImageFullDto imageDto);

    int updateImage(ImageFullDto imageFullDto);

    void deleteImage(int id);

    ImageFullDto getImageById(int id);
}
