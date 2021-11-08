package lt.insoft.gallery.domain.image;

import lt.insoft.gallery.ImageFullDto;
import lt.insoft.gallery.ImagePreviewDto;
import org.springframework.data.domain.Page;

public interface IImageService {

    Page<ImagePreviewDto> findImageByTagUsingSpecification(int page, int size, String query);

    Page<ImagePreviewDto> findPaginatedByNameOrDescription(int page, int size, String query);

    ImageFullDto findByUuid(String uuid);

    int saveImage(ImageFullDto imageDto);

    int updateImage(ImageFullDto imageFullDto);

    void deleteImage(int id);

}
