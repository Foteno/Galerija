package lt.insoft;

import lombok.*;

import javax.persistence.*;


@Getter
@Setter
@Entity
@Table
@NoArgsConstructor
public class Image {

    public Image(String name, String date, String description, String uuidName) {
        this.name = name;
        this.date = date;
        this.description = description;
        this.uuidName = uuidName;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private int id;
    @Column
    private String name;
    @Column
    private String date;
    @Column
    private String description;
    @Column
    private String uuidName;
}