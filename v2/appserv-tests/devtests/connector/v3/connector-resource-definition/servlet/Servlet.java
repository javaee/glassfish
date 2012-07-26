/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.ejb30.hello.session3;

import java.io.*;

import javax.resource.ConnectorResourceDefinitions;
import javax.resource.ConnectorResourceDefinition;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import javax.naming.*;
import javax.ejb.EJB;
import javax.ejb.EJBs;
import javax.ejb.EJBException;
import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import javax.transaction.UserTransaction;



@EJB(name = "helloStateless3", beanInterface = Hello.class)
@EJBs({@EJB(name = "helloStateless4", beanName = "HelloEJB",
        beanInterface = Hello.class),
        @EJB(name = "helloStateful3", beanInterface = HelloStateful.class)})

@ConnectorResourceDefinitions(
        value = {

                @ConnectorResourceDefinition(
                        description="global-scope resource defined by @ConnectorResourceDefinition",
                        name = "java:global/env/Servlet_ModByDD_ConnectorResource",
                        className = "javax.resource.cci.ConnectionFactory",
                        properties = {"transactionSupport=LocalTransaction","resource-adapter-name=RaApplicationName"}
                ),

                @ConnectorResourceDefinition(
                        description="global-scope resource defined by @ConnectorResourceDefinition",
                        name = "java:global/env/Servlet_ConnectorResource",
                        className = "javax.resource.cci.ConnectionFactory",
                        properties = {"transactionSupport=LocalTransaction","resource-adapter-name=RaApplicationName"}
                ),

                @ConnectorResourceDefinition(
                        description="application-scope resource defined by @ConnectorResourceDefinition",
                        name = "java:app/env/Servlet_ConnectorResource",
                        className = "javax.resource.cci.ConnectionFactory",
                        properties = {"transactionSupport=LocalTransaction","resource-adapter-name=RaApplicationName"}
                ),
                
                @ConnectorResourceDefinition(
                        description="module-scope resource defined by @ConnectorResourceDefinition",
                        name = "java:module/env/Servlet_ConnectorResource",
                        className = "javax.resource.cci.ConnectionFactory",
                        properties = {"transactionSupport=LocalTransaction","resource-adapter-name=RaApplicationName"}
                ),
                
                @ConnectorResourceDefinition(
                        description="component-scope resource defined by @ConnectorResourceDefinition",
                        name = "java:comp/env/Servlet_ConnectorResource",
                        className = "javax.resource.cci.ConnectionFactory",
                        properties = {"transactionSupport=LocalTransaction","resource-adapter-name=RaApplicationName"}
                )
        }
)
@WebServlet(name = "Servlet",
        urlPatterns = {"/servlet"}
)
public class Servlet extends HttpServlet {

    private
    @EJB
    Hello helloStateless;
    private
    @EJB(beanName = "HelloStatefulEJB")
    HelloStateful helloStateful;

/*
    private Hello helloStateless2;
    private HelloStateful helloStateful2;

*/
    private
    @Resource
    UserTransaction ut;

/*
    @EJB(beanName = "HelloEJB")
    private void setHelloStateless2(Hello h) {
        helloStateless2 = h;
    }

    @EJB
    private void setHelloStateful2(HelloStateful hf) {
        helloStateful2 = hf;
    }
*/

    public void init(ServletConfig config) throws ServletException {

        super.init(config);
        System.out.println("In DataSource-Definition-Test::servlet... init()");
    }

    public void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();

        try {

            InitialContext ic = new InitialContext();

//            boolean global = lookupConnectorResource("java:global/env/Servlet_ConnectorResource", true);
//            boolean comp = lookupConnectorResource("java:comp/env/Servlet_ConnectorResource", true);
//            boolean appServletDataSource = lookupConnectorResource("java:app/env/Servlet_ConnectorResource", true);
//            boolean moduleServletDataSource = lookupConnectorResource("java:module/env/Servlet_ConnectorResource", true);
//
//            boolean globalHelloSfulEJB = lookupConnectorResource("java:global/env/HelloStatefulEJB_DataSource", true);
//            boolean compHelloSfulEJB = lookupConnectorResource("java:comp/env/HelloStatefulEJB_DataSource", false);
//            boolean appHelloStatefulEjb = lookupConnectorResource("java:app/env/HelloStatefulEJB_DataSource", true);
//
//            boolean globalHelloEJB = lookupConnectorResource("java:global/env/HelloEJB_DataSource", true);
//            boolean compHelloEJB = lookupConnectorResource("java:comp/env/HelloEJB_DataSource", false);
//            boolean moduleHelloEjb = lookupConnectorResource("java:module/env/HelloEJB_DataSource", false);
//
//            boolean globalServlet_DD_DataSource = lookupConnectorResource("java:global/env/Servlet_DD_DataSource", true);
//            boolean compServlet_DD_DataSource = lookupConnectorResource("java:comp/env/Servlet_DD_DataSource", false);
//
//            boolean globalHelloStateful_DD_DataSource = lookupConnectorResource("java:global/env/HelloStatefulEJB_DD_DataSource", true);
//            boolean compHelloStateful_DD_DataSource = lookupConnectorResource("java:comp/env/HelloStatefulEJB_DD_DataSource", false);
//
//            boolean globalHello_DD_DataSource = lookupConnectorResource("java:global/env/HelloEJB_DD_DataSource", true);
//            boolean compHello_DD_DataSource = lookupConnectorResource("java:comp/env/HelloEJB_DD_DataSource", false);


//            if (global && comp && appServletDataSource && globalHelloSfulEJB && globalServlet_DD_DataSource && compServlet_DD_DataSource
//                    && !compHelloSfulEJB && globalHelloEJB && !compHelloEJB && globalHelloStateful_DD_DataSource
//                    && !compHelloStateful_DD_DataSource && globalHello_DD_DataSource && !compHello_DD_DataSource
//                    && appHelloStatefulEjb && !moduleHelloEjb && moduleServletDataSource) {
//                System.out.println("Servlet successful lookup of datasource-definitions !");
//            } else {
//                throw new RuntimeException("Servlet failure");
//            }


            System.out.println("beginning tx");
            ut.begin();

            // invoke method on the EJB
            System.out.println("invoking stateless ejb");
            helloStateless.hello();
//            helloStateless2.hello();

            System.out.println("committing tx");
            ut.commit();
            System.out.println("committed tx");


            System.out.println("invoking stateless ejb");
            helloStateful.hello();
  //          helloStateful2.hello();

    /*        Hello helloStateless3 = (Hello)
                    ic.lookup("java:comp/env/helloStateless3");

            helloStateless3.hello();

            Hello helloStateless4 = (Hello)
                    ic.lookup("java:comp/env/helloStateless4");

            helloStateless4.hello();

            HelloStateful helloStateful3 = (HelloStateful)
                    ic.lookup("java:comp/env/helloStateful3");

            helloStateful3.hello();
*/
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

            System.out.println("successfully accessed datasource definitions");

            out.println("<HTML> <HEAD> <TITLE> JMS Servlet Output </TITLE> </HEAD> <BODY BGCOLOR=white>");
            out.println("<CENTER> <FONT size=+1 COLOR=blue>DatabaseServlet :: All information I can give </FONT> </CENTER> <p> ");
            out.println("<FONT size=+1 color=red> Context Path :  </FONT> " + req.getContextPath() + "<br>");
            out.println("<FONT size=+1 color=red> Servlet Path :  </FONT> " + req.getServletPath() + "<br>");
            out.println("<FONT size=+1 color=red> Path Info :  </FONT> " + req.getPathInfo() + "<br>");
            out.println("</BODY> </HTML> ");

        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("DataSource-Definition-Test servlet test failed");
            throw new ServletException(ex);
        }
    }


    public void destroy() {
        System.out.println("in DataSource-Definition-Test client::servlet destroy");
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

    private boolean lookupConnectorResource(String dataSourceName, boolean expectSuccess) {
        Connection c = null;
        try {
            System.out.println("lookup dataSource : " + dataSourceName);
            InitialContext ic = new InitialContext();
            DataSource ds = (DataSource) ic.lookup(dataSourceName);
            c = ds.getConnection();
            System.out.println("got connection : " + c);
            return true;
        } catch (Exception e) {
            if(expectSuccess){
                e.printStackTrace();
            }
            return false;
        } finally {
            try {
                if (c != null) {
                    c.close();
                }
            } catch (Exception e) {
            }
        }
    }


}
