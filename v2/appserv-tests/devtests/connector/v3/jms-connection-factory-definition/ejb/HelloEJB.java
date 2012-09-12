package com.sun.s1asdev.ejb.ejb30.hello.session3;

import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.jms.JMSConnectionFactoryDefinition;
import javax.annotation.jms.JMSConnectionFactoryDefinitions;
import javax.ejb.Stateless;
import javax.jms.ConnectionFactory;
import javax.jms.Connection;
import javax.naming.InitialContext;

@JMSConnectionFactoryDefinitions(
        value = {
                @JMSConnectionFactoryDefinition(
                        description = "global-scope resource defined by @JMSConnectionFactoryDefinition",
                        name = "java:global/env/HelloEJB_ModByDD_JMSConnectionFactory",
                        className = "javax.jms.ConnectionFactory",
                        resourceAdapterName = "jmsra",
                        user = "admin",
                        password = "admin",
                        properties = {"org.glassfish.connector-connection-pool.transaction-support=LocalTransaction"},
                        initialPoolSize = 0,
                        minPoolSize = 0
                ),

                @JMSConnectionFactoryDefinition(
                        description = "global-scope resource defined by @JMSConnectionFactoryDefinition",
                        name = "java:global/env/HelloEJB_Annotation_JMSConnectionFactory",
                        className = "javax.jms.ConnectionFactory",
                        resourceAdapterName = "jmsra",
                        user = "admin",
                        password = "admin",
                        properties = {"org.glassfish.connector-connection-pool.transaction-support=XATransaction" },
                        initialPoolSize = 0,
                        minPoolSize = 0
                ),

                @JMSConnectionFactoryDefinition(
                        description = "application-scope resource defined by @JMSConnectionFactoryDefinition",
                        name = "java:app/env/HelloEJB_Annotation_JMSConnectionFactory",
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
                        name = "java:module/env/HelloEJB_Annotation_JMSConnectionFactory",
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
                        name = "java:comp/env/HelloEJB_Annotation_JMSConnectionFactory",
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

@Stateless
public class HelloEJB implements Hello {

    private Collection<Connection> connections = null;

    @PostConstruct
    public void postConstruction() {
    	  connections = new ArrayList<Connection>();
        System.out.println("In HelloEJB::postConstruction()");
    }

    @PreDestroy
    public void closeConnections() {
        for (Connection c : connections) {
            try {
                c.close();
            } catch (Exception e) {
            }
        }
        connections.clear();
        connections = null;
    }

    public void hello() {
        // JMSConnectionFactory-Definition through Annotation
        lookupJMSConnectionFactory("java:global/env/Appclient_ModByDD_JMSConnectionFactory", true);
        lookupJMSConnectionFactory("java:global/env/Appclient_Annotation_JMSConnectionFactory", true);
        lookupJMSConnectionFactory("java:app/env/Appclient_Annotation_JMSConnectionFactory", true);
        lookupJMSConnectionFactory("java:module/env/Appclient_Annotation_JMSConnectionFactory", false);
        lookupJMSConnectionFactory("java:comp/env/Appclient_Annotation_JMSConnectionFactory", false);

        lookupJMSConnectionFactory("java:global/env/Servlet_ModByDD_JMSConnectionFactory", true);
        lookupJMSConnectionFactory("java:global/env/Servlet_JMSConnectionFactory", true);
        lookupJMSConnectionFactory("java:app/env/Servlet_JMSConnectionFactory", true);
        lookupJMSConnectionFactory("java:module/env/Servlet_JMSConnectionFactory", false);
        lookupJMSConnectionFactory("java:comp/env/Servlet_JMSConnectionFactory", false);

        lookupJMSConnectionFactory("java:global/env/HelloStatefulEJB_ModByDD_JMSConnectionFactory", true);
        lookupJMSConnectionFactory("java:global/env/HelloStatefulEJB_Annotation_JMSConnectionFactory", true);
        lookupJMSConnectionFactory("java:app/env/HelloStatefulEJB_Annotation_JMSConnectionFactory", true);
        lookupJMSConnectionFactory("java:module/env/HelloStatefulEJB_Annotation_JMSConnectionFactory", true);
        lookupJMSConnectionFactory("java:comp/env/HelloStatefulEJB_Annotation_JMSConnectionFactory", false);

        lookupJMSConnectionFactory("java:global/env/HelloEJB_ModByDD_JMSConnectionFactory", true);
        lookupJMSConnectionFactory("java:global/env/HelloEJB_Annotation_JMSConnectionFactory", true);
        lookupJMSConnectionFactory("java:app/env/HelloEJB_Annotation_JMSConnectionFactory", true);
        lookupJMSConnectionFactory("java:module/env/HelloEJB_Annotation_JMSConnectionFactory", true);
        lookupJMSConnectionFactory("java:comp/env/HelloEJB_Annotation_JMSConnectionFactory", true);

        // JMSConnectionFactory-Definition through DD
        lookupJMSConnectionFactory("java:global/env/Application_DD_JMSConnectionFactory", true);
        lookupJMSConnectionFactory("java:app/env/Application_DD_JMSConnectionFactory", true);

        lookupJMSConnectionFactory("java:global/env/Appclient_DD_JMSConnectionFactory", true);
        lookupJMSConnectionFactory("java:app/env/Appclient_DD_JMSConnectionFactory", true);
        lookupJMSConnectionFactory("java:module/env/Appclient_DD_JMSConnectionFactory", false);
        lookupJMSConnectionFactory("java:comp/env/Appclient_DD_JMSConnectionFactory", false);

        lookupJMSConnectionFactory("java:global/env/Web_DD_JMSConnectionFactory", true);
        lookupJMSConnectionFactory("java:app/env/Web_DD_JMSConnectionFactory", true);
        lookupJMSConnectionFactory("java:module/env/Web_DD_JMSConnectionFactory", false);
        lookupJMSConnectionFactory("java:comp/env/Web_DD_JMSConnectionFactory", false);

        lookupJMSConnectionFactory("java:global/env/HelloStatefulEJB_DD_JMSConnectionFactory", true);
        lookupJMSConnectionFactory("java:app/env/HelloStatefulEJB_DD_JMSConnectionFactory", true);
        lookupJMSConnectionFactory("java:module/env/HelloStatefulEJB_DD_JMSConnectionFactory", true);
        lookupJMSConnectionFactory("java:comp/env/HelloStatefulEJB_DD_JMSConnectionFactory", false);

        lookupJMSConnectionFactory("java:global/env/HelloEJB_DD_JMSConnectionFactory", true);
        lookupJMSConnectionFactory("java:app/env/HelloEJB_DD_JMSConnectionFactory", true);
        lookupJMSConnectionFactory("java:module/env/HelloEJB_DD_JMSConnectionFactory", true);
        lookupJMSConnectionFactory("java:comp/env/HelloEJB_DD_JMSConnectionFactory", true);

        System.out.println("Stateless EJB lookup jms-connection-factory-definitions successfully!");
    }

    private void lookupJMSConnectionFactory(String jndiName, boolean expectSuccess) {
        Connection c = null;
        try {
            System.out.println("Stateless EJB lookup jms connection factory: " + jndiName);
            InitialContext ic = new InitialContext();
            ConnectionFactory cf = (ConnectionFactory) ic.lookup(jndiName);
            c = cf.createConnection();
            connections.add(c);
            System.out.println("Stateless EJB can access jms connection factory: " + jndiName);
        } catch (Exception e) {
            if (expectSuccess) {
                throw new RuntimeException("Stateless EJB failed to access jms connection factory: " + jndiName, e);
            }
            System.out.println("Stateless EJB cannot access jms connection factory: " + jndiName);
            return;
        }
        if (!expectSuccess) {
            throw new RuntimeException("Stateless EJB should not run into here.");
        }
    }
}
