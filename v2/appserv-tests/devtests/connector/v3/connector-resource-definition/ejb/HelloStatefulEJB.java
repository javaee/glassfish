package com.sun.s1asdev.ejb.ejb30.hello.session3;

import javax.ejb.Stateful;
import javax.annotation.PostConstruct;
import javax.resource.ConnectorResourceDefinitions;
import javax.resource.ConnectorResourceDefinition;
import java.sql.*;
import javax.sql.*;
import javax.naming.*;

@ConnectorResourceDefinitions(
        value = {
                @ConnectorResourceDefinition(
                        description="global-scope resource defined by @ConnectorResourceDefinition",
                        name = "java:global/env/HelloStatefulEJB_ModByDD_ConnectorResource",
                        className = "javax.resource.cci.ConnectionFactory",
                        properties = {"transactionSupport=LocalTransaction","resource-adapter-name=RaApplicationName"}
                ),

                @ConnectorResourceDefinition(
                        description="global-scope resource defined by @ConnectorResourceDefinition",
                        name = "java:global/env/HelloStatefulEJB_Annotation_ConnectorResource",
                        className = "javax.resource.cci.ConnectionFactory",
                        properties = {"transactionSupport=LocalTransaction","resource-adapter-name=RaApplicationName"}
                ),

                @ConnectorResourceDefinition(
                        description="application-scope resource defined by @ConnectorResourceDefinition",
                        name = "java:app/env/HelloStatefulEJB_Annotation_ConnectorResource",
                        className = "javax.resource.cci.ConnectionFactory",
                        properties = {"transactionSupport=LocalTransaction","resource-adapter-name=RaApplicationName"}
                ),

                @ConnectorResourceDefinition(
                        description="module-scope resource defined by @ConnectorResourceDefinition",
                        name = "java:module/env/HelloStatefulEJB_Annotation_ConnectorResource",
                        className = "javax.resource.cci.ConnectionFactory",
                        properties = {"transactionSupport=LocalTransaction","resource-adapter-name=RaApplicationName"}
                ),

                @ConnectorResourceDefinition(
                        description="component-scope resource defined by @ConnectorResourceDefinition",
                        name = "java:comp/env/HelloStatefulEJB_Annotation_ConnectorResource",
                        className = "javax.resource.cci.ConnectionFactory",
                        properties = {"transactionSupport=LocalTransaction","resource-adapter-name=RaApplicationName"}
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
        boolean global = lookupConnectorResource("java:global/env/HelloStatefulEJB_DataSource", true);
        boolean comp = lookupConnectorResource("java:comp/env/HelloStatefulEJB_DataSource", true);
        boolean appHelloStatefulEjb = lookupConnectorResource("java:app/env/HelloStatefulEJB_DataSource", true);

        boolean globalHelloEJB = lookupConnectorResource("java:global/env/HelloEJB_DataSource", true);
        boolean compHelloEJB = lookupConnectorResource("java:comp/env/HelloEJB_DataSource",false);
        boolean moduleHelloEjb = lookupConnectorResource("java:module/env/HelloEJB_DataSource", true);

        boolean globalServlet = lookupConnectorResource("java:global/env/Servlet_DataSource", true);
        boolean compServlet = lookupConnectorResource("java:comp/env/Servlet_DataSource",false);
        boolean appServletDataSource = lookupConnectorResource("java:app/env/Servlet_DataSource", true);
        boolean moduleServletDataSource = lookupConnectorResource("java:module/env/Servlet_DataSource", false);

        boolean globalServlet_DD_DataSource = lookupConnectorResource("java:global/env/Servlet_DD_DataSource", true);
        boolean compServlet_DD_DataSource = lookupConnectorResource("java:comp/env/Servlet_DD_DataSource",false);

        boolean globalHelloStateful_DD_DataSource = lookupConnectorResource("java:global/env/HelloStatefulEJB_DD_DataSource", true);
        boolean compHelloStateful_DD_DataSource = lookupConnectorResource("java:comp/env/HelloStatefulEJB_DD_DataSource",false);

        boolean globalHello_DD_DataSource = lookupConnectorResource("java:global/env/HelloEJB_DD_DataSource", true);
        boolean compHello_DD_DataSource = lookupConnectorResource("java:comp/env/HelloEJB_DD_DataSource",false);

        boolean globalAppLevel_DD_DataSource = lookupConnectorResource("java:global/env/Application_Level_DataSource", true);
        boolean appAppLevel_DD_DataSource = lookupConnectorResource("java:app/env/Application_Level_DataSource", true);

        if (global && comp && globalHelloEJB && !compHelloEJB && globalServlet && appServletDataSource && !compServlet &&
                globalServlet_DD_DataSource && !compServlet_DD_DataSource && globalHelloStateful_DD_DataSource
                && compHelloStateful_DD_DataSource && globalHello_DD_DataSource && !compHello_DD_DataSource
                && appHelloStatefulEjb && moduleHelloEjb && !moduleServletDataSource
                && globalAppLevel_DD_DataSource && appAppLevel_DD_DataSource) {
            System.out.println("StatefulEJB datasource-definitions Success");

        } else {
            System.out.println("StatefulEJB datasource-definitions Failure");
            throw new RuntimeException("StatefulEJB datasource-definitions Failure");
        }
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
