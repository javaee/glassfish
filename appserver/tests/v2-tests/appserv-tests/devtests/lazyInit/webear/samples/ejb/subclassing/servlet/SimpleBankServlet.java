/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

package samples.ejb.subclassing.servlet;

import java.io.*;
import java.util.*; 
import javax.servlet.*; 
import javax.naming.*; 
import javax.servlet.http.*; 
import javax.rmi.PortableRemoteObject;
import javax.ejb.*; 

import samples.ejb.subclassing.ejb.*; 

public class SimpleBankServlet extends HttpServlet {  


  InitialContext initContext = null;
  CustomerSavingsHome customerSavingsHome = null;
  CustomerCheckingHome customerCheckingHome = null;
  CustomerSavings customerSavings = null;
  CustomerChecking customerChecking = null;
  Hashtable env = new java.util.Hashtable(1);
  String JNDIName = null;
  Object objref = null;

  public void init()
  {
    doLookup();
  }

  public void doGet (HttpServletRequest request,HttpServletResponse response) 
        throws ServletException, IOException { 
    doPost(request, response);
  }  

  /** handles the HTTP POST operation **/ 
  public void doPost (HttpServletRequest request,HttpServletResponse response) 
        throws ServletException, IOException { 
    System.out.println("SimpleBankServlet is executing");
    String SSN = request.getParameter("SSN");

    String message = "";
    String jsp = "";
    String lastName = "";
    String firstName = "";
    String address1 = "";
    String address2 = "";
    String city = "";
    String state = "";
    String zipCode = "";
    long currentSavingsBalance = 0;
    long currentCheckingBalance = 0;
     
    String action = request.getParameter("action");
    if (action.equals("Create"))
    {
      message = "Add Customer";
      jsp = "/SimpleBankAdd.jsp";
    }
    else if (action.equals("Add Customer")) 
    {
      System.out.println("Add Customer button pressed");
      SSN = request.getParameter("SSN");
      lastName = request.getParameter("lastName");
      firstName = request.getParameter("firstName");
      address1 = request.getParameter("address1");
      address2 = request.getParameter("address2");
      city = request.getParameter("city");
      state = request.getParameter("state");
      zipCode = request.getParameter("zipCode");

      try {
        System.out.println("Creating the customer savings remote bean");
        customerSavings = customerSavingsHome.create(SSN, lastName, firstName, address1, address2, city, state, zipCode);
      } catch (Exception e) {
        System.out.println("Could not create the customer savings remote bean : " + e.toString());
        return;
      }
      message = "Customer Added.";
      jsp = "/SimpleBankMessage.jsp";
    }
    else if (action.equals("Edit"))
    {
      try {
        System.out.println("Finding the customer savings remote bean");
        customerSavings = customerSavingsHome.findByPrimaryKey(SSN);
      } catch (Exception e) {
        System.out.println("Could not find the customer remote bean : " + e.toString());
        return;
      }
      jsp = "/SimpleBankEdit.jsp";
    }
    else if (action.equals("Delete"))
    {
      try {
        System.out.println("Finding the customer savings remote bean");
        customerSavings = customerSavingsHome.findByPrimaryKey(SSN);
      } catch (Exception e) {
        System.out.println("Could not find the customer savings remote bean : " + e.toString());
        return;
      }
      message = "Delete Customer";
      jsp = "/SimpleBankDelete.jsp";
    }
    else if (action.equals("Delete Customer"))
    {
      try {
        customerSavingsHome.findByPrimaryKey(SSN).remove();
      } catch (Exception e) {
        System.out.println("Could not delete the customer savings bean : " + e.toString());
        return;
      }
      message = "Customer Deleted.";
      jsp = "/SimpleBankMessage.jsp";
    }  


    else if (action.equals("Update"))
    {
      try {
        System.out.println("Finding the customersavings remote bean");
        customerSavings = (CustomerSavings)customerSavingsHome.findByPrimaryKey(SSN);
      } catch (Exception e) {
        System.out.println("Could not find the customer savings remote bean : " + e.toString());
        return;
      }

      try {
        System.out.println("Finding the customerchecking remote bean");
        customerChecking = (CustomerChecking)customerCheckingHome.findByPrimaryKey(SSN);
      } catch (Exception e) {
        System.out.println("Could not find the customer checking remote bean : " + e.toString());
        return;
      }

      System.out.println("Transaction Complete");
      String operationSavings = request.getParameter("operationSavings");
      System.out.println("operationSavings = " + operationSavings);
      String operationChecking = request.getParameter("operationChecking");
      System.out.println("operationChecking = " + operationChecking);
      String amountSavings = request.getParameter("amountSavings");
      String amountChecking = request.getParameter("amountChecking");
      if (operationSavings.equals("credit"))
      {
        customerSavings.doCredit(Long.parseLong(amountSavings), CustomerEJB.SAVINGS);
        customerChecking.doCredit(Long.parseLong(amountSavings), CustomerEJB.SAVINGS);
      } else {
        customerSavings.doDebit(Long.parseLong(amountSavings), CustomerEJB.SAVINGS);
        customerChecking.doDebit(Long.parseLong(amountSavings), CustomerEJB.SAVINGS);
      }
      if (operationChecking.equals("credit"))
      {
        customerChecking.doCredit(Long.parseLong(amountChecking), CustomerEJB.CHECKING);
        customerSavings.doCredit(Long.parseLong(amountChecking), CustomerEJB.CHECKING);
      } else {
        customerChecking.doDebit(Long.parseLong(amountChecking), CustomerEJB.CHECKING);
        customerSavings.doDebit(Long.parseLong(amountChecking), CustomerEJB.CHECKING);
      }
      jsp = "/SimpleBankEdit.jsp";
    }

    if (customerSavings != null)
    {
      lastName = customerSavings.getLastName();
      firstName = customerSavings.getFirstName();
      address1 = customerSavings.getAddress1();
      address2 = customerSavings.getAddress2();
      city = customerSavings.getCity();
      state = customerSavings.getState();
      zipCode = customerSavings.getZipCode();
      SSN = customerSavings.getSSN();
      currentSavingsBalance = customerSavings.getSavingsBalance();
      currentCheckingBalance = customerSavings.getCheckingBalance();
    }

    System.out.println("storing the values in the request object");
    request.setAttribute("lastName", lastName);
    request.setAttribute("firstName", firstName);
    request.setAttribute("address1", address1);
    request.setAttribute("address2", address2);
    request.setAttribute("city", city);
    request.setAttribute("state", state);
    request.setAttribute("zipCode", zipCode);
    request.setAttribute("SSN", SSN);
    request.setAttribute("currentSavingsBalance", String.valueOf(currentSavingsBalance));
    request.setAttribute("currentCheckingBalance", String.valueOf(currentCheckingBalance));
    request.setAttribute("message", message);
    response.setContentType("text/html");
    RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(jsp);
    dispatcher.include(request, response);
    return;
  } 

  public void doLookup()
  {
    try {
      initContext = new javax.naming.InitialContext();
    } catch (Exception e) {
      System.out.println("Exception occured when creating InitialContext: " + e.toString());
      return;
    }

    try {
      System.out.println("Looking up customer savings bean home interface");
      JNDIName = "java:comp/env/ejb/customerSavings";
      System.out.println("Looking up: " + JNDIName);
      objref = initContext.lookup(JNDIName);
      customerSavingsHome = (CustomerSavingsHome)PortableRemoteObject.narrow(objref, CustomerSavingsHome.class);
    } catch (Exception e) {
      System.out.println("Customer savings bean home not found - Is the bean registered with JNDI? : " + e.toString());
      return;
    }

    try {
      System.out.println("Looking up customer checking bean home interface");
      JNDIName = "java:comp/env/ejb/customerChecking";
      System.out.println("Looking up: " + JNDIName);
      objref = initContext.lookup(JNDIName);
      customerCheckingHome = (CustomerCheckingHome)PortableRemoteObject.narrow(objref, CustomerCheckingHome.class);
    } catch (Exception e) {
      System.out.println("Customer checking bean home not found - Is the bean registered with JNDI? : " + e.toString());
      return;
    }
  }
}
