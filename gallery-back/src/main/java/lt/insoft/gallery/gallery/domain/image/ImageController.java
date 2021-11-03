package lt.insoft.gallery.gallery.domain.image;

import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import lt.insoft.gallery.ImagePreviewDto;
import lt.insoft.gallery.ImageFullDto;
import lt.insoft.gallery.IngoingImageDto;
import lt.insoft.gallery.gallery.domain.exceptions.ImageNotDeletedRuntimeException;
import lt.insoft.gallery.gallery.domain.constants.Constants;
import org.imgscalr.Scalr;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;


import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

@CrossOrigin
@RestController
@RequiredArgsConstructor
@RequestMapping("/image")
@CommonsLog
public class ImageController {
    private static final String IMAGE_PATH = Constants.IMAGE_STORAGE_PATH;
    private static final String THUMBNAIL_SUFFIX = "small";
    private static final int HEIGHT = Constants.THUMBNAIL_HEIGHT;
    private static final int WIDTH = Constants.THUMBNAIL_WIDTH;
    private final IImageService imageService;

    @ResponseBody
    @GetMapping(value = "{file-uuid}")
    public byte[] getImageByteArray(@PathVariable("file-uuid") String uuid) throws IOException {
        byte[] bytes;
        try (FileInputStream imageResponseFileInputStream = new FileInputStream(IMAGE_PATH + uuid)) {
            bytes = imageResponseFileInputStream.readAllBytes();
        } catch (FileNotFoundException e) {
            log.error("File wasn't found getImageByteArray");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return bytes;
    }

    @PutMapping(consumes = "multipart/form-data", value = "/details/{file-uuid}")
    public int putImageFullDetails(@ModelAttribute IngoingImageDto file, @PathVariable("file-uuid") String uuid) throws IOException {
        MultipartFile imageFile = file.getImage();
        ImageFullDto imageFullDto = imageService.findByUuid(uuid);
        if (imageFullDto == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        if (!imageFile.getName().equals("")) { // FIXME: o jei name null? o jei " "? arba non-breaking space arba TAB simbolis?
            saveImage(imageFile, uuid);
        }
        imageFullDto.setName(file.getName());
        imageFullDto.setDate(file.getDate());
        imageFullDto.setDescription(file.getDescription());
        imageFullDto.setTags(file.getTags());

        int a = imageService.updateImage(imageFullDto); // FIXME: kas yra "a"?

        log.info(imageFullDto.getName());
        return a;
    }

    @GetMapping(value = "/details/{file-uuid}")
    public ImageFullDto getImageFullDetails(@PathVariable("file-uuid") String uuid) {
        return imageService.findByUuid(uuid);
    }

    @GetMapping(params = {"page", "size", "name"})
    public Page<ImagePreviewDto> getPagedImagesByNameAndDescription(@RequestParam("page") int page, @RequestParam("size") int size,
                                                                    @RequestParam("name") String name) {
        Page<ImagePreviewDto> resultPage;
        try {
            resultPage = imageService.findPaginatedByNameOrDescription(page, size, name);
        } catch (IllegalArgumentException e) { // FIXME: iš kur gali būti throwinamas šis exceptionas? Paprastai tokias klaidas reiktų spręsti ne catch'inant iš neaišku kur giliai ateinančius exceptionus, bet per validaciją
            log.error("Wrong parameters getPagedImagesByNameAndDescription"); // FIXME: jei išloggintum exception'ą, metodo pavadinimo čia nurodyti nereiktų
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Blogi parametrai");
        }
        if (page > resultPage.getTotalPages()) {
            log.error("Try to get more pages, than there is getPagedImagesByNameAndDescription");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bandoma gauti daugiau psl nei yra");
        }
        return resultPage;
    }


    @GetMapping(params = {"page", "size", "tag"})
    public Page<ImagePreviewDto> getPagedImagesByTag(@RequestParam("page") int page, @RequestParam("size") int size,
                                                     @RequestParam("tag") String name) {
        Page<ImagePreviewDto> resultPage;
        try {
            resultPage = imageService.findImageByTagUsingSpecification(page, size, name);
        } catch (IllegalArgumentException e) {
            log.error("Wrong parameters getPagedImagesByTag");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Blogi parametrai");
        }
        if (page > resultPage.getTotalPages()) {
            log.error("Try to get more pages, than there is getPagedImagesByTag");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bandoma gauti daugiau psl nei yra");
        }
        return resultPage;
    }

    @DeleteMapping("{id}")
    private void deleteImage(@PathVariable int id) {
        try {
            imageService.deleteImage(id);
        } catch (ImageNotDeletedRuntimeException e) {
            log.error(e.getMessage());
        }

    }

    //could throw MaxUploadSizeExceededException
    @PostMapping(consumes = "multipart/form-data")
    private int postImage(@ModelAttribute IngoingImageDto file) throws IOException {
        MultipartFile image = file.getImage();
        UUID uuid = UUID.randomUUID();
        saveImage(image, uuid.toString());

        ImageFullDto imageDetailsToDb = new ImageFullDto(file.getName(), file.getDate(),
                file.getDescription(), uuid.toString(), file.getTags());
        imageService.saveImage(imageDetailsToDb);
        return 1;
    }

    private void saveImage(MultipartFile image, String uuid) throws IOException {
        BufferedImage newImageThumbnailBuffered = null;

        File newImageFile = new File(IMAGE_PATH + uuid);
        InputStream inputStream = image.getInputStream();
        byte[] buffer = new byte[inputStream.available()];
        if (inputStream.read(buffer) == -1) {
            log.error("No bytes read");
        }
        inputStream.close();
        if (buffer.length == 0) {
            return;
        }

        try (OutputStream outputStream = new FileOutputStream(newImageFile)) {
            outputStream.write(buffer);

            ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
            BufferedImage newImageBuffered = ImageIO.read(bais);
            newImageThumbnailBuffered = Scalr.resize(newImageBuffered, WIDTH, HEIGHT);
        } catch (IOException e) {
            log.error("Couldn't open OutputStream or write to BufferedImage");
            e.printStackTrace();
        }
        File newImageThumbnailFile = new File(IMAGE_PATH + uuid + THUMBNAIL_SUFFIX);
        assert newImageThumbnailBuffered != null;
        ImageIO.write(newImageThumbnailBuffered, "png", newImageThumbnailFile);
    }

    @GetMapping("/test1")
    @ResponseBody
    public Page<ImagePreviewDto> findCount() {
        return imageService.findPaginatedByNameOrDescription(0, 5, "ba");
    }

    @GetMapping("/test")
    @ResponseBody
    public String index() {
        return "it works!";
    }
}
