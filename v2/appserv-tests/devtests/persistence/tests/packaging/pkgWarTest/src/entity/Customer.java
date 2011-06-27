package entity;

import java.io.Serializable;
import javax.persistence.*;
import static javax.persistence.CascadeType.*;

import java.util.Collection;
import java.util.ArrayList;

/**
 * @author Marina Vatkina
 */

@Entity
@Table(name="PKG_CUSTOMER")
public class Customer implements Serializable {

    private int id;
    private String name;
    private Collection<Order> orders = new ArrayList<Order>();

    @Id
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

    @OneToMany(cascade=ALL, mappedBy="customer")
    public Collection<Order> getOrders() {
        return orders;
    }

    public void setOrders(Collection<Order> newValue) {
        this.orders = newValue;
    }

}
