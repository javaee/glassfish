package com.sun.s1asdev.ejb.ejb30.hello.session3;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.jms.Destination;
import javax.annotation.jms.JMSDestinationDefinition;
import javax.annotation.jms.JMSDestinationDefinitions;
import javax.naming.InitialContext;

@JMSDestinationDefinitions(
        value = {
                @JMSDestinationDefinition(
                        description = "global-scope resource defined by @JMSDestinationDefinition",
                        name = "java:global/env/Appclient_ModByDD_JMSDestination",
                        className = "javax.jms.Queue",
                        resourceAdapterName = "jmsra",
                        destinationName = "myPhysicalQueue"
                ),

                @JMSDestinationDefinition(
                        description = "global-scope resource defined by @JMSDestinationDefinition",
                        name = "java:global/env/Appclient_Annotation_JMSDestination",
                        className = "javax.jms.Queue",
                        resourceAdapterName = "jmsra",
                        destinationName = "myPhysicalQueue"
                ),

                @JMSDestinationDefinition(
                        description = "application-scope resource defined by @JMSDestinationDefinition",
                        name = "java:app/env/Appclient_Annotation_JMSDestination",
                        className = "javax.jms.Topic",
                        resourceAdapterName = "jmsra",
                        destinationName = "myPhysicalTopic"
                ),

                @JMSDestinationDefinition(
                        description = "module-scope resource defined by @JMSDestinationDefinition",
                        name = "java:module/env/Appclient_Annotation_JMSDestination",
                        className = "javax.jms.Topic",
                        resourceAdapterName = "jmsra",
                        destinationName = "myPhysicalTopic"
                ),

                @JMSDestinationDefinition(
                        description = "component-scope resource defined by @JMSDestinationDefinition",
                        name = "java:comp/env/Appclient_Annotation_JMSDestination",
                        className = "javax.jms.Queue",
                        resourceAdapterName = "jmsra",
                        destinationName = "myPhysicalQueue"
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
        stat.addDescription("jms-destination-definitionclient");
        Client client = new Client(args);
        client.doTest();
        stat.printSummary("jms-destination-definitionclient");
    }

    public void doTest() {
        try {
            // JMSDestination-Definition through Annotation
            lookupJMSDestination("java:global/env/Appclient_ModByDD_JMSDestination", true);
            lookupJMSDestination("java:global/env/Appclient_Annotation_JMSDestination", true);
            lookupJMSDestination("java:app/env/Appclient_Annotation_JMSDestination", true);
//            lookupJMSDestination("java:module/env/Appclient_Annotation_JMSDestination", true);
//            lookupJMSDestination("java:comp/env/Appclient_Annotation_JMSDestination", true);

            lookupJMSDestination("java:global/env/Servlet_ModByDD_JMSDestination", true);
            lookupJMSDestination("java:global/env/Servlet_JMSDestination", true);
            lookupJMSDestination("java:app/env/Servlet_JMSDestination", true);
            lookupJMSDestination("java:module/env/Servlet_JMSDestination", false);
            lookupJMSDestination("java:comp/env/Servlet_JMSDestination", false);


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
//            lookupJMSDestination("java:module/env/Appclient_DD_JMSDestination", true);
//            lookupJMSDestination("java:comp/env/Appclient_DD_JMSDestination", true);

            lookupJMSDestination("java:global/env/Web_DD_JMSDestination", true);
            lookupJMSDestination("java:app/env/Web_DD_JMSDestination", true);
            lookupJMSDestination("java:module/env/Web_DD_JMSDestination", false);
            lookupJMSDestination("java:comp/env/Web_DD_JMSDestination", false);

            lookupJMSDestination("java:global/env/HelloStatefulEJB_DD_JMSDestination", true);
            lookupJMSDestination("java:app/env/HelloStatefulEJB_DD_JMSDestination", true);
            lookupJMSDestination("java:module/env/HelloStatefulEJB_DD_JMSDestination", false);
            lookupJMSDestination("java:comp/env/HelloStatefulEJB_DD_JMSDestination", false);

            lookupJMSDestination("java:global/env/HelloEJB_DD_JMSDestination", true);
            lookupJMSDestination("java:app/env/HelloEJB_DD_JMSDestination", true);
            lookupJMSDestination("java:module/env/HelloEJB_DD_JMSDestination", false);
            lookupJMSDestination("java:comp/env/HelloEJB_DD_JMSDestination", false);

            System.out.println("Application client lookup jms-destination-definitions successfully!");
            stat.addStatus("JMSDestination-Definition-appclient-test", stat.PASS);

            String url = "http://" + host + ":" + port +
                    "/jms-destination-definition/servlet";
            System.out.println("invoking JMSDestination-Definition test servlet at " + url);
            int code = invokeServlet(url);

            if (code != 200) {
                System.out.println("Incorrect return code: " + code);
                stat.addStatus("JMSDestination-Definition-web-ejb-test", stat.FAIL);
            } else {
                stat.addStatus("JMSDestination-Definition-web-ejb-test", stat.PASS);
            }

        } catch (Exception ex) {
            System.out.println("JMSDestination-Definition appclient & web & ejb test failed.");
            stat.addStatus("JMSDestination-Definition-web-ejb-test", stat.FAIL);
            ex.printStackTrace();
        }
    }

    private void lookupJMSDestination(String jndiName, boolean expectSuccess) {
        try {
            System.out.println("Application client lookup jms destination: " + jndiName);
            InitialContext ic = new InitialContext();
            Destination dest = (Destination) ic.lookup(jndiName);
            System.out.println("Application client can access jms destination: " + jndiName);
        } catch (Exception e) {
            if (expectSuccess) {
                throw new RuntimeException("Application client failed to access jms destination: " + jndiName, e);
            }
            System.out.println("Application client cannot access jms destination: " + jndiName);
            return;
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
