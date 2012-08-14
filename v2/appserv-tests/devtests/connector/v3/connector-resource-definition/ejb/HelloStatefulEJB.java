package com.sun.s1asdev.ejb.ejb30.hello.session3;

import javax.ejb.Stateful;
import javax.annotation.PostConstruct;
import javax.resource.ConnectorResourceDefinitions;
import javax.resource.ConnectorResourceDefinition;
import javax.resource.cci.Connection;
import javax.resource.cci.ConnectionFactory;

import javax.naming.*;

@ConnectorResourceDefinitions(
        value = {
                @ConnectorResourceDefinition(
                        description="global-scope resource defined by @ConnectorResourceDefinition",
                        name = "java:global/env/HelloStatefulEJB_ModByDD_ConnectorResource",
                        className = "javax.resource.cci.ConnectionFactory",
                        properties = {"org.glassfish.connector-connection-pool.transaction-support=LocalTransaction",
                                      "org.glassfish.connector-connection-pool.resource-adapter-name=crd-ra"}
                ),

                @ConnectorResourceDefinition(
                        description="global-scope resource defined by @ConnectorResourceDefinition",
                        name = "java:global/env/HelloStatefulEJB_Annotation_ConnectorResource",
                        className = "javax.resource.cci.ConnectionFactory",
                        properties = {"org.glassfish.connector-connection-pool.transaction-support=LocalTransaction",
                                      "org.glassfish.connector-connection-pool.resource-adapter-name=crd-ra"}
                ),

                @ConnectorResourceDefinition(
                        description="application-scope resource defined by @ConnectorResourceDefinition",
                        name = "java:app/env/HelloStatefulEJB_Annotation_ConnectorResource",
                        className = "javax.resource.cci.ConnectionFactory",
                        properties = {"org.glassfish.connector-connection-pool.transaction-support=LocalTransaction",
                                      "org.glassfish.connector-connection-pool.resource-adapter-name=crd-ra"}
                ),

                @ConnectorResourceDefinition(
                        description="module-scope resource defined by @ConnectorResourceDefinition",
                        name = "java:module/env/HelloStatefulEJB_Annotation_ConnectorResource",
                        className = "javax.resource.cci.ConnectionFactory",
                        properties = {"org.glassfish.connector-connection-pool.transaction-support=LocalTransaction",
                                      "org.glassfish.connector-connection-pool.resource-adapter-name=crd-ra"}
                ),

                @ConnectorResourceDefinition(
                        description="component-scope resource defined by @ConnectorResourceDefinition",
                        name = "java:comp/env/HelloStatefulEJB_Annotation_ConnectorResource",
                        className = "javax.resource.cci.ConnectionFactory",
                        properties = {"org.glassfish.connector-connection-pool.transaction-support=LocalTransaction",
                                      "org.glassfish.connector-connection-pool.resource-adapter-name=crd-ra"}
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

        // Connector-Resource-Definition through Annotation
        lookupConnectorResource("java:global/env/Servlet_ConnectorResource", true);
        lookupConnectorResource("java:app/env/Servlet_ConnectorResource", true);
        lookupConnectorResource("java:module/env/Servlet_ConnectorResource", false);
        lookupConnectorResource("java:comp/env/Servlet_ConnectorResource", false);

        lookupConnectorResource("java:global/env/HelloStatefulEJB_Annotation_ConnectorResource", true);
        lookupConnectorResource("java:app/env/HelloStatefulEJB_Annotation_ConnectorResource", true);
        lookupConnectorResource("java:module/env/HelloStatefulEJB_Annotation_ConnectorResource", true);
        lookupConnectorResource("java:comp/env/HelloStatefulEJB_Annotation_ConnectorResource", true);

        lookupConnectorResource("java:global/env/HelloEJB_Annotation_ConnectorResource", true);
        lookupConnectorResource("java:app/env/HelloEJB_Annotation_ConnectorResource", true);
        lookupConnectorResource("java:module/env/HelloEJB_Annotation_ConnectorResource", true);
        lookupConnectorResource("java:comp/env/HelloEJB_Annotation_ConnectorResource", false);

        // Connector-Resource-Definition through DD
        lookupConnectorResource("java:global/env/EAR_ConnectorResource", true);
        lookupConnectorResource("java:app/env/EAR_ConnectorResource", true);

        lookupConnectorResource("java:global/env/Web_DD_ConnectorResource", true);
        lookupConnectorResource("java:app/env/Web_DD_ConnectorResource", true);
        lookupConnectorResource("java:module/env/Web_DD_ConnectorResource", false);
        lookupConnectorResource("java:comp/env/Web_DD_ConnectorResource", false);

        lookupConnectorResource("java:global/env/HelloStatefulEJB_DD_ConnectorResource", true);
        lookupConnectorResource("java:app/env/HelloStatefulEJB_DD_ConnectorResource", true);
        lookupConnectorResource("java:module/env/HelloStatefulEJB_DD_ConnectorResource", true);
        lookupConnectorResource("java:comp/env/HelloStatefulEJB_DD_ConnectorResource", true);

        lookupConnectorResource("java:global/env/HelloEJB_DD_ConnectorResource", true);
        lookupConnectorResource("java:app/env/HelloEJB_DD_ConnectorResource", true);
        lookupConnectorResource("java:module/env/HelloEJB_DD_ConnectorResource", true);
        lookupConnectorResource("java:comp/env/HelloEJB_DD_ConnectorResource", false);
        
        System.out.println("StatefulEJB datasource-definitions Success");

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

    private void lookupConnectorResource(String jndiName, boolean expectSuccess) throws RuntimeException{
        Connection c = null;
        try {
            InitialContext ic = new InitialContext();
            ConnectionFactory ds = (ConnectionFactory) ic.lookup(jndiName);
            c = ds.getConnection();
            System.out.println("Stateful EJB: can access connector resource : " + jndiName);
        } catch (Exception e) {
            if(expectSuccess){
                e.printStackTrace();
                throw new RuntimeException("Fail to access connector resource: "+jndiName, e);
            }else{
                System.out.println("Stateful EJB: can not access connector resource : " + jndiName);
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
