package com.sun.s1asdev.ejb.ejb30.hello.session3;

import javax.ejb.Stateless;

import javax.naming.InitialContext;
import javax.resource.ConnectorResourceDefinitions;
import javax.resource.ConnectorResourceDefinition;
import javax.resource.cci.Connection;
import javax.resource.cci.ConnectionFactory;

@ConnectorResourceDefinitions(
     value = {
          @ConnectorResourceDefinition(
                description="global-scope resource defined by @ConnectorResourceDefinition",
                name = "java:global/env/HelloEJB_ModByDD_ConnectorResource",
                className = "javax.resource.cci.ConnectionFactory",
                properties = {"org.glassfish.connector-connection-pool.transaction-support=LocalTransaction",
                              "org.glassfish.connector-connection-pool.resource-adapter-name=crd-ra"}
          ),
          @ConnectorResourceDefinition(
               description = "global-scope resource defined by @ConnectorResourceDefinition", 
               name = "java:global/env/HelloEJB_Annotation_ConnectorResource", 
               className = "javax.resource.cci.ConnectionFactory", 
               properties = {"org.glassfish.connector-connection-pool.transaction-support=LocalTransaction",
                             "org.glassfish.connector-connection-pool.resource-adapter-name=crd-ra"}
          ),
          
          @ConnectorResourceDefinition(
               description = "application-scope resource defined by @ConnectorResourceDefinition", 
               name = "java:app/env/HelloEJB_Annotation_ConnectorResource", 
               className = "javax.resource.cci.ConnectionFactory", 
               properties = {"org.glassfish.connector-connection-pool.transaction-support=LocalTransaction",
                             "org.glassfish.connector-connection-pool.resource-adapter-name=crd-ra"}
          ),
          
          @ConnectorResourceDefinition(
               description = "module-scope resource defined by @ConnectorResourceDefinition", 
               name = "java:module/env/HelloEJB_Annotation_ConnectorResource", 
               className = "javax.resource.cci.ConnectionFactory", 
               properties = {"org.glassfish.connector-connection-pool.transaction-support=LocalTransaction",
                             "org.glassfish.connector-connection-pool.resource-adapter-name=crd-ra"}
          ),
          
          @ConnectorResourceDefinition(
               description = "component-scope resource defined by @ConnectorResourceDefinition", 
               name = "java:comp/env/HelloEJB_Annotation_ConnectorResource", 
               className = "javax.resource.cci.ConnectionFactory", 
               properties = {"org.glassfish.connector-connection-pool.transaction-support=LocalTransaction",
                             "org.glassfish.connector-connection-pool.resource-adapter-name=crd-ra"}
          )

     }
)
@Stateless
public class HelloEJB implements Hello {

    public void hello() {

        // Connector-Resource-Definition through Annotation
        lookupConnectorResource("java:global/env/Servlet_ConnectorResource", true);
        lookupConnectorResource("java:app/env/Servlet_ConnectorResource", true);
        lookupConnectorResource("java:module/env/Servlet_ConnectorResource", false);
        lookupConnectorResource("java:comp/env/Servlet_ConnectorResource", false);

        lookupConnectorResource("java:global/env/HelloStatefulEJB_Annotation_ConnectorResource", true);
        lookupConnectorResource("java:app/env/HelloStatefulEJB_Annotation_ConnectorResource", true);
        lookupConnectorResource("java:module/env/HelloStatefulEJB_Annotation_ConnectorResource", true);
        lookupConnectorResource("java:comp/env/HelloStatefulEJB_Annotation_ConnectorResource", false);

        lookupConnectorResource("java:global/env/HelloEJB_Annotation_ConnectorResource", true);
        lookupConnectorResource("java:app/env/HelloEJB_Annotation_ConnectorResource", true);
        lookupConnectorResource("java:module/env/HelloEJB_Annotation_ConnectorResource", true);
        lookupConnectorResource("java:comp/env/HelloEJB_Annotation_ConnectorResource", true);

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
        lookupConnectorResource("java:comp/env/HelloStatefulEJB_DD_ConnectorResource", false);

        lookupConnectorResource("java:global/env/HelloEJB_DD_ConnectorResource", true);
        lookupConnectorResource("java:app/env/HelloEJB_DD_ConnectorResource", true);
        lookupConnectorResource("java:module/env/HelloEJB_DD_ConnectorResource", true);
        lookupConnectorResource("java:comp/env/HelloEJB_DD_ConnectorResource", true);
        
        System.out.println("In HelloEJB::hello()");
    }

    private void lookupConnectorResource(String jndiName, boolean expectSuccess) throws RuntimeException{
        Connection c = null;
        try {
            InitialContext ic = new InitialContext();
            ConnectionFactory ds = (ConnectionFactory) ic.lookup(jndiName);
            c = ds.getConnection();
            System.out.println("Stateless EJB: can access connector resource : " + jndiName);
        } catch (Exception e) {
            if(expectSuccess){
                e.printStackTrace();
                throw new RuntimeException("Fail to access connector resource: "+jndiName, e);
            }else{
                System.out.println("Stateless EJB cannot access connector resource : " + jndiName);
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
