package lt.insoft.gallery.domain.image;

import lombok.RequiredArgsConstructor;
import lt.insoft.Image;
import lt.insoft.IngoingImageDto;
import org.imgscalr.Scalr;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;


import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.UUID;

@CrossOrigin
@RestController
@RequiredArgsConstructor
@RequestMapping("/image")
public class ImageController {
    private static final String IMAGE_PATH = "C:\\Users\\patrikas.styra\\Desktop\\Images\\";
    private static final String THUMBNAIL_SUFFIX = "small";
    private static final int HEIGHT = 200, WIDTH = 200;
    private final ImageService imageService;

    @ResponseBody
    @GetMapping(value = "{file-uuid}", produces = MediaType.IMAGE_JPEG_VALUE)
    private byte[] getImageByteArray(@PathVariable("file-uuid") String uuid) throws IOException {
        FileInputStream imageResponseFileInputStream;
        byte[] bytes = {};
        try {
            imageResponseFileInputStream = new FileInputStream(IMAGE_PATH + uuid);
            bytes = imageResponseFileInputStream.readAllBytes();
        } catch (FileNotFoundException e) {
            System.out.println("File wasn't found");
        }
        return bytes;
    }

    @GetMapping(params = {"page", "size"})
    public Page<Image> getPagedMetadata(@RequestParam("page") int page, @RequestParam("size") int size) {
        Page<Image> resultPage;
        try {
            resultPage = imageService.findPaginated(page, size);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Blogi parametrai");
        }
        if (page > resultPage.getTotalPages()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Bandoma gauti daugiau psl nei yra");
        }
        return resultPage;
    }

    @DeleteMapping("{id}")
    private void deleteImage(@PathVariable int id) {
        imageService.deleteImage(id);
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
            BufferedImage newImageBuffered = ImageIO.read(newImageFile);
            newImageThumbnailBuffered = Scalr.resize(newImageBuffered, WIDTH, HEIGHT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        File newImageThumbnailFile = new File(IMAGE_PATH + uuid + THUMBNAIL_SUFFIX);
        assert newImageThumbnailBuffered != null;
        ImageIO.write(newImageThumbnailBuffered, "png", newImageThumbnailFile);
        Image imageDetailsToDb = new Image(file.getName(), file.getDate(), file.getDescription(), uuid.toString());
        try {
            imageService.saveImage(imageDetailsToDb);
        } catch (DataIntegrityViolationException e) {
            System.out.println("A field is too big");
        }
        return imageDetailsToDb.getId();
    }

    @GetMapping("/test")
    public @ResponseBody
    String index() {
        return "It works";
    }
}
