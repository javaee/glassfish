package endpoint;

import java.math.BigInteger;
import java.util.*;
import javax.jws.*;
import javax.ejb.*;
import javax.persistence.*;

@Stateless
@WebService
public class CustomerManager {
    @PersistenceContext EntityManager em;

    @WebMethod
    public Customer createCustomer( BigInteger code, String name) {
        System.out.println("createCustomer " + code + " " + name);
        Customer customer = new Customer( code, name );
        em.persist( customer );
        return customer;
    }

    @WebMethod
    public Collection getCustomerList() {
        System.out.println("getCustomerList");
        String ejbQL = "SELECT c FROM Customer c";
        return em.createQuery( ejbQL ).getResultList();
    }

    @WebMethod
    public int removeCustomer(String id) {
        System.out.println("removeCustomer");
        String ejbQL = "DELETE FROM Customer c WHERE c.name = \""+id+"\"";
        int ret = em.createQuery( ejbQL ).executeUpdate();
 	return ret;
    }
}
