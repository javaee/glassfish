package com.sun.s1asdev.ejb.ejb30.hello.session3;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.annotation.jms.JMSConnectionFactoryDefinition;
import javax.annotation.jms.JMSConnectionFactoryDefinitions;
import javax.naming.InitialContext;
import javax.jms.ConnectionFactory;
import javax.jms.Connection;

@JMSConnectionFactoryDefinitions(
        value = {

               @JMSConnectionFactoryDefinition(
                        description = "global-scope resource defined by @JMSConnectionFactoryDefinition",
                        name = "java:global/env/Appclient_ModByDD_JMSConnectionFactory",
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
                        name = "java:global/env/Appclient_Annotation_JMSConnectionFactory",
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
                        name = "java:app/env/Appclient_Annotation_JMSConnectionFactory",
                        className = "javax.jms.ConnectionFactory",
                        resourceAdapterName = "jmsra",
                        user = "admin",
                        password = "admin",
                        properties = {"org.glassfish.connector-connection-pool.transaction-support=NoTransaction"},
                        initialPoolSize = 0,
                        minPoolSize = 0
                ),

                @JMSConnectionFactoryDefinition(
                        description = "module-scope resource defined by @JMSConnectionFactoryDefinition",
                        name = "java:module/env/Appclient_Annotation_JMSConnectionFactory",
                        className = "javax.jms.ConnectionFactory",
                        resourceAdapterName = "jmsra",
                        user = "admin",
                        password = "admin",
                        properties = {"org.glassfish.connector-connection-pool.transaction-support=LocalTransaction"},
                        initialPoolSize = 0,
                        minPoolSize = 0
                ),

                @JMSConnectionFactoryDefinition(
                        description = "component-scope resource defined by @JMSConnectionFactoryDefinition",
                        name = "java:comp/env/Appclient_Annotation_JMSConnectionFactory",
                        className = "javax.jms.ConnectionFactory",
                        resourceAdapterName = "jmsra",
                        user = "admin",
                        password = "admin",
                        properties = {"org.glassfish.connector-connection-pool.transaction-support=LocalTransaction"},
                        initialPoolSize = 0,
                        minPoolSize = 0
                )
        }
)

public class Client {

    private String host;
    private String port;

    private static SimpleReporterAdapter stat =
            new SimpleReporterAdapter("appserv-tests");


    public Client(String[] args) {
        host = (args.length > 0) ? args[0] : "localhost";
        port = (args.length > 1) ? args[1] : "4848";
    }

    public static void main(String[] args) {
        stat.addDescription("jms-connection-factory-definitionclient");
        Client client = new Client(args);
        client.doTest();
        stat.printSummary("jms-connection-factory-definitionclient");
    }

    public void doTest() {
        try {
            // JMSConnectionFactory-Definition through Annotation
            lookupJMSConnectionFactory("java:global/env/Appclient_ModByDD_JMSConnectionFactory", true);
            lookupJMSConnectionFactory("java:global/env/Appclient_Annotation_JMSConnectionFactory", true);
            lookupJMSConnectionFactory("java:app/env/Appclient_Annotation_JMSConnectionFactory", true);
            lookupJMSConnectionFactory("java:module/env/Appclient_Annotation_JMSConnectionFactory", true);
            lookupJMSConnectionFactory("java:comp/env/Appclient_Annotation_JMSConnectionFactory", true);

            lookupJMSConnectionFactory("java:global/env/Servlet_ModByDD_JMSConnectionFactory", true);
            lookupJMSConnectionFactory("java:global/env/Servlet_JMSConnectionFactory", true);
            lookupJMSConnectionFactory("java:app/env/Servlet_JMSConnectionFactory", true);
            lookupJMSConnectionFactory("java:module/env/Servlet_JMSConnectionFactory", false);
            lookupJMSConnectionFactory("java:comp/env/Servlet_JMSConnectionFactory", false);


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
            lookupJMSConnectionFactory("java:module/env/Appclient_DD_JMSConnectionFactory", true);
            lookupJMSConnectionFactory("java:comp/env/Appclient_DD_JMSConnectionFactory", true);

            lookupJMSConnectionFactory("java:global/env/Web_DD_JMSConnectionFactory", true);
            lookupJMSConnectionFactory("java:app/env/Web_DD_JMSConnectionFactory", true);
            lookupJMSConnectionFactory("java:module/env/Web_DD_JMSConnectionFactory", false);
            lookupJMSConnectionFactory("java:comp/env/Web_DD_JMSConnectionFactory", false);

            lookupJMSConnectionFactory("java:global/env/HelloStatefulEJB_DD_JMSConnectionFactory", true);
            lookupJMSConnectionFactory("java:app/env/HelloStatefulEJB_DD_JMSConnectionFactory", true);
            lookupJMSConnectionFactory("java:module/env/HelloStatefulEJB_DD_JMSConnectionFactory", false);
            lookupJMSConnectionFactory("java:comp/env/HelloStatefulEJB_DD_JMSConnectionFactory", false);

            lookupJMSConnectionFactory("java:global/env/HelloEJB_DD_JMSConnectionFactory", true);
            lookupJMSConnectionFactory("java:app/env/HelloEJB_DD_JMSConnectionFactory", true);
            lookupJMSConnectionFactory("java:module/env/HelloEJB_DD_JMSConnectionFactory", false);
            lookupJMSConnectionFactory("java:comp/env/HelloEJB_DD_JMSConnectionFactory", false);

            System.out.println("Application client lookup jms-connection-factory-definitions successfully!");
            stat.addStatus("JMSConnectionFactory-Definition-appclient-test", stat.PASS);

            String url = "http://" + host + ":" + port +
                    "/jms-connection-factory-definition/servlet";
            System.out.println("invoking JMSConnectionFactory-Definition test servlet at " + url);
            int code = invokeServlet(url);

            if (code != 200) {
                System.out.println("Incorrect return code: " + code);
                stat.addStatus("JMSConnectionFactory-Definition-web-ejb-test", stat.FAIL);
            } else {
                stat.addStatus("JMSConnectionFactory-Definition-web-ejb-test", stat.PASS);
            }

        } catch (Exception ex) {
            System.out.println("JMSConnectionFactory-Definition appclient & web & ejb test failed.");
            stat.addStatus("JMSConnectionFactory-Definition-web-ejb-test", stat.FAIL);
            ex.printStackTrace();
        }
    }

    private void lookupJMSConnectionFactory(String jndiName, boolean expectSuccess) {
        Connection c = null;
        try {
            System.out.println("Application client lookup jms connection factory: " + jndiName);
            InitialContext ic = new InitialContext();
            ConnectionFactory cf = (ConnectionFactory) ic.lookup(jndiName);
            c = cf.createConnection();
            System.out.println("Application client can access jms connection factory: " + jndiName);
        } catch (Exception e) {
            if (expectSuccess) {
                throw new RuntimeException("Application client failed to access jms connection factory: " + jndiName, e);
            }
            System.out.println("Application client cannot access jms connection factory: " + jndiName);
            return;
        } finally {
            try {
                if (c != null) {
                    c.close();
                }
            } catch (Exception e) {
            }
        }
        if (!expectSuccess) {
            System.out.println("Application client failed ...");
            throw new RuntimeException("Application client should not run into here.");
        }
    }

    private int invokeServlet(String url) throws Exception {

        URL u = new URL(url);

        HttpURLConnection c1 = (HttpURLConnection) u.openConnection();
        int code = c1.getResponseCode();
        InputStream is = c1.getInputStream();
        BufferedReader input = new BufferedReader(new InputStreamReader(is));
        String line = null;
        while ((line = input.readLine()) != null)
            System.out.println(line);
        if (code != 200) {
            System.out.println("Incorrect return code: " + code);
        }
        return code;
    }
}
