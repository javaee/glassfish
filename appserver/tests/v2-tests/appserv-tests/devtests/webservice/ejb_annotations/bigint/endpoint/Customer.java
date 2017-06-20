package endpoint;

import java.math.BigInteger;
import javax.persistence.*;

@Entity public class Customer implements java.io.Serializable {
    BigInteger bigInteger;
    @Id String name;
    public Customer(){}
    public Customer(BigInteger bi, String name) {
        this.bigInteger = bi;
        this.name = name;
    }
    public BigInteger getBigInteger() { return this.bigInteger;}
    public String getName() { return this.name;}
    public void setBigInteger(BigInteger b) { this.bigInteger = b;}
    public void setName(String n) { this.name = n;}
}
