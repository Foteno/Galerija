package lt.insoft.gallery.domain.image;

import lombok.RequiredArgsConstructor;
import lt.insoft.Image;
import lt.insoft.IngoingImageDTO;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;


import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@CrossOrigin
@RestController
@RequiredArgsConstructor
public class ImageController {
    private final String IMAGE_PATH = "C:\\Users\\patrikas.styra\\Desktop\\Images\\";
    final ImageService imageService;

    @GetMapping("/images")
    private List<Image> getAllImages() {
        return imageService.getAllImages();
    }


    @GetMapping(value = "/paged/getImages", params = {"page", "size"})
    private List<byte[]> getImageByteArrayList(@RequestParam("page") int page, @RequestParam("size") int size) throws IOException {
        Page<Image> pagedImages = getPagedImages(page, size);
        List<byte[]> images = new ArrayList<>();
        try {
            for (Image image : pagedImages.getContent()) {
                images.add(getImageByteArray(image.getUuidName()));
            }
        } catch (FileNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return images;
    }

    @GetMapping(value = "/paged/getMeta", params = {"page", "size"})
    public Page<Image> getPagedMetadata(@RequestParam("page") int page, @RequestParam("size") int size) {
        return getPagedImages(page, size);
    }

    @DeleteMapping("image/{id}")
    private void deleteImage(@PathVariable int id) {
        try {
            Image image = imageService.getImageById(id);
            imageService.deleteImage(id);
            File file = new File(IMAGE_PATH + image.getUuidName());
            file.delete();
        } catch (EmptyResultDataAccessException e) {
            System.out.println("There's no such entry in database");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } catch (NullPointerException e) {
            System.out.println("There's no such file");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

    }

    @PostMapping(value = "/image", consumes = "multipart/form-data")
    private int postImage(@ModelAttribute IngoingImageDTO file, ModelMap modelMap) throws IOException {
        modelMap.addAttribute("file", file);
        MultipartFile image = file.getImage();
        UUID uuid = UUID.randomUUID();
        File newFile = new File(IMAGE_PATH + uuid);
        image.transferTo(newFile);

        Image pic = new Image(file.getName(), file.getDate(), file.getDescription(), uuid.toString());
        imageService.saveImage(pic);
        return pic.getId();
    }

    private Page<Image> getPagedImages(int page, int size) {
        Page<Image> resultPage = imageService.findPaginated(page, size);
        if (page > resultPage.getTotalPages()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "vstk niekur nesimato");
        }
        return resultPage;
    }

    /*private Image getImageMetadata(int id) {
        Image image = imageService.getImageById(id);
        if (image == null) {
            System.out.println("Image data not found");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Image data not found");
        }
        return image;
    }*/

    private byte[] getImageByteArray(String uuid) throws IOException {
        FileInputStream in = new FileInputStream(IMAGE_PATH + uuid);
        return in.readAllBytes();
    }

    //lmao
    @GetMapping("/")
    public @ResponseBody
    String index() {
        return "It works";
    }
}
