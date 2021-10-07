package lt.insoft;

import org.springframework.http.MediaType;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

@RestController
public class ImageController {

    @GetMapping("/")
    public @ResponseBody
    String index() {
        return "It finally works?";
    }

    @GetMapping(value = "/image/{filename:.+}", produces = MediaType.IMAGE_JPEG_VALUE)
    public byte[] getImage(@PathVariable String filename) throws IOException {
        InputStream in;
        try {
            in = new FileInputStream("C:\\Users\\patrikas.styra\\Desktop\\Images\\" + filename);
        } catch (FileNotFoundException e) {
            //String string = "File not found";
            return null;
        }
        return in.readAllBytes();
    }

    @RequestMapping(value = "/images", method = RequestMethod.GET)
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
    }

    @RequestMapping(value = "/postImage", method = RequestMethod.POST)
    public String postImage(@RequestParam("file") MultipartFile file, ModelMap modelMap) throws IOException {
        modelMap.addAttribute("file", file);
        //pakeisti filepath
        File newFile = new File("C:\\Users\\patrikas.styra\\Desktop\\Images\\" + file.getOriginalFilename());
        file.transferTo(newFile);

        return file.getOriginalFilename() + " received.";
    }


}
