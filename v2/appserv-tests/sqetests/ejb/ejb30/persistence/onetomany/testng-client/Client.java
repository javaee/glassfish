package pe.ejb.ejb30.persistence.toplinksample.client;

import java.io.*;
import java.util.*;
import javax.ejb.*;
import javax.naming.*;
import java.rmi.*;
import pe.ejb.ejb30.persistence.toplinksample.ejb.*;

import org.testng.Assert;
import org.testng.annotations.Configuration;
import org.testng.annotations.Test;


public class Client {

    private static @EJB StatelessInterface sless;
    List rows;
    Iterator i;

    public static void main(String[] args) {
        org.testng.TestNG testng = new org.testng.TestNG();
        testng.setTestClasses(
            new Class[] { pe.ejb.ejb30.persistence.toplinksample.client.Client.class } );
        testng.run();
    }

    @Configuration(beforeTestClass = true)
    public void setup() throws Exception {
        System.out.println("Client: invoking stateful setup");
        sless.setUp();
    }

    @Configuration(afterTestClass = true)
    public void cleanup() throws Exception {
        System.out.println("Cleanup: DELETING ROWS...");
        sless.cleanUp();
    }

    @Test
    public void testGetCustomerOrders() throws Exception {
        System.out.println("Client: getting customer orders");
        Collection coll = sless.getCustomerOrders(1);
        if (coll != null) {
            for (Iterator iterator=coll.iterator(); iterator.hasNext();)
                System.out.println((OrderEntity)iterator.next());
        }
        Assert.assertTrue((coll != null), "Got customers orders");
    }

    @Test
    public void testGetCustomerByName() throws Exception {
        System.out.println("Get customer by name and address");
        List rows = sless.getCustomers("Alice", "Santa Clara");
        if (rows != null) {
            for (Iterator iterator = rows.iterator(); iterator.hasNext();)
                System.out.println((CustomerEntity)iterator.next());
        }

        Assert.assertTrue((rows != null), "Got customers");
    }

    @Test
    public void testGetAllCustomers() throws Exception {
        System.out.println("Get all customers");
        List rows = sless.getAllCustomers();
        if (rows != null) {
            for (Iterator iterator = rows.iterator(); iterator.hasNext();)
                System.out.println((CustomerEntity)iterator.next());
        }

        Assert.assertTrue((rows != null), "Got all customers");
    }

    @Test
    public void testGetAllItemsByName() throws Exception {
        System.out.println("Get all items by name");
        List rows = sless.getAllItemsByName();

        if (rows != null) {
            for (Iterator iterator = rows.iterator(); iterator.hasNext();)
                System.out.println((ItemEntity)iterator.next());
        }
        Assert.assertTrue((rows != null), "Got all item by name");
    }

    @Test
    public void testGetAllOrdersByItem() throws Exception {
        System.out.println("Get all orders by item");
        List rows = sless.getAllOrdersByItem();

        if (rows != null) {
            for (Iterator iterator = rows.iterator(); iterator.hasNext();)
                System.out.println((ItemEntity)iterator.next());
        }
        Assert.assertTrue((rows != null), "Got all orders by item");
    }
}
