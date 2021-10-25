package lt.insoft.gallery.domain.image;

import lt.insoft.Image;
import lt.insoft.ImageFullDto;
import lt.insoft.ImagePreviewDto;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

public interface IImageService {
    @Transactional // FIXME: anotacija ir čia, ir implementacijoje. kaip turėtų būti?
    Page<ImagePreviewDto> findPaginated(int page, int size) throws IllegalArgumentException;

    default ImagePreviewDto convertToImageDto(Image image) { // FIXME: default metodo nereiktų turėti be kažkokios priežasties tam
        return new ImagePreviewDto(image.getName(), image.getDescription(), image.getUuid());
    }

    void saveImage(ImageFullDto imageDto);

    @Transactional // FIXME: tas pats dėl anotacijos
    void deleteImage(int id);

    Image getImageById(int id);
}
