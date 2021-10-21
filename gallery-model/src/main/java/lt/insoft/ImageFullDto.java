package lt.insoft;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
public class ImageFullDto {
    private String name;
    private String date;
    private String description;
    private String uuid;
    private Set<TagDto> tags;
}
