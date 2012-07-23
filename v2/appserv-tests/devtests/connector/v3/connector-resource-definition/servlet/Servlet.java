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
@WebServlet(name = "Servlet", urlPatterns = {"/servlet"})
public class Servlet extends HttpServlet {

    private
    @EJB
    Hello helloStateless;
    private
    @EJB(beanName = "HelloStatefulEJB")
    HelloStateful helloStateful;

    private
    @Resource
    UserTransaction ut;

    public void init(ServletConfig config) throws ServletException {

        super.init(config);
        System.out.println("In DataSource-Definition-Test::servlet... init()");
    }

    public void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

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


}
