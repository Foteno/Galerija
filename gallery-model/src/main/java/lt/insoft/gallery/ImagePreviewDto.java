package lt.insoft.gallery;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImagePreviewDto {
    private String name;
    private String description;
    private String uuid;

    public ImagePreviewDto(String name, String description, String uuid) {
        this.name = name;
        this.description = description;
        this.uuid = uuid;
    }
}
