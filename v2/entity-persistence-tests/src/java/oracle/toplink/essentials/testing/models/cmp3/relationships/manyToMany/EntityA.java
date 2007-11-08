package oracle.toplink.essentials.testing.models.cmp3.relationships.manyToMany;

import static javax.persistence.CascadeType.*;
import static javax.persistence.GenerationType.TABLE;

import javax.persistence.*;

import java.util.Collection;
import java.util.HashSet;

@Entity
@Table(name="CMP3_ENTITYA")
public class EntityA
{
    private int id;
    private String name;
    private Collection<EntityB> bs;

    public EntityA() {       
        bs = new HashSet<EntityB>();
    }

    @Id
    @GeneratedValue(strategy=TABLE, generator="ENTITYA_TABLE_GENERATOR")
    @TableGenerator(
        name="ENTITYA_TABLE_GENERATOR",
        table="CMP3_ENTITYA_SEQ",
        pkColumnName="SEQ_NAME",
        valueColumnName="SEQ_COUNT",
        pkColumnValue="ENTITYA_SEQ"
    )
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ManyToMany(cascade={PERSIST, MERGE})
    @JoinTable(
            name="CMP3_ENTITYA_ENTITYB",
            joinColumns=
            @JoinColumn(name="ENTITYA_ID", referencedColumnName="ID"),
            inverseJoinColumns=
            @JoinColumn(name="ENTITYB_ID", referencedColumnName="ID")
    )    
    public Collection<EntityB> getBs() {
        return bs;
    }
    public void setBs(Collection<EntityB> bs) {
        this.bs = bs;
    }
}