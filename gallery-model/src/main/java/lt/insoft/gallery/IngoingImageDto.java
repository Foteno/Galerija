package lt.insoft.gallery;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class IngoingImageDto {
    private MultipartFile image;
    private String name;
    private String date;
    private String description;
    private String uuid;
    private Set<TagDto> tags;
}
