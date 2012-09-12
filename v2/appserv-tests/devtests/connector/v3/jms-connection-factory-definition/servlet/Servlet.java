package com.sun.s1asdev.ejb.ejb30.hello.session3;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.Resource;
import javax.annotation.jms.JMSConnectionFactoryDefinition;
import javax.annotation.jms.JMSConnectionFactoryDefinitions;
import javax.ejb.EJB;
import javax.ejb.EJBs;
import javax.jms.ConnectionFactory;
import javax.jms.Connection;
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

@JMSConnectionFactoryDefinitions(
        value = {

                @JMSConnectionFactoryDefinition(
                        description = "global-scope resource defined by @JMSConnectionFactoryDefinition",
                        name = "java:global/env/Servlet_ModByDD_JMSConnectionFactory",
                        className = "javax.jms.ConnectionFactory",
                        resourceAdapterName = "jmsra",
                        user = "admin",
                        password = "admin",
                        properties = {"org.glassfish.connector-connection-pool.transaction-support=NoTransaction"},
                        initialPoolSize = 0,
                        minPoolSize = 0
                ),

                @JMSConnectionFactoryDefinition(
                        description = "global-scope resource defined by @JMSConnectionFactoryDefinition",
                        name = "java:global/env/Servlet_JMSConnectionFactory",
                        className = "javax.jms.ConnectionFactory",
                        resourceAdapterName = "jmsra",
                        user = "admin",
                        password = "admin",
                        properties = {"org.glassfish.connector-connection-pool.transaction-support=XATransaction"},
                        initialPoolSize = 0,
                        minPoolSize = 0
                ),

                @JMSConnectionFactoryDefinition(
                        description = "application-scope resource defined by @JMSConnectionFactoryDefinition",
                        name = "java:app/env/Servlet_JMSConnectionFactory",
                        className = "javax.jms.ConnectionFactory",
                        resourceAdapterName = "jmsra",
                        user = "admin",
                        password = "admin",
                        properties = {"org.glassfish.connector-connection-pool.transaction-support=XATransaction"},
                        initialPoolSize = 0,
                        minPoolSize = 0
                ),

                @JMSConnectionFactoryDefinition(
                        description = "module-scope resource defined by @JMSConnectionFactoryDefinition",
                        name = "java:module/env/Servlet_JMSConnectionFactory",
                        className = "javax.jms.ConnectionFactory",
                        resourceAdapterName = "jmsra",
                        user = "admin",
                        password = "admin",
                        properties = {"org.glassfish.connector-connection-pool.transaction-support=XATransaction"},
                        initialPoolSize = 0,
                        minPoolSize = 0
                ),

                @JMSConnectionFactoryDefinition(
                        description = "component-scope resource defined by @JMSConnectionFactoryDefinition",
                        name = "java:comp/env/Servlet_JMSConnectionFactory",
                        className = "javax.jms.ConnectionFactory",
                        resourceAdapterName = "jmsra",
                        user = "admin",
                        password = "admin",
                        properties = {"org.glassfish.connector-connection-pool.transaction-support=XATransaction"},
                        initialPoolSize = 0,
                        minPoolSize = 0
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

    private Collection<Connection> connections = new ArrayList<Connection>();

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        System.out.println("In JMSConnectionFactory-Definition-Test::servlet... init()");
    }

    public void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();

        try {
            // JMSConnectionFactory-Definition through Annotation
            lookupJMSConnectionFactory("java:global/env/Appclient_ModByDD_JMSConnectionFactory", true);
            lookupJMSConnectionFactory("java:global/env/Appclient_Annotation_JMSConnectionFactory", true);
            lookupJMSConnectionFactory("java:app/env/Appclient_Annotation_JMSConnectionFactory", true);
            lookupJMSConnectionFactory("java:module/env/Appclient_Annotation_JMSConnectionFactory", false);
            lookupJMSConnectionFactory("java:comp/env/Appclient_Annotation_JMSConnectionFactory", false);

            lookupJMSConnectionFactory("java:global/env/Servlet_ModByDD_JMSConnectionFactory", true);
            lookupJMSConnectionFactory("java:global/env/Servlet_JMSConnectionFactory", true);
            lookupJMSConnectionFactory("java:app/env/Servlet_JMSConnectionFactory", true);
            lookupJMSConnectionFactory("java:module/env/Servlet_JMSConnectionFactory", true);
            lookupJMSConnectionFactory("java:comp/env/Servlet_JMSConnectionFactory", true);

            lookupJMSConnectionFactory("java:global/env/HelloStatefulEJB_ModByDD_JMSConnectionFactory", true);
            lookupJMSConnectionFactory("java:global/env/HelloStatefulEJB_Annotation_JMSConnectionFactory", true);
            lookupJMSConnectionFactory("java:app/env/HelloStatefulEJB_Annotation_JMSConnectionFactory", true);
            lookupJMSConnectionFactory("java:module/env/HelloStatefulEJB_Annotation_JMSConnectionFactory", false);
            lookupJMSConnectionFactory("java:comp/env/HelloStatefulEJB_Annotation_JMSConnectionFactory", false);

            lookupJMSConnectionFactory("java:global/env/HelloEJB_ModByDD_JMSConnectionFactory", true);
            lookupJMSConnectionFactory("java:global/env/HelloEJB_Annotation_JMSConnectionFactory", true);
            lookupJMSConnectionFactory("java:app/env/HelloEJB_Annotation_JMSConnectionFactory", true);
            lookupJMSConnectionFactory("java:module/env/HelloEJB_Annotation_JMSConnectionFactory", false);
            lookupJMSConnectionFactory("java:comp/env/HelloEJB_Annotation_JMSConnectionFactory", false);

            // JMSConnectionFactory-Definition through DD
            lookupJMSConnectionFactory("java:global/env/Application_DD_JMSConnectionFactory", true);
            lookupJMSConnectionFactory("java:app/env/Application_DD_JMSConnectionFactory", true);

            lookupJMSConnectionFactory("java:global/env/Appclient_DD_JMSConnectionFactory", true);
            lookupJMSConnectionFactory("java:app/env/Appclient_DD_JMSConnectionFactory", true);
            lookupJMSConnectionFactory("java:module/env/Appclient_DD_JMSConnectionFactory", false);
            lookupJMSConnectionFactory("java:comp/env/Appclient_DD_JMSConnectionFactory", false);

            lookupJMSConnectionFactory("java:global/env/Web_DD_JMSConnectionFactory", true);
            lookupJMSConnectionFactory("java:app/env/Web_DD_JMSConnectionFactory", true);
            lookupJMSConnectionFactory("java:module/env/Web_DD_JMSConnectionFactory", true);
            lookupJMSConnectionFactory("java:comp/env/Web_DD_JMSConnectionFactory", true);

            lookupJMSConnectionFactory("java:global/env/HelloStatefulEJB_DD_JMSConnectionFactory", true);
            lookupJMSConnectionFactory("java:app/env/HelloStatefulEJB_DD_JMSConnectionFactory", true);
            lookupJMSConnectionFactory("java:module/env/HelloStatefulEJB_DD_JMSConnectionFactory", false);
            lookupJMSConnectionFactory("java:comp/env/HelloStatefulEJB_DD_JMSConnectionFactory", false);

            lookupJMSConnectionFactory("java:global/env/HelloEJB_DD_JMSConnectionFactory", true);
            lookupJMSConnectionFactory("java:app/env/HelloEJB_DD_JMSConnectionFactory", true);
            lookupJMSConnectionFactory("java:module/env/HelloEJB_DD_JMSConnectionFactory", false);
            lookupJMSConnectionFactory("java:comp/env/HelloEJB_DD_JMSConnectionFactory", false);

            System.out.println("Servlet lookup jms-connection-factory-definitions successfully!");

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
                throw new Exception("Got some wierd exception: " + th);
            }

            System.out.println("Application successfully accessed jms connection factory definitions");

            out.println("<HTML> <HEAD> <TITLE> JMS Servlet Output </TITLE> </HEAD> <BODY BGCOLOR=white>");
            out.println("<CENTER> <FONT size=+1 COLOR=blue>DatabaseServlet :: All information I can give </FONT> </CENTER> <p> ");
            out.println("<FONT size=+1 color=red> Context Path :  </FONT> " + req.getContextPath() + "<br>");
            out.println("<FONT size=+1 color=red> Servlet Path :  </FONT> " + req.getServletPath() + "<br>");
            out.println("<FONT size=+1 color=red> Path Info :  </FONT> " + req.getPathInfo() + "<br>");
            out.println("</BODY> </HTML> ");

        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("JMSConnectionFactory-Definition-Test servlet test failed");
            throw new ServletException(ex);
        } finally {
            closeConnections();
        }
    }

    public void destroy() {
        System.out.println("in JMSConnectionFactory-Definition-Test client::servlet destroy");
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

    private void lookupJMSConnectionFactory(String jndiName, boolean expectSuccess) {
        Connection c = null;
        try {
            System.out.println("Servlet lookup jms connection factory: " + jndiName);
            InitialContext ic = new InitialContext();
            ConnectionFactory cf = (ConnectionFactory) ic.lookup(jndiName);
            c = cf.createConnection();
            connections.add(c);
            System.out.println("Servlet can access jms connection factory: " + jndiName);
        } catch (Exception e) {
            if (expectSuccess) {
                throw new RuntimeException("Servlet failed to access jms connection factory: " + jndiName, e);
            }
            System.out.println("Servlet cannot access jms connection factory: " + jndiName);
            return;

        }
        if (!expectSuccess) {
            throw new RuntimeException("Servlet should not run into here.");
        }
    }

    private void closeConnections() {
        for (Connection c : connections) {
            try {
                c.close();
            } catch (Exception e) {
            }
        }
        connections.clear();
    }
}
