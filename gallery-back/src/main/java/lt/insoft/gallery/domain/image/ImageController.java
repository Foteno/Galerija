package lt.insoft.gallery.domain.image;

import lt.insoft.Image;
import lt.insoft.ImageDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
public class ImageController {
    private final String IMAGE_PATH = "C:\\Users\\patrikas.styra\\Desktop\\Images\\";
    final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @GetMapping("/images")
    private List<Image> getAllImages() {
        return imageService.getAllImages();
    }

    @GetMapping(value = "/image/{id}", produces = "multipart/form-data")
    private ResponseEntity<ImageDTO> getImage(@PathVariable int id) throws IOException {
        Image image = getImageMetadata(id);

        byte[] bytes = getImageByteArray(image.getUuidName());


/*
        MultipartFile multipartFile =
                new MockMultipartFile(im.getName(), im.getName(), "image/jpeg", in.readAllBytes());
        ImageDTO imageDTO =
                new ImageDTO(multipartFile, im.getName(), im.getDate(), im.getDescription(), im.getUuidName());
       */ return new ResponseEntity<>(new ImageDTO(), HttpStatus.OK);
    }

    @DeleteMapping("image/{id}")
    private void deleteImage(@PathVariable int id) {
        imageService.deleteImage(id);
    }

    @PostMapping(value = "/image", consumes = "multipart/form-data")
    private int postImage(@ModelAttribute ImageDTO file, ModelMap modelMap) throws IOException {
        modelMap.addAttribute("file", file);
        MultipartFile image = file.getImage();
        String string = image.getContentType();
        UUID uuid = UUID.randomUUID();
        File newFile = new File(IMAGE_PATH + uuid);
        image.transferTo(newFile);

        Image pic = new Image(file.getName(), file.getDate(), file.getDescription(), uuid.toString());
        imageService.saveImage(pic);
        return pic.getId();
    }


    private Image getImageMetadata(int id) {
        Image image = imageService.getImageById(id);
        if (image == null) {
            System.out.println("Image data not found");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Image data not found");
        }
        return image;
    }
    private byte[] getImageByteArray(String uuid) throws IOException {
        FileInputStream in = null;
        try {
            in = new FileInputStream(IMAGE_PATH + uuid);
        } catch (FileNotFoundException e) {
            System.out.println("Failas nerastas...");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "It not found");
        }
        return in.readAllBytes();
    }
    //lmao
    @GetMapping("/")
    public @ResponseBody
    String index() {
        return "It works";
    }

    /*@GetMapping(value = "/picture/{filename:.+}", produces = MediaType.IMAGE_JPEG_VALUE)
    public byte[] getImage(@PathVariable String filename) throws IOException {
        InputStream in;
        try {
            in = new FileInputStream("C:\\Users\\patrikas.styra\\Desktop\\Images\\" + filename);
        } catch (FileNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Image not found");
        }
        return in.readAllBytes();
    }*/




    /*@RequestMapping(value = "/images", method = RequestMethod.GET)
    @ResponseBody
    public String getMultipleImages() {
        ArrayList<File> files = new ArrayList<>();
        File folder = new File("C:\\Users\\patrikas.styra\\Desktop\\Images");
        Collections.addAll(files, Objects.requireNonNull(folder.listFiles()));
        for (File file : files) {
            System.out.println(file.getName());
            System.out.println(file.getAbsolutePath());
        }
        return "a;";
    }*/
}
