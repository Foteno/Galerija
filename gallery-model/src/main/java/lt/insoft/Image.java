package lt.insoft;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ConstraintMode; // FIXME: nenaudojami importai
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.HashSet;
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
    private int id; // FIXME: fieldus, kurie turi tiek daug anotacijų dėl geresnio skaitomumo siūlyčiau atskyrinėti tuščia eilute

    @Column
    private String name;

    @Column
    private String date;
    @Column
    private String description;
    @Column
    private String uuid;

    @Setter(value = AccessLevel.NONE)
    @ManyToMany(cascade = { CascadeType.MERGE, CascadeType.PERSIST } , fetch = FetchType.LAZY)
    @JoinTable(
            name = "Image_Tag",
            joinColumns = @JoinColumn(name = "image_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<Tag> tags = new HashSet<>(); // FIXME: minor pastaba: tokiais atvejais patogu inicializuoti List/Set reikšmę

    public Image(String name, String date, String description, String uuid, Set<Tag> tags) {
        this.name = name;
        this.date = date;
        this.description = description;
        this.uuid = uuid;
        this.tags = tags;
    }
}
