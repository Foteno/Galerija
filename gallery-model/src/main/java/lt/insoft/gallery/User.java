package lt.insoft.gallery;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class User {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column
    private String username;

    @Column
    private String dateCreated;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
    private Set<Image> images;

    public User(String username, String dateCreated, Set<Image> images) {
        this.username = username;
        this.dateCreated = dateCreated;
        this.images = images;
    }
}
