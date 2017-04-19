/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
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
