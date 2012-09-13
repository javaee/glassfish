package com.sun.s1asdev.ejb.ejb30.hello.session3;

import javax.annotation.PostConstruct;
import javax.ejb.Stateful;
import javax.jms.Destination;
import javax.annotation.jms.JMSDestinationDefinition;
import javax.annotation.jms.JMSDestinationDefinitions;
import javax.naming.InitialContext;

@JMSDestinationDefinitions(
        value = {
                @JMSDestinationDefinition(
                        description = "global-scope resource defined by @JMSDestinationDefinition",
                        name = "java:global/env/HelloStatefulEJB_ModByDD_JMSDestination",
                        className = "javax.jms.Queue",
                        resourceAdapterName = "jmsra",
                        destinationName = "myPhysicalQueue"
                ),

                @JMSDestinationDefinition(
                        description = "global-scope resource defined by @JMSDestinationDefinition",
                        name = "java:global/env/HelloStatefulEJB_Annotation_JMSDestination",
                        className = "javax.jms.Queue",
                        resourceAdapterName = "jmsra",
                        destinationName = "myPhysicalQueue"
                ),

                @JMSDestinationDefinition(
                        description = "application-scope resource defined by @JMSDestinationDefinition",
                        name = "java:app/env/HelloStatefulEJB_Annotation_JMSDestination",
                        className = "javax.jms.Topic",
                        resourceAdapterName = "jmsra",
                        destinationName = "myPhysicalTopic"
                ),

                @JMSDestinationDefinition(
                        description = "module-scope resource defined by @JMSDestinationDefinition",
                        name = "java:module/env/HelloStatefulEJB_Annotation_JMSDestination",
                        className = "javax.jms.Topic",
                        resourceAdapterName = "jmsra",
                        destinationName = "myPhysicalTopic"
                ),

                @JMSDestinationDefinition(
                        description = "component-scope resource defined by @JMSDestinationDefinition",
                        name = "java:comp/env/HelloStatefulEJB_Annotation_JMSDestination",
                        className = "javax.jms.Queue",
                        resourceAdapterName = "jmsra",
                        destinationName = "myPhysicalQueue"
                )
        }
)

@Stateful
public class HelloStatefulEJB implements HelloStateful {

    @PostConstruct
    public void postConstruction() {
        System.out.println("In HelloStatefulEJB::postConstruction()");
    }

    public void hello() {
        // JMSDestination-Definition through Annotation
        lookupJMSDestination("java:global/env/Appclient_ModByDD_JMSDestination", true);
        lookupJMSDestination("java:global/env/Appclient_Annotation_JMSDestination", true);
        lookupJMSDestination("java:app/env/Appclient_Annotation_JMSDestination", true);
        lookupJMSDestination("java:module/env/Appclient_Annotation_JMSDestination", false);
        lookupJMSDestination("java:comp/env/Appclient_Annotation_JMSDestination", false);

        lookupJMSDestination("java:global/env/Servlet_ModByDD_JMSDestination", true);
        lookupJMSDestination("java:global/env/Servlet_JMSDestination", true);
        lookupJMSDestination("java:app/env/Servlet_JMSDestination", true);
        lookupJMSDestination("java:module/env/Servlet_JMSDestination", false);
        lookupJMSDestination("java:comp/env/Servlet_JMSDestination", false);

        lookupJMSDestination("java:global/env/HelloStatefulEJB_ModByDD_JMSDestination", true);
        lookupJMSDestination("java:global/env/HelloStatefulEJB_Annotation_JMSDestination", true);
        lookupJMSDestination("java:app/env/HelloStatefulEJB_Annotation_JMSDestination", true);
        lookupJMSDestination("java:module/env/HelloStatefulEJB_Annotation_JMSDestination", true);
        lookupJMSDestination("java:comp/env/HelloStatefulEJB_Annotation_JMSDestination", true);

        lookupJMSDestination("java:global/env/HelloEJB_ModByDD_JMSDestination", true);
        lookupJMSDestination("java:global/env/HelloEJB_Annotation_JMSDestination", true);
        lookupJMSDestination("java:app/env/HelloEJB_Annotation_JMSDestination", true);
        lookupJMSDestination("java:module/env/HelloEJB_Annotation_JMSDestination", true);
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
        lookupJMSDestination("java:module/env/Web_DD_JMSDestination", false);
        lookupJMSDestination("java:comp/env/Web_DD_JMSDestination", false);

        lookupJMSDestination("java:global/env/HelloStatefulEJB_DD_JMSDestination", true);
        lookupJMSDestination("java:app/env/HelloStatefulEJB_DD_JMSDestination", true);
        lookupJMSDestination("java:module/env/HelloStatefulEJB_DD_JMSDestination", true);
        lookupJMSDestination("java:comp/env/HelloStatefulEJB_DD_JMSDestination", true);

        lookupJMSDestination("java:global/env/HelloEJB_DD_JMSDestination", true);
        lookupJMSDestination("java:app/env/HelloEJB_DD_JMSDestination", true);
        lookupJMSDestination("java:module/env/HelloEJB_DD_JMSDestination", true);
        lookupJMSDestination("java:comp/env/HelloEJB_DD_JMSDestination", false);

        System.out.println("Stateful EJB lookup jms-destination-definitions successfully!");
    }

    public void sleepFor(int sec) {
        try {
            for (int i = 0; i < sec; i++) {
                Thread.currentThread().sleep(1000);
            }
        } catch (Exception ex) {
        }
    }

    public void ping() {
    }

    private void lookupJMSDestination(String jndiName, boolean expectSuccess) {
        try {
            System.out.println("Stateful EJB lookup jms destination: " + jndiName);
            InitialContext ic = new InitialContext();
            Destination dest = (Destination) ic.lookup(jndiName);
            System.out.println("Stateful EJB can access jms destination: " + jndiName);
        } catch (Exception e) {
            if (expectSuccess) {
                throw new RuntimeException("Stateful EJB failed to access jms destination: " + jndiName, e);
            }
            System.out.println("Stateful EJB cannot access jms destination: " + jndiName);
            return;
        }
        if (!expectSuccess) {
            throw new RuntimeException("Stateful EJB should not run into here.");
        }
    }

}
