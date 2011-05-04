/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package j2eeguide.product;

import java.util.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class ProductClient {

   private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

   public static void main(String[] args) {

       try {

           Context initial = new InitialContext();
           Object objref = initial.lookup("java:comp/env/MyProduct");

           ProductHome home = 
               (ProductHome)PortableRemoteObject.narrow(objref, 
                                            ProductHome.class);
	    System.out.println("START");
            stat.addDescription("j2eeguide_product");

           Product duke = home.create("123", "Ceramic Dog", 10.00);
           System.out.println(duke.getDescription() + ": " + duke.getPrice());
           duke.setPrice(14.00);
           System.out.println(duke.getDescription() + ": " + duke.getPrice());

           duke = home.create("456", "Wooden Duck", 13.00);
           duke = home.create("999", "Ivory Cat", 19.00);
           duke = home.create("789", "Ivory Cat", 33.00);
           duke = home.create("876", "Chrome Fish", 22.00);

           Product earl = home.findByPrimaryKey("876");
           System.out.println(earl.getDescription() + ": " + earl.getPrice());

           Collection c = home.findByDescription("Ivory Cat");
           Iterator i = c.iterator();

           while (i.hasNext()) {
              Product product = (Product)i.next();
              String productId = (String)product.getPrimaryKey();
              String description = product.getDescription();
              double price = product.getPrice(); 
              System.out.println(productId + ": " + description + " " + price);
           }

           c = home.findInRange(10.00, 20.00);
           i = c.iterator();

           while (i.hasNext()) {
              Product product = (Product)i.next();
              String productId = (String)product.getPrimaryKey();
              double price = product.getPrice(); 
              System.out.println(productId + ": " + price);
           }
            stat.addStatus("ejbclient j2eeguide_product", stat.PASS);
            System.out.println("FINISH");

       } catch (Exception ex) {
           System.err.println("Caught an exception." );
           ex.printStackTrace();
            stat.addStatus("ejbclient j2eeguide_product", stat.FAIL);
       }
          stat.printSummary("j2eeguide_product");
   } 
} 
