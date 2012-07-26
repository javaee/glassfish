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

        boolean appDSDriver = lookupConnectorResource("java:app/jdbc/appds_driver", true);
        boolean moduleDSDriver= lookupConnectorResource("java:module/env/HelloEJB_DataSource_driver", true);

        boolean global = lookupConnectorResource("java:global/env/HelloEJB_DataSource", true);
        boolean comp = lookupConnectorResource("java:comp/env/HelloEJB_DataSource", true);
        boolean moduleHelloEjb = lookupConnectorResource("java:module/env/HelloEJB_DataSource", true);

        boolean globalHelloStatefulEJB = lookupConnectorResource("java:global/env/HelloStatefulEJB_DataSource", true);
        boolean compHelloStatefulEJB = lookupConnectorResource("java:comp/env/HelloStatefulEJB_DataSource", false);
        boolean appHelloStatefulEjb = lookupConnectorResource("java:app/env/HelloStatefulEJB_DataSource", true);

        boolean globalServlet = lookupConnectorResource("java:global/env/Servlet_DataSource", true);
        boolean compServlet = lookupConnectorResource("java:comp/env/Servlet_DataSource", false);
        boolean appServletDataSource = lookupConnectorResource("java:app/env/Servlet_DataSource", true);
        boolean moduleServletDataSource = lookupConnectorResource("java:module/env/Servlet_DataSource", false);

        boolean globalServlet_DD_DataSource = lookupConnectorResource("java:global/env/Servlet_DD_DataSource", true);
        boolean compServlet_DD_DataSource = lookupConnectorResource("java:comp/env/Servlet_DD_DataSource", false);

        boolean globalHelloStateful_DD_DataSource = lookupConnectorResource("java:global/env/HelloStatefulEJB_DD_DataSource", true);
        boolean compHelloStateful_DD_DataSource = lookupConnectorResource("java:comp/env/HelloStatefulEJB_DD_DataSource", false);

        boolean globalHello_DD_DataSource = lookupConnectorResource("java:global/env/HelloEJB_DD_DataSource", true);
        boolean compHello_DD_DataSource = lookupConnectorResource("java:comp/env/HelloEJB_DD_DataSource", false);


        if (appDSDriver && moduleDSDriver && global && comp && globalHelloStatefulEJB && !compHelloStatefulEJB && globalServlet
                && !compServlet && appServletDataSource && globalServlet_DD_DataSource && !compServlet_DD_DataSource
                && globalHelloStateful_DD_DataSource && !compHelloStateful_DD_DataSource &&
                globalHello_DD_DataSource && compHello_DD_DataSource && appHelloStatefulEjb &&
                moduleHelloEjb && !moduleServletDataSource) {
            System.out.println("HelloEJB successful datasource definitions lookup");
        } else {
            System.out.println("HelloEJB datasource definitions lookup failure");
            throw new RuntimeException("HelloEJB failure");
        }

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
