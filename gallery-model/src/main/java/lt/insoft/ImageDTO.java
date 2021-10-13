package lt.insoft;

import org.springframework.web.multipart.MultipartFile;

public class ImageDTO {
    private MultipartFile image;
    private String name;
    private String date;
    private String description;
    private String UUID_Name;

    public ImageDTO(MultipartFile image, String name, String date, String description, String UUID_Name) {
        this.image = image;
        this.name = name;
        this.date = date;
        this.description = description;
        this.UUID_Name = UUID_Name;
    }

    public ImageDTO() {
    }

    public MultipartFile getImage() {
        return image;
    }

    public String getUUID_Name() {
        return UUID_Name;
    }

    public void setUUID_Name(String UUID_Name) {
        this.UUID_Name = UUID_Name;
    }

    public void setImage(MultipartFile image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
