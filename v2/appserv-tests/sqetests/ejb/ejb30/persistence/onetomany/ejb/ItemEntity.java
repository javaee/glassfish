//Copyright (c) 1998, 2004, Oracle Corporation. All rights reserved.

package pe.ejb.ejb30.persistence.toplinksample.ejb;

import javax.persistence.*;

@Entity
@Table(name = "CMP3_ITEM")
@NamedQuery(name = "findAllItemsByName", query =
    "SELECT OBJECT(item) FROM ItemEntity item WHERE item.name = ?1") 
public class ItemEntity implements java.io.Serializable {

    private Integer itemId;
    private int version;
    private String name;
    private String description;

    public ItemEntity() { }

    public ItemEntity(int id, String name) {
        this.setItemId(new Integer(id));
        this.setName(name);
    }

    @Id
    @Column(name = "ITEM_ID")
    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer id) {
        this.itemId = id;
    }

    @Version@Column(name = "ITEM_VERSION") 
    protected int getVersion() {
        return version;
    }

    protected void setVersion(int version) {
        this.version = version;
    }


    @Column(name="DESCRIPTION")
    public String getDescription() {
        return description;
    }

    public void setDescription(String desc) {
        this.description = desc;
    }


    @Column(name="NAME")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String toString(){
        return "ID: "+itemId+": name :"+name;
    }
}
