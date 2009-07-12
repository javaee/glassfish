/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.ejb30.hello.session3;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.naming.*;
import javax.ejb.EJB;
import javax.ejb.EJBs;
import javax.ejb.EJBException;
import javax.annotation.Resource;
import javax.annotation.Resources;
import javax.sql.DataSource;
import java.sql.Connection;
import javax.servlet.*;
import javax.servlet.annotation.*;
import javax.servlet.http.*;

import javax.transaction.UserTransaction;


import javax.annotation.sql.*;

@EJB(name = "helloStateless3", beanInterface = Hello.class)
@EJBs({@EJB(name = "helloStateless4", beanName = "HelloEJB",
        beanInterface = Hello.class),
        @EJB(name = "helloStateful3", beanInterface = HelloStateful.class)})


@DataSourceDefinitions(
        value = {

                @DataSourceDefinition(name = "java:global/Servlet_DataSource",
                        className = "org.apache.derby.jdbc.EmbeddedXADataSource",
                        user = "APP",
                        password = "APP",
                        databaseName = "hello-servlet",
                        properties = {"connectionAttributes=;create=true"}
                ),

                @DataSourceDefinition(name = "java:comp/Servlet_DataSource",
                        className = "org.apache.derby.jdbc.EmbeddedXADataSource",
                        user = "APP",
                        password = "APP",
                        databaseName = "hello-servlet",
                        properties = {"connectionAttributes=;create=true"}
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

    private Hello helloStateless2;
    private HelloStateful helloStateful2;

    private
    @Resource
    UserTransaction ut;

    @EJB(beanName = "HelloEJB")
    private void setHelloStateless2(Hello h) {
        helloStateless2 = h;
    }

    @EJB
    private void setHelloStateful2(HelloStateful hf) {
        helloStateful2 = hf;
    }

    public void init(ServletConfig config) throws ServletException {

        super.init(config);
        System.out.println("In webclient::servlet... init()");
    }

    public void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();

        try {

            InitialContext ic = new InitialContext();

            boolean global = lookupDataSource("java:global/Servlet_DataSource");
            boolean comp = lookupDataSource("java:comp/Servlet_DataSource");

            boolean globalHelloSfulEJB = lookupDataSource("java:global/HelloStatefulEJB_DataSource");
            boolean compHelloSfulEJB = lookupDataSource("java:comp/HelloStatefulEJB_DataSource");

            boolean globalHelloEJB = lookupDataSource("java:global/HelloEJB_DataSource");
            boolean compHelloEJB = lookupDataSource("java:comp/HelloEJB_DataSource");

            boolean globalServlet_DD_DataSource = lookupDataSource("java:global/Servlet_DD_DataSource");
            boolean compServlet_DD_DataSource = lookupDataSource("java:comp/Servlet_DD_DataSource");

            boolean globalHelloStateful_DD_DataSource = lookupDataSource("java:global/HelloStatefulEJB_DD_DataSource");
            boolean compHelloStateful_DD_DataSource = lookupDataSource("java:comp/HelloStatefulEJB_DD_DataSource");

            boolean globalHello_DD_DataSource = lookupDataSource("java:global/HelloEJB_DD_DataSource");
            boolean compHello_DD_DataSource = lookupDataSource("java:comp/HelloEJB_DD_DataSource");


            if (global && comp && globalHelloSfulEJB && globalServlet_DD_DataSource && compServlet_DD_DataSource
                    && !compHelloSfulEJB && globalHelloEJB && !compHelloEJB && globalHelloStateful_DD_DataSource
                    && !compHelloStateful_DD_DataSource && globalHello_DD_DataSource && !compHello_DD_DataSource) {
                System.out.println("4444 Servlet Success");
                System.out.println("Servlet successful injection of EMF/EM references!");
            } else {
                System.out.println("4444 Servlet Failure");
                throw new RuntimeException("HelloEJB failure");
            }


            System.out.println("beginning tx");
            ut.begin();

            // invoke method on the EJB
            System.out.println("invoking stateless ejb");
            helloStateless.hello();
            helloStateless2.hello();

            System.out.println("committing tx");
            ut.commit();
            System.out.println("committed tx");


            System.out.println("invoking stateless ejb");
            helloStateful.hello();
            helloStateful2.hello();

            Hello helloStateless3 = (Hello)
                    ic.lookup("java:comp/env/helloStateless3");

            helloStateless3.hello();

            Hello helloStateless4 = (Hello)
                    ic.lookup("java:comp/env/helloStateless4");

            helloStateless4.hello();

            HelloStateful helloStateful3 = (HelloStateful)
                    ic.lookup("java:comp/env/helloStateful3");

            helloStateful3.hello();

            System.out.println("successfully invoked ejbs");

            System.out.println("accessing connections");
            try {
                MyThread thread = new MyThread(helloStateful2);
                thread.start();

                sleepFor(2);
                helloStateful2.ping();
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
            System.out.println("webclient servlet test failed");
            throw new ServletException(ex);
        }
    }


    public void destroy() {
        System.out.println("in webclient::servlet destroy");
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

    private boolean lookupDataSource(String dataSourceName) {
        Connection c = null;
        try {
            System.out.println("lookup dataSource : " + dataSourceName);
            InitialContext ic = new InitialContext();
            DataSource ds = (DataSource) ic.lookup(dataSourceName);
            c = ds.getConnection();
            System.out.println("got connection : " + c);
            return true;
        } catch (Exception e) {
            // e.printStackTrace();
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
