package com.sun.s1asdev.ejb.ejb30.hello.session3;

import javax.ejb.Stateless;

import javax.naming.InitialContext;
import javax.resource.ConnectorResourceDefinitions;
import javax.resource.ConnectorResourceDefinition;
import javax.sql.DataSource;
import java.sql.Connection;

@ConnectorResourceDefinitions(
     value = {
          @ConnectorResourceDefinition(
                description="global-scope resource defined by @ConnectorResourceDefinition",
                name = "java:global/env/HelloEJB_ModByDD_ConnectorResource",
                className = "javax.resource.cci.ConnectionFactory",
                properties = {"transactionSupport=LocalTransaction","resource-adapter-name=RaApplicationName"}
          ),
          @ConnectorResourceDefinition(
               description = "global-scope resource defined by @ConnectorResourceDefinition", 
               name = "java:global/env/HelloEJB_Annotation_ConnectorResource", 
               className = "javax.resource.cci.ConnectionFactory", 
               properties = {"transactionSupport=LocalTransaction","resource-adapter-name=RaApplicationName"}
          ),
          
          @ConnectorResourceDefinition(
               description = "application-scope resource defined by @ConnectorResourceDefinition", 
               name = "java:app/env/HelloEJB_Annotation_ConnectorResource", 
               className = "javax.resource.cci.ConnectionFactory", 
               properties = {"transactionSupport=LocalTransaction","resource-adapter-name=RaApplicationName"}
          ),
          
          @ConnectorResourceDefinition(
               description = "module-scope resource defined by @ConnectorResourceDefinition", 
               name = "java:module/env/HelloEJB_Annotation_ConnectorResource", 
               className = "javax.resource.cci.ConnectionFactory", 
               properties = {"transactionSupport=LocalTransaction","resource-adapter-name=RaApplicationName"}
          ),
          
          @ConnectorResourceDefinition(
               description = "component-scope resource defined by @ConnectorResourceDefinition", 
               name = "java:comp/env/HelloEJB_Annotation_ConnectorResource", 
               className = "javax.resource.cci.ConnectionFactory", 
               properties = {"transactionSupport=LocalTransaction","resource-adapter-name=RaApplicationName"}
          ) 
     }
)
@Stateless
public class HelloEJB implements Hello {

    public void hello() {

        System.out.println("In HelloEJB::hello()");
    }

    private boolean lookupConnectorResource(String dataSourceName, boolean expectSuccess) {
        Connection c = null;
        try {
            InitialContext ic = new InitialContext();
            System.out.println("lookup dataSource : " + dataSourceName);
            DataSource ds = (DataSource) ic.lookup(dataSourceName);
            c = ds.getConnection();
            System.out.println("got connection : " + c);
            return true;
        } catch (Exception e) {
            if(expectSuccess){
                e.printStackTrace();
            }
            return false;
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
