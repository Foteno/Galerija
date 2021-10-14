import lombok.RequiredArgsConstructor;
import lt.insoft.Image;
import lt.insoft.gallery.domain.image.ImageRepository;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
@RequiredArgsConstructor
class ImageRepositoryTest {
//sukurti simple test
    private final ImageRepository imageRepository;

    @Test
    public void testDelete() {
        Image image = new Image();
        imageRepository.save(image);

    }

}
