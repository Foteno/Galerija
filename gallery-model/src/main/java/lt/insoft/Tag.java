package lt.insoft;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Getter
@Setter
@Entity
@Table
@NoArgsConstructor
public class Tag implements Serializable { // FIXME: kodėl Serializable? Kada serializuojama? Ar reikia/nereikia?

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private int id; // FIXME: atskirti fieldus newline'ais

    @Column(unique = true)
    private String name;

    public Tag(String name) {
        this.name = name;
    }
}
