package com.sun.s1asdev.ejb.ejb30.hello.session3;

import javax.ejb.Stateless;

import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.EntityManager;
import javax.naming.InitialContext;
import javax.annotation.sql.*;
import javax.sql.DataSource;
import java.sql.Connection;


@DataSourceDefinitions(
        value = {

                @DataSourceDefinition(name = "java:global/HelloEJB_DataSource",
                        className = "org.apache.derby.jdbc.EmbeddedXADataSource",
                        user = "APP",
                        password = "APP",
                        databaseName = "hello-ejb-global",
                        properties = {"connectionAttributes=;create=true"}
                ),

                @DataSourceDefinition(name = "java:comp/HelloEJB_DataSource",
                        className = "org.apache.derby.jdbc.EmbeddedXADataSource",
                        user = "APP",
                        password = "APP",
                        databaseName = "hello-ejb-comp",
                        properties = {"connectionAttributes=;create=true"}
                )

        }
)

@Stateless
public class HelloEJB implements Hello {

    public void hello() {

        boolean global = lookupDataSource("java:global/HelloEJB_DataSource");
        boolean comp = lookupDataSource("java:comp/HelloEJB_DataSource");

        boolean globalHelloStatefulEJB = lookupDataSource("java:global/HelloStatefulEJB_DataSource");
        boolean compHelloStatefulEJB = lookupDataSource("java:comp/HelloStatefulEJB_DataSource");

        boolean globalServlet = lookupDataSource("java:global/Servlet_DataSource");
        boolean compServlet = lookupDataSource("java:comp/Servlet_DataSource");

        boolean globalServlet_DD_DataSource = lookupDataSource("java:global/Servlet_DD_DataSource");
        boolean compServlet_DD_DataSource = lookupDataSource("java:comp/Servlet_DD_DataSource");

        boolean globalHelloStateful_DD_DataSource = lookupDataSource("java:global/HelloStatefulEJB_DD_DataSource");
        boolean compHelloStateful_DD_DataSource = lookupDataSource("java:comp/HelloStatefulEJB_DD_DataSource");

        boolean globalHello_DD_DataSource = lookupDataSource("java:global/HelloEJB_DD_DataSource");
        boolean compHello_DD_DataSource = lookupDataSource("java:comp/HelloEJB_DD_DataSource");


        if (global && comp && globalHelloStatefulEJB && !compHelloStatefulEJB && globalServlet
                && !compServlet && globalServlet_DD_DataSource && !compServlet_DD_DataSource
                && globalHelloStateful_DD_DataSource && !compHelloStateful_DD_DataSource &&
                globalHello_DD_DataSource && compHello_DD_DataSource) {
            System.out.println("HelloEJB successful datasource definitions lookup");
        } else {
            System.out.println("HelloEJB datasource definitions lookup failure");
            throw new RuntimeException("HelloEJB failure");
        }


        System.out.println("In HelloEJB::hello()");
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
