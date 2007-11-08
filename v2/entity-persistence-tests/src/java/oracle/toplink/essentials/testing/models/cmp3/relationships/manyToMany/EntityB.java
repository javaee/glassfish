package oracle.toplink.essentials.testing.models.cmp3.relationships.manyToMany;

import static javax.persistence.CascadeType.*;
import static javax.persistence.GenerationType.TABLE;

import javax.persistence.*;

import java.util.Collection;
import java.util.HashSet;

@Entity
@Table(name="CMP3_ENTITYB")
public class EntityB
{
    private int id;
    private String name;
    private Collection<EntityA> as;

    public EntityB() {
        as = new HashSet<EntityA>();
    }

    @Id
    @GeneratedValue(strategy=TABLE, generator="ENTITYB_TABLE_GENERATOR")
    @TableGenerator(
        name="ENTITYB_TABLE_GENERATOR",
        table="CMP3_ENTITYB_SEQ",
        pkColumnName="SEQ_NAME",
        valueColumnName="SEQ_COUNT",
        pkColumnValue="ENTITYB_SEQ"
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

    @ManyToMany(cascade=REMOVE, mappedBy="bs")
    public Collection<EntityA> getAs() {
        return as;
    }
    public void setAs(Collection<EntityA> as) {
        this.as = as;
    }
}