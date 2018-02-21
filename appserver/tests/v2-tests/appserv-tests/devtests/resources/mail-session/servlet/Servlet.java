/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2002-2018 Oracle and/or its affiliates. All rights reserved.
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

import javax.annotation.Resource;
import javax.mail.MailSessionDefinition;
import javax.mail.MailSessionDefinitions;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.EJBs;
import javax.naming.InitialContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.UserTransaction;
import java.io.IOException;
import java.io.PrintWriter;

@EJB(name = "helloStateless3", beanInterface = Hello.class)
@EJBs({@EJB(name = "helloStateless4", beanName = "HelloEJB",
        beanInterface = Hello.class),
        @EJB(name = "helloStateful3", beanInterface = HelloStateful.class)})


@MailSessionDefinitions(
        value = {

                @MailSessionDefinition(description = "Mail Session Description 1",
                        name = "java:global/mail/MySession",
                        storeProtocol = "IMAP",
                        transportProtocol = "SMTP",
                        host = "localhost",
                        user = "naman",
                        password = "naman",
                        from = "naman.mehta@oracle.com",
                        properties = {"property1=10;property2=20"}
                ),

                @MailSessionDefinition(description = "Mail Session Description 3",
                        name = "java:app/mail/MySession",
                        storeProtocol = "IMAP",
                        transportProtocol = "SMTP",
                        host = "localhost",
                        user = "naman",
                        password = "naman",
                        from = "naman.mehta@oracle.com",
                        properties = {"property1=10;property2=20"}
                ),
                @MailSessionDefinition(description = "Mail Session Description 4",
                        name = "java:module/mail/MySession",
                        storeProtocol = "IMAP",
                        transportProtocol = "SMTP",
                        host = "localhost",
                        user = "naman",
                        password = "naman",
                        from = "naman.mehta@oracle.com",
                        properties = {"property1=10;property2=20"}
                ),
                @MailSessionDefinition(description = "Mail Session Description 5",
                        name = "java:global/env/Servlet_MailSession",
                        storeProtocol = "IMAP",
                        transportProtocol = "SMTP",
                        host = "localhost",
                        user = "naman",
                        password = "naman",
                        from = "naman.mehta@oracle.com",
                        properties = {"property1=10;property2=20"}
                ),
                @MailSessionDefinition(description = "Mail Session Description 5",
                        name = "java:app/env/Servlet_MailSession",
                        storeProtocol = "IMAP",
                        transportProtocol = "SMTP",
                        host = "localhost",
                        user = "naman",
                        password = "naman",
                        from = "naman.mehta@oracle.com",
                        properties = {"property1=10;property2=20"}
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

    public void init(ServletConfig config) throws ServletException {

        super.init(config);
        System.out.println("In Mail-Session-Test::servlet... init()");
    }

    public void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();

        try {

            InitialContext ic = new InitialContext();

            boolean global = lookupMailSession("java:global/mail/MySession", true);

            boolean app = lookupMailSession("java:app/mail/MySession", true);

            boolean module = lookupMailSession("java:module/mail/MySession", true);

            boolean globalHelloSfulEJB = lookupMailSession("java:global/mail/HelloStatefulEJB_MailSession", true);

            boolean appHelloStatefulEjb = lookupMailSession("java:app/mail/HelloStatefulEJB_MailSession", true);

            boolean globalHelloEJB = lookupMailSession("java:global/mail/HelloEJB_MailSession", true);

            boolean moduleHelloEjb = lookupMailSession("java:module/mail/HelloEJB_MailSession", false);

            boolean globalServlet_MS_MailSession = lookupMailSession("java:global/mail/Servlet_MS_MailSession", true);

            boolean globalHelloStateful_MS_MailSession = lookupMailSession("java:global/mail/HelloStatefulEJB_MS_MailSession", true);

            boolean globalHello_MS_MailSession = lookupMailSession("java:global/mail/HelloEJB_MS_MailSession", true);

            if (global && app && globalHelloSfulEJB && globalServlet_MS_MailSession && globalHelloEJB && globalHelloStateful_MS_MailSession
                    && globalHello_MS_MailSession && appHelloStatefulEjb && !moduleHelloEjb && module) {
                System.out.println("Servlet successful lookup of mail-sessions !");
            } else {
                throw new RuntimeException("Servlet failure ");
            }


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

            System.out.println("successfully accessed mail-session definitions");

            out.println("<HTML> <HEAD> <TITLE> JMS Servlet Output </TITLE> </HEAD> <BODY BGCOLOR=white>");
            out.println("<CENTER> <FONT size=+1 COLOR=blue>DatabaseServlet :: All information I can give </FONT> </CENTER> <p> ");
            out.println("<FONT size=+1 color=red> Context Path :  </FONT> " + req.getContextPath() + "<br>");
            out.println("<FONT size=+1 color=red> Servlet Path :  </FONT> " + req.getServletPath() + "<br>");
            out.println("<FONT size=+1 color=red> Path Info :  </FONT> " + req.getPathInfo() + "<br>");
            out.println("</BODY> </HTML> ");

        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Mail-Session-Test servlet test failed");
            throw new ServletException(ex);
        }
    }


    public void destroy() {
        System.out.println("in Mail-Session-Test client::servlet destroy");
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

    private boolean lookupMailSession(String mailSessionName, boolean expectSuccess) {
        try {
            InitialContext ic = new InitialContext();
            Object ds = ic.lookup(mailSessionName);
            return true;
        } catch (Exception e) {
            if (expectSuccess) {
                e.printStackTrace();
            }
            return false;
        }
    }


}
