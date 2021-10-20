package lt.insoft;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;


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
}
