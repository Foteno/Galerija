package lt.insoft;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;


import java.io.*;
import java.util.List;
import java.util.UUID;

@RestController
public class ImageController {
    final
    ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @GetMapping("/images")
    private List<Image> getAllImages() {
        return imageService.getAllImages();
    }

    @GetMapping("/image/{id}")
    private Image getImage(@PathVariable int id) {
        return imageService.getImageById(id);
    }

    @DeleteMapping("image/{id}")
    private void deleteImage(@PathVariable int id) {
        imageService.deleteImage(id);
    }

    @PostMapping(value = "/image", consumes = {"multipart/form-data"})
    private int postImage(@ModelAttribute ImageDTO file, ModelMap modelMap) throws IOException {
        modelMap.addAttribute("file", file);
        MultipartFile image = file.getImage();
        UUID uuid = UUID.randomUUID();
        File newFile = new File("C:\\Users\\patrikas.styra\\Desktop\\Images\\" + uuid);
        image.transferTo(newFile);

        Image pic = new Image(file.getName(), file.getDate(), file.getDescription());
        pic.setUuidName(uuid.toString());
        imageService.saveImage(pic);
        return pic.getId();
    }

    //lmao
    @GetMapping("/")
    public @ResponseBody
    String index() {
        return "It works";
    }

    @GetMapping(value = "/picture/{filename:.+}", produces = MediaType.IMAGE_JPEG_VALUE)
    public byte[] getImage(@PathVariable String filename) throws IOException {
        InputStream in;
        try {
            in = new FileInputStream("C:\\Users\\patrikas.styra\\Desktop\\Images\\" + filename);
        } catch (FileNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Image not found");
        }
        return in.readAllBytes();
    }




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
