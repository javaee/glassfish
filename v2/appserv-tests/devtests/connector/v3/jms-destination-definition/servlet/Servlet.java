package com.sun.s1asdev.ejb.ejb30.hello.session3;

import java.io.IOException;
import java.io.PrintWriter;

import javax.annotation.Resource;
import javax.annotation.jms.JMSDestinationDefinition;
import javax.annotation.jms.JMSDestinationDefinitions;
import javax.ejb.EJB;
import javax.ejb.EJBs;
import javax.jms.Destination;
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
                        className = "javax.jms.Queue",
                        resourceAdapterName = "jmsra",
                        destinationName = "myPhysicalQueue"
                ),

                @JMSDestinationDefinition(
                        description = "global-scope resource defined by @JMSDestinationDefinition",
                        name = "java:global/env/Servlet_JMSDestination",
                        className = "javax.jms.Queue",
                        resourceAdapterName = "jmsra",
                        destinationName = "myPhysicalQueue"
                ),

                @JMSDestinationDefinition(
                        description = "application-scope resource defined by @JMSDestinationDefinition",
                        name = "java:app/env/Servlet_JMSDestination",
                        className = "javax.jms.Topic",
                        resourceAdapterName = "jmsra",
                        destinationName = "myPhysicalTopic"
                ),

                @JMSDestinationDefinition(
                        description = "module-scope resource defined by @JMSDestinationDefinition",
                        name = "java:module/env/Servlet_JMSDestination",
                        className = "javax.jms.Topic",
                        resourceAdapterName = "jmsra",
                        destinationName = "myPhysicalTopic"
                ),

                @JMSDestinationDefinition(
                        description = "component-scope resource defined by @JMSDestinationDefinition",
                        name = "java:comp/env/Servlet_JMSDestination",
                        className = "javax.jms.Queue",
                        resourceAdapterName = "jmsra",
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
