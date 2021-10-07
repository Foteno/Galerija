package lt.insoft;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.http.MediaType;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@RestController
public class ImageController {
    @GetMapping("/")
    public @ResponseBody String index() {
        return "It finally works?";
    }
    @GetMapping(value = "/image", produces = MediaType.IMAGE_JPEG_VALUE)
    public @ResponseBody byte[] getImage() throws IOException {
        InputStream in = getClass().getResourceAsStream("Untitled.jpg");
        if (in == null)
            return new byte[] {1,3,127};
        return in.readAllBytes();
    }
    @RequestMapping(value = "/postImage", method = RequestMethod.POST)
    public String postImage(@RequestParam("file") MultipartFile file, ModelMap modelMap) throws IOException {
        modelMap.addAttribute("file", file);
        File newFile = new File("C:\\Users\\patrikas.styra\\Desktop\\b.jpg");
        file.transferTo(newFile);
        return "abcdefghijkl";

    }
}
