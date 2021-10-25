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
import org.springframework.http.MediaType;
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
    @GetMapping(value = "{file-uuid}", produces = MediaType.IMAGE_JPEG_VALUE)
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
        saveImage(imageFile, uuid);
        imageFullDto.setName(file.getName());
        imageFullDto.setDate(file.getDate());
        imageFullDto.setDescription(file.getDescription());
        imageFullDto.setTags(file.getTags());

        imageService.updateImage(imageFullDto);

        System.out.println(imageFullDto.getName());
        return 1;
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

    @GetMapping("/test")
    public @ResponseBody
    String index() {
        return "It works";
    }
}
