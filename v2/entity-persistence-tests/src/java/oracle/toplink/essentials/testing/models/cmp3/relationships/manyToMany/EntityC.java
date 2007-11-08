package oracle.toplink.essentials.testing.models.cmp3.relationships.manyToMany;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.TableGenerator;
import javax.persistence.ManyToMany;
import javax.persistence.JoinTable;
import javax.persistence.JoinColumn;
import javax.persistence.GenerationType;
import javax.persistence.CascadeType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import java.util.Collection;
import java.util.HashSet;

@Entity
@Table(name="CMP3_ENTITYC")
public class EntityC
{
    private int id;
    private String name;
    private Collection<EntityD> ds = new HashSet<EntityD>();

    public EntityC() {
    }

    @Id
    @GeneratedValue(strategy= GenerationType.TABLE, generator="ENTITYC_TABLE_GENERATOR")
    @TableGenerator(
        name="ENTITYC_TABLE_GENERATOR",
        table="CMP3_ENTITYC_SEQ",
        pkColumnName="SEQ_NAME",
        valueColumnName="SEQ_COUNT",
        pkColumnValue="ENTITYC_SEQ"
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

    @OneToMany(cascade={CascadeType.ALL})
    @JoinTable(
            name="CMP3_UNIDIR_ENTITYC_ENTITYD",
            joinColumns=
            @JoinColumn(name="ENTITYC_ID", referencedColumnName="ID"),
            inverseJoinColumns=
            @JoinColumn(name="ENTITYD_ID", referencedColumnName="ID")
    )
    public Collection<EntityD> getDs() {
        return ds;
    }
    public void setDs(Collection<EntityD> ds) {
        this.ds = ds;
    }
}
