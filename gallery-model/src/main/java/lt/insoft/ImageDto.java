package lt.insoft;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class ImageDto {
    private String name;
    private String date;
    private String description;
    private String uuid;
    private Set<TagDto> tags;

    public ImageDto(String name, String date, String description, String uuid, Set<TagDto> tags) {
        this.name = name;
        this.date = date;
        this.description = description;
        this.uuid = uuid;
        this.tags = tags;
    }
}
