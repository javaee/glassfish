/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2003-2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
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
