package oracle.toplink.essentials.testing.models.cmp3.relationships.manyToMany;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.TableGenerator;
import javax.persistence.ManyToMany;
import javax.persistence.GenerationType;
import javax.persistence.CascadeType;
import javax.persistence.JoinTable;
import javax.persistence.JoinColumn;
import java.util.Collection;
import java.util.HashSet;

@Entity
@Table(name="CMP3_ENTITYD")
public class EntityD
{
    private int id;
    private String name;

    public EntityD() {
    }

    @Id
    @GeneratedValue(strategy= GenerationType.TABLE, generator="ENTITYD_TABLE_GENERATOR")
    @TableGenerator(
        name="ENTITYD_TABLE_GENERATOR",
        table="CMP3_ENTITYD_SEQ",
        pkColumnName="SEQ_NAME",
        valueColumnName="SEQ_COUNT",
        pkColumnValue="ENTITYD_SEQ"
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
}
