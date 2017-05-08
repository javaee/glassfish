/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2006-2017 Oracle and/or its affiliates. All rights reserved.
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

package samples.ejb.stateless.simple.servlet;

import java.io.*; 
import java.util.*; 
import javax.servlet.*; 
import javax.naming.*; 
import javax.servlet.http.*; 
import javax.rmi.PortableRemoteObject;
import javax.ejb.*; 

import samples.ejb.stateless.simple.ejb.*; 

/**
 * This servlet is responsible for throwing the html pages for the HelloWorld application.
 */
public class GreeterServlet extends HttpServlet {  

   /**
    * The doGet method of the servlet. Handles all http GET request.
    * Required by the servlet specification.
    * @exception throws ServletException and IOException.
    */
   public void doGet (HttpServletRequest request,HttpServletResponse response) 
        throws ServletException, IOException { 

        javax.ejb.Handle beanHandle; 
        Greeter myGreeterBean; 
        GreeterHome myGreeterHome; 
        Greeter myGreeterRemote;
 
        InitialContext initContext = null; 
        Hashtable env = new java.util.Hashtable(1); 
        ResourceBundle rb = ResourceBundle.getBundle("LocalStrings", Locale.getDefault());

        System.out.println("\n"+rb.getString("greeting_servlet")+ "...");  

        System.out.println(rb.getString("retrieving_jndi")); 
        try { 
            initContext = new javax.naming.InitialContext(); 
        }  
        catch (Exception e) { 
          System.out.println(rb.getString("exception")+": " + e.toString()); 
          return; 
        } 

        try { 
            System.out.println(rb.getString("looking_up")); 
            String JNDIName = "java:comp/env/ejb/greeter"; 
            System.out.println(rb.getString("looking")+": " + JNDIName); 
            Object objref = initContext.lookup(JNDIName); 
            myGreeterHome = (GreeterHome)PortableRemoteObject.narrow(objref,
                                            GreeterHome.class);
        }  
        catch(Exception e) { 
          System.out.println(rb.getString("greeter_bean")+" - " +  
           rb.getString("is_registered")+"?: " + e.toString()); 
        return; 
        } 
        try { 
            System.out.println(rb.getString("creating")); 
            myGreeterRemote = myGreeterHome.create();  
        } 
        catch(CreateException e) { 
            System.out.println(rb.getString("could_not")+": "+  
            e.toString()); 
            return; 
        }  

        System.out.println(rb.getString("getting")); 
        String theMessage = myGreeterRemote.getGreeting();  
        System.out.println(rb.getString("got")+": " + theMessage); 

        System.out.println(rb.getString("storing")); 
        request.setAttribute("message", theMessage); 

        System.out.println(rb.getString("dispatching")); 
        response.setContentType("text/html"); 
        RequestDispatcher dispatcher; 
        dispatcher = getServletContext().getRequestDispatcher 
         ("/GreeterView.jsp"); 
        dispatcher.include(request, response); 
        return;  
    }  

   /**
    * The doPost method of the servlet. Handles all http POST request.
    * Required by the servlet specification.
    * @exception throws ServletException and IOException.
    */
    public void doPost (HttpServletRequest request,HttpServletResponse response) 
    throws ServletException, IOException { 
        doGet(request,response); 
    } 

   /**
    * Returns the servlet info as a String.
    * @return returns the servlet info as a String.
    */
    public String getServletInfo() { 
        ResourceBundle rb = ResourceBundle.getBundle("LocalStrings", Locale.getDefault());
        return rb.getString("call")+"."; 
    } 
} 
