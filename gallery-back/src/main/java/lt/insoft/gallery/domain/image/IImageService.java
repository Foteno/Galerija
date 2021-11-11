package lt.insoft.gallery.domain.image;

import lt.insoft.gallery.Image;
import lt.insoft.gallery.ImageFullDto;
import lt.insoft.gallery.ImagePreviewDto;
import lt.insoft.gallery.domain.user.UserDetailsImpl;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface IImageService {

    Page<ImagePreviewDto> findImageByTagUsingSpecification(UserDetailsImpl userDetails, int page, int size, String query);

    Page<ImagePreviewDto> findPaginatedByNameOrDescription(UserDetailsImpl userDetails, int page, int size, String query);

    ImageFullDto findByUuid(String uuid);

    int saveImage(ImageFullDto imageDto);

    int updateImage(ImageFullDto imageFullDto);

    void deleteImage(UserDetailsImpl userDetails, int id);

    void saveImageLocally(MultipartFile image, String uuid) throws IOException;

    Image getImageByThumbnailUuid(String uuid);
}
