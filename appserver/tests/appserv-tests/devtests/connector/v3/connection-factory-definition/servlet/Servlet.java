/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2002-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.ejb.ejb30.hello.session3;

import java.io.*;

import javax.resource.ConnectionFactoryDefinitions;
import javax.resource.ConnectionFactoryDefinition;
import javax.resource.cci.Connection;
import javax.resource.cci.ConnectionFactory;
import javax.resource.spi.TransactionSupport.TransactionSupportLevel;
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

@ConnectionFactoryDefinitions(
        value = {

                @ConnectionFactoryDefinition(
                        description="global-scope resource defined by @ConnectionFactoryDefinition",
                        name = "java:global/env/Servlet_ModByDD_ConnectionFactory",
                        interfaceName = "javax.resource.cci.ConnectionFactory",
                        resourceAdapter = "cfd-ra",
                        properties = {"testName=foo"}
                ),

                @ConnectionFactoryDefinition(
                        description="global-scope resource defined by @ConnectionFactoryDefinition",
                        name = "java:global/env/Servlet_ConnectionFactory",
                        interfaceName = "javax.resource.cci.ConnectionFactory",
                        resourceAdapter = "cfd-ra",
                        transactionSupport = TransactionSupportLevel.LocalTransaction,
                        maxPoolSize = 16,
                        minPoolSize = 4,
                        properties = {"testName=foo"}
                ),

                @ConnectionFactoryDefinition(
                        description="application-scope resource defined by @ConnectionFactoryDefinition",
                        name = "java:app/env/Servlet_ConnectionFactory",
                        interfaceName = "javax.resource.cci.ConnectionFactory",
                        resourceAdapter = "cfd-ra",
                        transactionSupport = TransactionSupportLevel.XATransaction,
                        maxPoolSize = 16,
                        minPoolSize = 4,
                        properties = {"testName=foo"}
                ),
                
                @ConnectionFactoryDefinition(
                        description="module-scope resource defined by @ConnectionFactoryDefinition",
                        name = "java:module/env/Servlet_ConnectionFactory",
                        interfaceName = "javax.resource.cci.ConnectionFactory",
                        resourceAdapter = "cfd-ra",
                        properties = {"testName=foo"}
                ),
                
                @ConnectionFactoryDefinition(
                        description="component-scope resource defined by @ConnectionFactoryDefinition",
                        name = "java:comp/env/Servlet_ConnectionFactory",
                        interfaceName = "javax.resource.cci.ConnectionFactory",
                        resourceAdapter = "cfd-ra",
                        properties = {"testName=foo"}
                )
        }
)
@WebServlet(name = "Servlet", urlPatterns = {"/servlet"})
public class Servlet extends HttpServlet {

    @EJB
    private  Hello helloStateless;
    
    @EJB(beanName = "HelloStatefulEJB")
    private  HelloStateful helloStateful;

    @Resource
    private UserTransaction ut;

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        System.out.println("In Connection-Factory-Definition-Test::servlet... init()");
    }

    public void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();
        
        try {

            // Connection-Factory-Definition through Annotation
            lookupConnectionFactory("java:global/env/Servlet_ConnectionFactory", true);
            lookupConnectionFactory("java:app/env/Servlet_ConnectionFactory", true);
            lookupConnectionFactory("java:module/env/Servlet_ConnectionFactory", true);
            lookupConnectionFactory("java:comp/env/Servlet_ConnectionFactory", true);

            lookupConnectionFactory("java:global/env/HelloStatefulEJB_Annotation_ConnectionFactory", true);
            lookupConnectionFactory("java:app/env/HelloStatefulEJB_Annotation_ConnectionFactory", true);
            lookupConnectionFactory("java:module/env/HelloStatefulEJB_Annotation_ConnectionFactory", false);
            lookupConnectionFactory("java:comp/env/HelloStatefulEJB_Annotation_ConnectionFactory", false);

            lookupConnectionFactory("java:global/env/HelloEJB_Annotation_ConnectionFactory", true);
            lookupConnectionFactory("java:app/env/HelloEJB_Annotation_ConnectionFactory", true);
            lookupConnectionFactory("java:module/env/HelloEJB_Annotation_ConnectionFactory", false);
            lookupConnectionFactory("java:comp/env/HelloEJB_Annotation_ConnectionFactory", false);

            // Connection-Factory-Definition through DD
            lookupConnectionFactory("java:global/env/EAR_ConnectionFactory", true);
            lookupConnectionFactory("java:app/env/EAR_ConnectionFactory", true);

            lookupConnectionFactory("java:global/env/Web_DD_ConnectionFactory", true);
            lookupConnectionFactory("java:app/env/Web_DD_ConnectionFactory", true);
            lookupConnectionFactory("java:module/env/Web_DD_ConnectionFactory", true);
            lookupConnectionFactory("java:comp/env/Web_DD_ConnectionFactory", true);

            lookupConnectionFactory("java:global/env/HelloStatefulEJB_DD_ConnectionFactory", true);
            lookupConnectionFactory("java:app/env/HelloStatefulEJB_DD_ConnectionFactory", true);
            lookupConnectionFactory("java:module/env/HelloStatefulEJB_DD_ConnectionFactory", false);
            lookupConnectionFactory("java:comp/env/HelloStatefulEJB_DD_ConnectionFactory", false);

            lookupConnectionFactory("java:global/env/HelloEJB_DD_ConnectionFactory", true);
            lookupConnectionFactory("java:app/env/HelloEJB_DD_ConnectionFactory", true);
            lookupConnectionFactory("java:module/env/HelloEJB_DD_ConnectionFactory", false);
            lookupConnectionFactory("java:comp/env/HelloEJB_DD_ConnectionFactory", false);

            System.out.println("Servlet successful lookup of connection-factory-definitions !");

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

            System.out.println("successfully accessed connector factory definitions");

            out.println("<HTML> <HEAD> <TITLE> Servlet Output </TITLE> </HEAD> <BODY BGCOLOR=white>");
            out.println("<CENTER> <FONT size=+1 COLOR=blue>Servlet :: All information I can give </FONT> </CENTER> <p> ");
            out.println("<FONT size=+1 color=red> Context Path :  </FONT> " + req.getContextPath() + "<br>");
            out.println("<FONT size=+1 color=red> Servlet Path :  </FONT> " + req.getServletPath() + "<br>");
            out.println("<FONT size=+1 color=red> Path Info :  </FONT> " + req.getPathInfo() + "<br>");
            out.println("</BODY> </HTML> ");

        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Connection-Factory-Definition-Test servlet test failed");
            throw new ServletException(ex);
        }
    }


    public void destroy() {
        System.out.println("in Connector-Factory-Definition-Test client::servlet destroy");
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

    private void lookupConnectionFactory(String jndiName, boolean expectSuccess) throws Exception{
        Connection c = null;
        try {
            InitialContext ic = new InitialContext();
            ConnectionFactory ds = (ConnectionFactory) ic.lookup(jndiName);
            c = ds.getConnection();
            System.out.println("Servlet can access connector factory : " + jndiName);
        } catch (Exception e) {
            if(expectSuccess){
                throw new RuntimeException("Fail to access connector factory: "+jndiName, e);
            }else{
                System.out.println("Servlet cannot access connector factory : " + jndiName);
            }
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
