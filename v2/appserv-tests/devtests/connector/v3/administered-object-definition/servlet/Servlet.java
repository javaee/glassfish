/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.ejb30.hello.session3;

import java.io.*;

import javax.resource.AdministeredObjectDefinitions;
import javax.resource.AdministeredObjectDefinition;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import javax.naming.*;
import javax.ejb.EJB;
import javax.ejb.EJBs;
import javax.ejb.EJBException;
import javax.annotation.Resource;
import javax.transaction.UserTransaction;

@EJB(name = "helloStateless3", beanInterface = Hello.class)
@EJBs({@EJB(name = "helloStateless4", beanName = "HelloEJB",
        beanInterface = Hello.class),
        @EJB(name = "helloStateful3", beanInterface = HelloStateful.class)})

@AdministeredObjectDefinitions(
        value = {

                @AdministeredObjectDefinition(
                        description="global-scope resource defined by @AdministeredObjectDefinition",
                        name = "java:global/env/Servlet_ModByDD_AdminObject",
                        resourceAdapterName="aod-ra",
                        className = "connector.MyAdminObject",
                        properties = {"org.glassfish.admin-object.resType=connector.MyAdminObject"}
                ),

                @AdministeredObjectDefinition(
                        description="global-scope resource defined by @AdministeredObjectDefinition",
                        name = "java:global/env/Servlet_AdminObject",
                        className = "connector.MyAdminObject",
                        resourceAdapterName="aod-ra",
                        properties = {"org.glassfish.admin-object.resType=connector.MyAdminObject"}
                ),

                @AdministeredObjectDefinition(
                        description="application-scope resource defined by @AdministeredObjectDefinition",
                        name = "java:app/env/Servlet_AdminObject",
                        className = "connector.MyAdminObject",
                        resourceAdapterName="aod-ra",
                        properties = {"org.glassfish.admin-object.resType=connector.MyAdminObject"}
                ),
                
                @AdministeredObjectDefinition(
                        description="module-scope resource defined by @AdministeredObjectDefinition",
                        name = "java:module/env/Servlet_AdminObject",
                        className = "connector.MyAdminObject",
                        resourceAdapterName="aod-ra",
                        properties = {"org.glassfish.admin-object.resType=connector.MyAdminObject"}
                ),
                
                @AdministeredObjectDefinition(
                        description="component-scope resource defined by @AdministeredObjectDefinition",
                        name = "java:comp/env/Servlet_AdminObject",
                        className = "connector.MyAdminObject",
                        resourceAdapterName="aod-ra",
                        properties = {"org.glassfish.admin-object.resType=connector.MyAdminObject"}
                )
        }
)
@WebServlet(name = "Servlet", urlPatterns = {"/servlet"})
public class Servlet extends HttpServlet {

    private static final long serialVersionUID = 3735011494952348746L;

    @EJB
    private  Hello helloStateless;
    
    @EJB(beanName = "HelloStatefulEJB")
    private  HelloStateful helloStateful;

    @Resource
    private UserTransaction ut;

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        System.out.println("In Connector-Resource-Definition-Test::servlet... init()");
    }

    public void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();

        try {

            // Connector-Resource-Definition through Annotation
            lookupAdminObject("java:global/env/Servlet_AdminObject", true);
            lookupAdminObject("java:app/env/Servlet_AdminObject", true);
            lookupAdminObject("java:module/env/Servlet_AdminObject", true);
            lookupAdminObject("java:comp/env/Servlet_AdminObject", true);

            lookupAdminObject("java:global/env/HelloStatefulEJB_Annotation_AdminObject", true);
            lookupAdminObject("java:app/env/HelloStatefulEJB_Annotation_AdminObject", true);
            lookupAdminObject("java:module/env/HelloStatefulEJB_Annotation_AdminObject", false);
            lookupAdminObject("java:comp/env/HelloStatefulEJB_Annotation_AdminObject", false);

            lookupAdminObject("java:global/env/HelloEJB_Annotation_AdminObject", true);
            lookupAdminObject("java:app/env/HelloEJB_Annotation_AdminObject", false);
            lookupAdminObject("java:module/env/HelloEJB_Annotation_AdminObject", false);
            lookupAdminObject("java:comp/env/HelloEJB_Annotation_AdminObject", false);

            // Connector-Resource-Definition through DD
            lookupAdminObject("java:global/env/EAR_AdminObject", true);
            lookupAdminObject("java:app/env/EAR_AdminObject", true);

            lookupAdminObject("java:global/env/Web_DD_AdminObject", true);
            lookupAdminObject("java:app/env/Web_DD_AdminObject", true);
            lookupAdminObject("java:module/env/Web_DD_AdminObject", true);
            lookupAdminObject("java:comp/env/Web_DD_AdminObject", true);

            lookupAdminObject("java:global/env/HelloStatefulEJB_DD_AdminObject", true);
            lookupAdminObject("java:app/env/HelloStatefulEJB_DD_AdminObject", true);
            lookupAdminObject("java:module/env/HelloStatefulEJB_DD_AdminObject", false);
            lookupAdminObject("java:comp/env/HelloStatefulEJB_DD_AdminObject", false);

            lookupAdminObject("java:global/env/HelloEJB_DD_AdminObject", true);
            lookupAdminObject("java:app/env/HelloEJB_DD_AdminObject", true);
            lookupAdminObject("java:module/env/HelloEJB_DD_AdminObject", false);
            lookupAdminObject("java:comp/env/HelloEJB_DD_AdminObject", false);

            System.out.println("Servlet successful lookup of administered-object-definitions !");

            System.out.println("beginning tx");
            ut.begin();

            // invoke method on the EJB
            System.out.println("invoking stateless ejb");
            helloStateless.hello();

            System.out.println("committing tx");
            ut.commit();
            System.out.println("committed tx");


            System.out.println("invoking stateless ejb");
            helloStateful.hello();

            System.out.println("successfully invoked ejbs");

            System.out.println("accessing connections");

            try {
                MyThread thread = new MyThread(helloStateful);
                thread.start();

                sleepFor(2);
                helloStateful.ping();
                //throw new EJBException("Did not get ConcurrentAccessException");
            } catch (javax.ejb.ConcurrentAccessException conEx) {
                ;   //Everything is fine
            } catch (Throwable th) {
                throw new EJBException("Got some wierd exception: " + th);
            }

            System.out.println("successfully accessed administered object definitions");

            out.println("<HTML> <HEAD> <TITLE> Servlet Output </TITLE> </HEAD> <BODY BGCOLOR=white>");
            out.println("<CENTER> <FONT size=+1 COLOR=blue>Servlet :: All information I can give </FONT> </CENTER> <p> ");
            out.println("<FONT size=+1 color=red> Context Path :  </FONT> " + req.getContextPath() + "<br>");
            out.println("<FONT size=+1 color=red> Servlet Path :  </FONT> " + req.getServletPath() + "<br>");
            out.println("<FONT size=+1 color=red> Path Info :  </FONT> " + req.getPathInfo() + "<br>");
            out.println("</BODY> </HTML> ");

        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Administered-Object-Definition-Test servlet test failed");
            throw new ServletException(ex);
        }
    }


    public void destroy() {
        System.out.println("in Administered-Object-Definition-Test client::servlet destroy");
    }

    class MyThread extends Thread {
        HelloStateful ref;

        MyThread(HelloStateful ref) {
            this.ref = ref;
        }

        public void run() {
            try {
                ref.sleepFor(2);
            } catch (Throwable th) {
                throw new RuntimeException("Could not invoke waitfor() method");
            }
        }
    }


    private void sleepFor(int sec) {
        try {
            for (int i = 0; i < sec; i++) {
                Thread.currentThread().sleep(1000);
                System.out.println("[" + i + "/" + sec + "]: Sleeping....");
            }
        } catch (Exception ex) {
        }
    }

    private void lookupAdminObject(String jndiName, boolean expectSuccess) throws Exception{
        try {
            InitialContext ic = new InitialContext();
            Object ao = ic.lookup(jndiName);
            System.out.println("Servlet can access administered object : " + jndiName);
        } catch (Exception e) {
            if(expectSuccess){
                throw new RuntimeException("Fail to access administered object: "+jndiName, e);
            }else{
                System.out.println("Servlet cannot access administered object : " + jndiName);
            }
        }
    }


}
