package lt.insoft.gallery;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Set;


@Getter
@Setter
@Entity
@Table
@NoArgsConstructor
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @Column
    private String name;

    @Column
    private String date;

    @Column
    private String description;

    @Column
    private String uuid;

    @ManyToMany(cascade = { CascadeType.MERGE, CascadeType.PERSIST } , fetch = FetchType.LAZY)
    @JoinTable(
            name = "Image_Tag",
            joinColumns = @JoinColumn(name = "image_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<Tag> tags;

    public Image(User user, String name, String date, String description, String uuid, Set<Tag> tags) {
        this.user = user;
        this.name = name;
        this.date = date;
        this.description = description;
        this.uuid = uuid;
        this.tags = tags;
    }
}
