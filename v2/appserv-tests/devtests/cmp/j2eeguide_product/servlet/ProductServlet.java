/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package j2eeguide.product;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.rmi.PortableRemoteObject;
import javax.naming.InitialContext;

import j2eeguide.product.*;

public class ProductServlet extends HttpServlet {

   public void doGet (HttpServletRequest req, HttpServletResponse res)
                      throws ServletException, IOException {

      res.setContentType("text/html");
      PrintWriter out = res.getWriter();

      out.println("<html>");
      out.println("<head>");
      out.println("<title>Product Sample App for CMP</title>");
      out.println("</head>");
      out.println("<body>");

       try {
					
	   InitialContext ic = new InitialContext();

	   System.out.println("looking up java:comp/env/ejb/MyProduct");
           Object objref = ic.lookup("java:comp/env/ejb/MyProduct");
           System.out.println("lookup ok");

           ProductHome home = 
               (ProductHome)PortableRemoteObject.narrow(objref, 
                                            ProductHome.class);

           Product duke = home.create("123", "Ceramic Dog", 10.00);
           out.println("<BR>" + duke.getDescription() + ": " + duke.getPrice());
           duke.setPrice(14.00);
           out.println("<BR>" + duke.getDescription() + ": " + duke.getPrice());

           duke = home.create("456", "Wooden Duck", 13.00);
           duke = home.create("999", "Ivory Cat", 19.00);
           duke = home.create("789", "Ivory Cat", 33.00);
           duke = home.create("876", "Chrome Fish", 22.00);

           Product earl = home.findByPrimaryKey("876");
           out.println("<BR>" + earl.getDescription() + ": " + earl.getPrice());

           Collection c = home.findByDescription("Ivory Cat");
           Iterator i = c.iterator();

           while (i.hasNext()) {
              Product product = (Product)i.next();
              String productId = (String)product.getPrimaryKey();
              String description = product.getDescription();
              double price = product.getPrice(); 
              out.println("<BR>" + productId + ": " + description + " " + price);
           }

           c = home.findInRange(10.00, 20.00);
           i = c.iterator();

           while (i.hasNext()) {
              Product product = (Product)i.next();
              String productId = (String)product.getPrimaryKey();
              double price = product.getPrice(); 
              out.println("<BR>" + productId + ": " + price);
           }

       } catch (Exception ex) {
           System.err.println("Caught an exception." );
           ex.printStackTrace();
       }

      out.println("</body>");
      out.println("</html>");
   }

} 
