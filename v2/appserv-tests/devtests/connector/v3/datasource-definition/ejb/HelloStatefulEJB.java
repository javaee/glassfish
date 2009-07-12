package com.sun.s1asdev.ejb.ejb30.hello.session3;

import javax.ejb.Stateful;
import javax.annotation.PostConstruct;
import javax.annotation.sql.*;
import java.sql.*;
import javax.sql.*;
import javax.naming.*;

@DataSourceDefinitions(
        value = {

                @DataSourceDefinition(name = "java:global/HelloStatefulEJB_DataSource",
                        className = "org.apache.derby.jdbc.ClientXADataSource",
                        portNumber = 1527,
                        serverName = "localhost",
                        user = "APP",
                        password = "APP",
                        databaseName = "hello-stateful-ejb-global",
                        properties = {"connectionAttributes=;create=true"}
                ),

                @DataSourceDefinition(name = "java:comp/HelloStatefulEJB_DataSource",
                        className = "org.apache.derby.jdbc.ClientXADataSource",
                        portNumber = 1527,
                        serverName = "localhost",
                        user = "APP",
                        password = "APP",
                        databaseName = "hello-stateful-ejb-comp",
                        properties = {"connectionAttributes=;create=true"}
                ),
                // only user should be considered.
                // incorrect values for : className, portNumber, url, properties which should be ignored 
                @DataSourceDefinition(name = "java:global/HelloStatefulEJB_DD_DataSource",
                        className = "UnknownDataSource",
                        portNumber = 9527,
                        serverName = "localhost",
                        user = "APP",
                        url = "test_url",
                        properties = {"connectionAttributes=;create=false"}
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
        boolean global = lookupDataSource("java:global/HelloStatefulEJB_DataSource");
        boolean comp = lookupDataSource("java:comp/HelloStatefulEJB_DataSource");

        boolean globalHelloEJB = lookupDataSource("java:global/HelloEJB_DataSource");
        boolean compHelloEJB = lookupDataSource("java:comp/HelloEJB_DataSource");

        boolean globalServlet = lookupDataSource("java:global/Servlet_DataSource");
        boolean compServlet = lookupDataSource("java:comp/Servlet_DataSource");

        boolean globalServlet_DD_DataSource = lookupDataSource("java:global/Servlet_DD_DataSource");
        boolean compServlet_DD_DataSource = lookupDataSource("java:comp/Servlet_DD_DataSource");

        boolean globalHelloStateful_DD_DataSource = lookupDataSource("java:global/HelloStatefulEJB_DD_DataSource");
        boolean compHelloStateful_DD_DataSource = lookupDataSource("java:comp/HelloStatefulEJB_DD_DataSource");

        boolean globalHello_DD_DataSource = lookupDataSource("java:global/HelloEJB_DD_DataSource");
        boolean compHello_DD_DataSource = lookupDataSource("java:comp/HelloEJB_DD_DataSource");


        if (global && comp && globalHelloEJB && !compHelloEJB && globalServlet && !compServlet &&
                globalServlet_DD_DataSource && !compServlet_DD_DataSource && globalHelloStateful_DD_DataSource
                && compHelloStateful_DD_DataSource && globalHello_DD_DataSource && !compHello_DD_DataSource) {
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

    private boolean lookupDataSource(String dataSourceName) {
        Connection c = null;
        try {
            InitialContext ic = new InitialContext();
            System.out.println("lookup dataSource : " + dataSourceName);
            DataSource ds = (DataSource) ic.lookup(dataSourceName);
            c = ds.getConnection();
            System.out.println("got connection : " + c);
            return true;
        } catch (Exception e) {
            //e.printStackTrace();
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
