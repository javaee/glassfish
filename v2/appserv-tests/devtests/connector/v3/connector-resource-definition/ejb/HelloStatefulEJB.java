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
