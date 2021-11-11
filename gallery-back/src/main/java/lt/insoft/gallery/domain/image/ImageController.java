package lt.insoft.gallery.domain.image;

import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import lt.insoft.gallery.ImagePreviewDto;
import lt.insoft.gallery.ImageFullDto;
import lt.insoft.gallery.IngoingImageDto;
import lt.insoft.gallery.domain.constants.Constants;
import lt.insoft.gallery.domain.exceptions.ImageNotDeletedRuntimeException;
import lt.insoft.gallery.domain.user.UserDetailsImpl;
import lt.insoft.gallery.domain.user.UserService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequiredArgsConstructor
@RequestMapping("/image")
@CommonsLog
public class ImageController {
    private static final String IMAGE_PATH = Constants.IMAGE_STORAGE_PATH;
    private final IImageService imageService;
    private final UserService userService;

    @ResponseBody
    @GetMapping(value = "{file-uuid}")
    @Secured({"ROLE_user", "ROLE_admin"})
    public byte[] getImageByteArray(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                    @PathVariable("file-uuid") String uuid) throws IOException {
        if (userService.isNotAllowedUser(uuid, userDetails)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        try (FileInputStream imageResponseFileInputStream = new FileInputStream(IMAGE_PATH + uuid)) {
            return imageResponseFileInputStream.readAllBytes();
        } catch (FileNotFoundException e) {
            log.error("File wasn't found getImageByteArray");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping(consumes = "multipart/form-data", value = "/details/{file-uuid}")
    @Secured({"ROLE_user", "ROLE_admin"})
    public int putImageFullDetails(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                   @ModelAttribute IngoingImageDto file,
                                   @PathVariable("file-uuid") String uuid) throws IOException {
        if (userService.isNotAllowedUser(uuid, userDetails)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        MultipartFile imageFile = file.getImage();
        if (!imageFile.getName().isBlank()) {
            imageService.saveImageLocally(imageFile, uuid);
        }

        ImageFullDto imageFullDto = new ImageFullDto(file.getName(), file.getDate(),
                file.getDescription(), uuid, file.getTags());

        int updatedImageId = imageService.updateImage(imageFullDto);

        log.info(imageFullDto.getName());
        return updatedImageId;
    }

    @GetMapping(value = "/details/{file-uuid}")
    @Secured({"ROLE_user", "ROLE_admin"})
    public ImageFullDto getImageFullDetails(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                            @PathVariable("file-uuid") String uuid) {
        if (userService.isNotAllowedUser(uuid, userDetails)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return imageService.findByUuid(uuid);
    }

    @GetMapping(params = {"page", "size", "name"})
    @Secured({"ROLE_user", "ROLE_admin"})
    public Page<ImagePreviewDto> getPagedImagesByNameAndDescription(
            @AuthenticationPrincipal UserDetailsImpl userDetails, @RequestParam("page") int page,
            @RequestParam("size") int size, @RequestParam("name") String name) {
        Page<ImagePreviewDto> resultPage;
        resultPage = imageService.findPaginatedByNameOrDescription(userDetails, page, size, name);
        if (page > resultPage.getTotalPages()) {
            log.error("Try to get more pages, than there is getPagedImagesByNameAndDescription");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bandoma gauti daugiau psl nei yra");
        }
        return resultPage;
    }


    @GetMapping(params = {"page", "size", "tag"})
    @Secured({"ROLE_user", "ROLE_admin"})
    public Page<ImagePreviewDto> getPagedImagesByTag(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                     @RequestParam("page") int page, @RequestParam("size") int size,
                                                     @RequestParam("tag") String name) {
        Page<ImagePreviewDto> resultPage;
        resultPage = imageService.findImageByTagUsingSpecification(userDetails, page, size, name);
        if (page > resultPage.getTotalPages()) {
            log.error("Try to get more pages, than there is getPagedImagesByTag");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bandoma gauti daugiau psl nei yra");
        }
        return resultPage;
    }

    @DeleteMapping("{id}")
    @Secured({"ROLE_user", "ROLE_admin"})
    public void deleteImage(@AuthenticationPrincipal UserDetailsImpl userDetails, @PathVariable int id) {
        try {
            imageService.deleteImage(userDetails, id);
        } catch (ImageNotDeletedRuntimeException e) {
            log.error(e.getMessage());
        }

    }

    @PostMapping(consumes = "multipart/form-data")
    @Secured({"ROLE_user", "ROLE_admin"})
    public int postImage(@ModelAttribute IngoingImageDto file) throws IOException {
        MultipartFile image = file.getImage();
        String uuid = UUID.randomUUID().toString();
        imageService.saveImageLocally(image, uuid);

        ImageFullDto imageDetailsToDb = new ImageFullDto(file.getName(), file.getDate(),
                file.getDescription(), uuid, file.getTags());
        return imageService.saveImage(imageDetailsToDb);
    }

    @GetMapping("/test1")
    @ResponseBody
    public Page<ImagePreviewDto> findCount(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return imageService.findPaginatedByNameOrDescription(userDetails, 0, 5, "ba");
    }

    @GetMapping("/test")
    @ResponseBody
    public String index() {
        return "it works!";
    }
}
