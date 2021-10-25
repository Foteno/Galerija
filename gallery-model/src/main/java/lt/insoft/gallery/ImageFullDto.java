package lt.insoft.gallery;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class ImageFullDto {
    private int id;
    private String name;
    private String date;
    private String description;
    private String uuid;
    private Set<TagDto> tags;

    public ImageFullDto(String name, String date, String description, String uuid, Set<TagDto> tags) {
        this.name = name;
        this.date = date;
        this.description = description;
        this.uuid = uuid;
        this.tags = tags;
    }
}
