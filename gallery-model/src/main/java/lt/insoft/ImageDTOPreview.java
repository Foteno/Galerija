package lt.insoft;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImageDTOPreview {
    private byte[] image;
    private String name;
    private String description;
}
