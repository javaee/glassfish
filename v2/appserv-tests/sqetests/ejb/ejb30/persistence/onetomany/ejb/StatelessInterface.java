package pe.ejb.ejb30.persistence.toplinksample.ejb;

import java.util.*;

public interface StatelessInterface{

    public String getMessage();
    public void setMessage(String msg);
    public void setUp();
    public void cleanUp();
    public List getCustomers(String name, String city);
    public List getAllCustomers();
    public List getAllItemsByName();
    public List getAllOrdersByItem();
    public Collection getCustomerOrders(int custId);

}
