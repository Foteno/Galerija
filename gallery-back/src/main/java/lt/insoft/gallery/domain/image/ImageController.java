package lt.insoft.gallery.domain.image;

import lombok.RequiredArgsConstructor;
import lt.insoft.ImagePreviewDto;
import lt.insoft.ImageFullDto;
import lt.insoft.IngoingImageDto;
import org.imgscalr.Scalr;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;


import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

@CrossOrigin
@RestController
@RequiredArgsConstructor
@RequestMapping("/image")
public class ImageController {
    private static final String IMAGE_PATH = "C:\\Users\\patrikas.styra\\Desktop\\Images\\"; // FIXME: ImageService irgi tokia pati konstanta. Kas su tuo negerai?
    private static final String THUMBNAIL_SUFFIX = "small";
    private static final int HEIGHT = 200;
    private static final int WIDTH = 200; // FIXME: pagal konvenciją kiekvienam laukui atskira eilutė
    private final IImageService imageService;

    @ResponseBody
    @GetMapping(value = "{file-uuid}", produces = MediaType.IMAGE_JPEG_VALUE)
    public byte[] getImageByteArray(@PathVariable("file-uuid") String uuid) throws IOException {
        FileInputStream imageResponseFileInputStream; // FIXME: kur uždaromas streamas? kokios problemos gali kilti neuždarinėjant atidarytų streamų?
        byte[] bytes = {};
        try {
            imageResponseFileInputStream = new FileInputStream(IMAGE_PATH + uuid);
            bytes = imageResponseFileInputStream.readAllBytes();
        } catch (FileNotFoundException e) {
            System.out.println("File wasn't found getImageByteArray"); // FIXME: ką tokiu atveju gaus useris? ką turėtų gauti?
        }
        return bytes;
    }

    @GetMapping(value = "/details/{file-uuid}")
    public ImageFullDto getImageFullDetails(@PathVariable("file-uuid") String uuid) {
        return imageService.findByUuid(uuid);
    }

    @GetMapping(params = {"page", "size"})
    public Page<ImagePreviewDto> getPagedMetadata(@RequestParam("page") int page, @RequestParam("size") int size) {
        Page<ImagePreviewDto> resultPage;
        try {
            resultPage = imageService.findPaginated(page, size);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Blogi parametrai");
        }
        if (page > resultPage.getTotalPages()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Bandoma gauti daugiau psl nei yra"); // FIXME: ar čia geriau 404 ar 400?
        }
        return resultPage;
    }

    @DeleteMapping("{id}")
    private void deleteImage(@PathVariable int id) {
        try {
            imageService.deleteImage(id);
        } catch (RuntimeException e) { // FIXME: kas čia galėtų būti geriau?

        }

    }

    //could throw MaxUploadSizeExceededException
    @PostMapping(consumes = "multipart/form-data")
    private int postImage(@ModelAttribute IngoingImageDto file) throws IOException {
        MultipartFile image = file.getImage();
        UUID uuid = UUID.randomUUID();
        File newImageFile = new File(IMAGE_PATH + uuid);
        BufferedImage newImageThumbnailBuffered = null;
        image.transferTo(newImageFile);
        try {
            BufferedImage newImageBuffered = ImageIO.read(newImageFile); // FIXME: kodėl reikia skaityti iš disko image, kurį ką tik ten įrašei?
            newImageThumbnailBuffered = Scalr.resize(newImageBuffered, WIDTH, HEIGHT);
        } catch (IOException e) {
            e.printStackTrace(); // FIXME: naudoti logginimą. Įvykus klaidai tęsiame darbą toliau lyg niekur nieko?
        }
        File newImageThumbnailFile = new File(IMAGE_PATH + uuid + THUMBNAIL_SUFFIX);
        assert newImageThumbnailBuffered != null;
        ImageIO.write(newImageThumbnailBuffered, "png", newImageThumbnailFile); // FIXME: man vis dar nepatinka PNG/JPEG maišymas kode. Neaišku, kur/ar įvyksta konvertavimas

        ImageFullDto imageDetailsToDb = new ImageFullDto(file.getName(), file.getDate(),
                file.getDescription(), uuid.toString(), file.getTags());
        try {
            imageService.saveImage(imageDetailsToDb);
        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
        }
        return 1; // FIXME: kodėl 1?
    }

    @GetMapping("/test") // FIXME: useless
    public @ResponseBody
    String index() {
        return "It works";
    }
}
