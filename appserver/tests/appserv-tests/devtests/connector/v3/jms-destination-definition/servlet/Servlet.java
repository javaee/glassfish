/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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

import java.io.IOException;
import java.io.PrintWriter;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.EJBs;
import javax.jms.Destination;
import javax.jms.JMSDestinationDefinition;
import javax.jms.JMSDestinationDefinitions;
import javax.naming.InitialContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.UserTransaction;

@EJB(name = "helloStateless3", beanInterface = Hello.class)
@EJBs({@EJB(name = "helloStateless4", beanName = "HelloEJB",
        beanInterface = Hello.class),
        @EJB(name = "helloStateful3", beanInterface = HelloStateful.class)})

@JMSDestinationDefinitions(
        value = {

                @JMSDestinationDefinition(
                        description = "global-scope resource defined by @JMSDestinationDefinition",
                        name = "java:global/env/Servlet_ModByDD_JMSDestination",
                        interfaceName = "javax.jms.Queue",
                        resourceAdapter = "jmsra",
                        destinationName = "myPhysicalQueue"
                ),

                @JMSDestinationDefinition(
                        description = "global-scope resource defined by @JMSDestinationDefinition",
                        name = "java:global/env/Servlet_JMSDestination",
                        interfaceName = "javax.jms.Queue",
                        resourceAdapter = "jmsra",
                        destinationName = "myPhysicalQueue"
                ),

                @JMSDestinationDefinition(
                        description = "application-scope resource defined by @JMSDestinationDefinition",
                        name = "java:app/env/Servlet_JMSDestination",
                        interfaceName = "javax.jms.Topic",
                        resourceAdapter = "jmsra",
                        destinationName = "myPhysicalTopic"
                ),

                @JMSDestinationDefinition(
                        description = "module-scope resource defined by @JMSDestinationDefinition",
                        name = "java:module/env/Servlet_JMSDestination",
                        interfaceName = "javax.jms.Topic",
//                        resourceAdapter = "jmsra",
                        destinationName = "myPhysicalTopic"
                ),

                @JMSDestinationDefinition(
                        description = "component-scope resource defined by @JMSDestinationDefinition",
                        name = "java:comp/env/Servlet_JMSDestination",
                        interfaceName = "javax.jms.Queue",
//                        resourceAdapter = "jmsra",
                        destinationName = "myPhysicalQueue"
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
        System.out.println("In JMSDestination-Definition-Test::servlet... init()");
    }

    public void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();

        try {
            // JMSDestination-Definition through Annotation
            lookupJMSDestination("java:global/env/Appclient_ModByDD_JMSDestination", true);
            lookupJMSDestination("java:global/env/Appclient_Annotation_JMSDestination", true);
            lookupJMSDestination("java:app/env/Appclient_Annotation_JMSDestination", true);
            lookupJMSDestination("java:module/env/Appclient_Annotation_JMSDestination", false);
            lookupJMSDestination("java:comp/env/Appclient_Annotation_JMSDestination", false);

            lookupJMSDestination("java:global/env/Servlet_ModByDD_JMSDestination", true);
            lookupJMSDestination("java:global/env/Servlet_JMSDestination", true);
            lookupJMSDestination("java:app/env/Servlet_JMSDestination", true);
            lookupJMSDestination("java:module/env/Servlet_JMSDestination", true);
            lookupJMSDestination("java:comp/env/Servlet_JMSDestination", true);

            lookupJMSDestination("java:global/env/HelloStatefulEJB_ModByDD_JMSDestination", true);
            lookupJMSDestination("java:global/env/HelloStatefulEJB_Annotation_JMSDestination", true);
            lookupJMSDestination("java:app/env/HelloStatefulEJB_Annotation_JMSDestination", true);
            lookupJMSDestination("java:module/env/HelloStatefulEJB_Annotation_JMSDestination", false);
            lookupJMSDestination("java:comp/env/HelloStatefulEJB_Annotation_JMSDestination", false);

            lookupJMSDestination("java:global/env/HelloEJB_ModByDD_JMSDestination", true);
            lookupJMSDestination("java:global/env/HelloEJB_Annotation_JMSDestination", true);
            lookupJMSDestination("java:app/env/HelloEJB_Annotation_JMSDestination", true);
            lookupJMSDestination("java:module/env/HelloEJB_Annotation_JMSDestination", false);
            lookupJMSDestination("java:comp/env/HelloEJB_Annotation_JMSDestination", false);

            // JMSDestination-Definition through DD
            lookupJMSDestination("java:global/env/Application_DD_JMSDestination", true);
            lookupJMSDestination("java:app/env/Application_DD_JMSDestination", true);

            lookupJMSDestination("java:global/env/Appclient_DD_JMSDestination", true);
            lookupJMSDestination("java:app/env/Appclient_DD_JMSDestination", true);
            lookupJMSDestination("java:module/env/Appclient_DD_JMSDestination", false);
            lookupJMSDestination("java:comp/env/Appclient_DD_JMSDestination", false);

            lookupJMSDestination("java:global/env/Web_DD_JMSDestination", true);
            lookupJMSDestination("java:app/env/Web_DD_JMSDestination", true);
            lookupJMSDestination("java:module/env/Web_DD_JMSDestination", true);
            lookupJMSDestination("java:comp/env/Web_DD_JMSDestination", true);

            lookupJMSDestination("java:global/env/HelloStatefulEJB_DD_JMSDestination", true);
            lookupJMSDestination("java:app/env/HelloStatefulEJB_DD_JMSDestination", true);
            lookupJMSDestination("java:module/env/HelloStatefulEJB_DD_JMSDestination", false);
            lookupJMSDestination("java:comp/env/HelloStatefulEJB_DD_JMSDestination", false);

            lookupJMSDestination("java:global/env/HelloEJB_DD_JMSDestination", true);
            lookupJMSDestination("java:app/env/HelloEJB_DD_JMSDestination", true);
            lookupJMSDestination("java:module/env/HelloEJB_DD_JMSDestination", false);
            lookupJMSDestination("java:comp/env/HelloEJB_DD_JMSDestination", false);

            System.out.println("Servlet lookup jms-destination-definitions successfully!");

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

            try {
                MyThread thread = new MyThread(helloStateful);
                thread.start();

                sleepFor(2);
                helloStateful.ping();
                //throw new EJBException("Did not get ConcurrentAccessException");
            } catch (javax.ejb.ConcurrentAccessException conEx) {
                ;   //Everything is fine
            } catch (Throwable th) {
                throw new Exception("Got some wierd exception: " + th);
            }

            System.out.println("Application successfully accessed jms destination definitions");

            out.println("<HTML> <HEAD> <TITLE> JMS Servlet Output </TITLE> </HEAD> <BODY BGCOLOR=white>");
            out.println("<CENTER> <FONT size=+1 COLOR=blue>DatabaseServlet :: All information I can give </FONT> </CENTER> <p> ");
            out.println("<FONT size=+1 color=red> Context Path :  </FONT> " + req.getContextPath() + "<br>");
            out.println("<FONT size=+1 color=red> Servlet Path :  </FONT> " + req.getServletPath() + "<br>");
            out.println("<FONT size=+1 color=red> Path Info :  </FONT> " + req.getPathInfo() + "<br>");
            out.println("</BODY> </HTML> ");

        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("JMSDestination-Definition-Test servlet test failed");
            throw new ServletException(ex);
        }
    }

    public void destroy() {
        System.out.println("in JMSDestination-Definition-Test client::servlet destroy");
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

    private void lookupJMSDestination(String jndiName, boolean expectSuccess) {
        try {
            System.out.println("Servlet lookup jms destination: " + jndiName);
            InitialContext ic = new InitialContext();
            Destination dest = (Destination) ic.lookup(jndiName);
            System.out.println("Servlet can access jms destination: " + jndiName);
        } catch (Exception e) {
            if (expectSuccess) {
                throw new RuntimeException("Servlet failed to access jms destination: " + jndiName, e);
            }
            System.out.println("Servlet cannot access jms destination: " + jndiName);
            return;

        }
        if (!expectSuccess) {
            throw new RuntimeException("Servlet should not run into here.");
        }
    }
}
