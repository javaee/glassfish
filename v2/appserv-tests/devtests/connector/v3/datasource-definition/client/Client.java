/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.ejb30.hello.session3;

import java.net.*;
import java.io.*;
import javax.naming.*;
import javax.sql.*;
import java.sql.*;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;


import javax.annotation.sql.*;


@DataSourceDefinitions(
        value = {


               @DataSourceDefinition(name = "java:global/env/Appclient_DataSource",
                        className = "org.apache.derby.jdbc.ClientXADataSource",
                        portNumber = 1527,
                        serverName = "localhost",
                        user = "APP",
                        password = "APP",
                        databaseName = "hello-client-annotation-global",
                        properties = {"connectionAttributes=;create=true"},
                        isolationLevel = 8
                ),


                @DataSourceDefinition(name = "java:comp/env/Appclient_DataSource",
                        minPoolSize = 0,
                        initialPoolSize = 0,
                        className = "org.apache.derby.jdbc.ClientXADataSource",
                        portNumber = 1527,
                        serverName = "localhost",
                        user = "APP",
                        password = "APP",
                        databaseName = "hello-client-annotation-comp",
                        properties = {"connectionAttributes=;create=true"},
                        isolationLevel = -1
                ),
                //test case to ensure that isolationLevel specified as integer in annotation
                //is converted to DD equivalent string.
                //The only way to confirm the fix for IT 9292 is that appclient console
                //should not have "invalidDescriptorFailureException" printed
                @DataSourceDefinition(name = "java:comp/env/Appclient_DataSource1",
                        minPoolSize = 0,
                        initialPoolSize = 0,
                        className = "org.apache.derby.jdbc.ClientXADataSource",
                        portNumber = 1527,
                        serverName = "localhost",
                        user = "APP",
                        password = "APP",
                        databaseName = "hello-client-annotation-comp",
                        properties = {"connectionAttributes=;create=true"},
                        isolationLevel = 4
                ),
                //test case to ensure that isolationLevel specified as integer in annotation
                //is converted to DD equivalent string.
                //The only way to confirm the fix for IT 9292 is that appclient console
                //should not have "invalidDescriptorFailureException" printed
                //no isolation level specified
		@DataSourceDefinition(name = "java:comp/env/Appclient_DataSource2",
                        minPoolSize = 0,
                        initialPoolSize = 0,
                        className = "org.apache.derby.jdbc.ClientXADataSource",
                        portNumber = 1527,
                        serverName = "localhost",
                        user = "APP",
                        password = "APP",
                        databaseName = "hello-client-annotation-comp",
                        properties = {"connectionAttributes=;create=true"}
                )
        }
)

public class Client {

    private String host;
    private String port;

    private static SimpleReporterAdapter stat =
            new SimpleReporterAdapter("appserv-tests");


    public Client(String[] args) {
        host = (args.length > 0) ? args[0] : "localhost";
        port = (args.length > 1) ? args[1] : "4848";
    }

    public static void main(String[] args) {
        stat.addDescription("datasource-definitionclient");
        Client client = new Client(args);
        client.doTest();
        stat.printSummary("datasource-definitionID");
    }

    public void doTest() {

        String env = null;
        try {

/*
            InitialContext ic = new InitialContext();
            boolean global = lookupDataSource("java:global/env/Appclient_DataSource");
            boolean comp = lookupDataSource("java:comp/env/Appclient_DataSource");
            if (global && comp) {
                System.out.println("4444 appclient Success");
                System.out.println("AppClient successful injection of EMF references!");
            } else {
                System.out.println("4444 appclient failure");
                throw new RuntimeException("Appclient failure");
            }
*/



            boolean compds = lookupDataSource("java:comp/env/compds",true);
            boolean defaultds = lookupDataSource("java:comp/env/defaultds",true);
            boolean moduleds = lookupDataSource("java:module/env/moduleds", true);
            boolean appds = lookupDataSource("java:app/env/appclient/appds",true);
            boolean globalds = lookupDataSource("java:global/env/ts/datasource/appclient/globalds",true);

            boolean comp = lookupDataSource("java:comp/env/Appclient_DataSource",true);
            boolean comp_dd = lookupDataSource("java:comp/env/Appclient_DD_DataSource",true);
            boolean globalAppclient = lookupDataSource("java:global/env/Appclient_DataSource", true);

            boolean globalServlet_DataSource = lookupDataSource("java:global/env/Servlet_DataSource", true);
            boolean compServlet_DataSource = lookupDataSource("java:comp/env/Servlet_DataSource", false);

            boolean globalHelloSfulEJB = lookupDataSource("java:global/env/HelloStatefulEJB_DataSource", true);
            boolean compHelloSfulEJB = lookupDataSource("java:comp/env/HelloStatefulEJB_DataSource", false);
            boolean appHelloStatefulEjb = lookupDataSource("java:app/env/HelloStatefulEJB_DataSource", true);

            boolean globalHelloEJB = lookupDataSource("java:global/env/HelloEJB_DataSource", true);
            boolean compHelloEJB = lookupDataSource("java:comp/env/HelloEJB_DataSource", false);

            boolean globalServlet_DD_DataSource = lookupDataSource("java:global/env/Servlet_DD_DataSource", true);
            boolean compServlet_DD_DataSource = lookupDataSource("java:comp/env/Servlet_DD_DataSource", false);

            boolean globalHelloStateful_DD_DataSource = lookupDataSource("java:global/env/HelloStatefulEJB_DD_DataSource", true);
            boolean compHelloStateful_DD_DataSource = lookupDataSource("java:comp/env/HelloStatefulEJB_DD_DataSource", false);

            boolean globalHello_DD_DataSource = lookupDataSource("java:global/env/HelloEJB_DD_DataSource", true);
            boolean compHello_DD_DataSource = lookupDataSource("java:comp/env/HelloEJB_DD_DataSource", false);



            if (compds && defaultds && moduleds && appds && globalds && comp && comp_dd &&  globalAppclient && globalServlet_DataSource && !compServlet_DataSource && globalHelloSfulEJB &&
                    globalServlet_DD_DataSource && !compServlet_DD_DataSource
                    && !compHelloSfulEJB && globalHelloEJB
                    && !compHelloEJB && globalHelloStateful_DD_DataSource
                    && !compHelloStateful_DD_DataSource && globalHello_DD_DataSource
                    && !compHello_DD_DataSource && appHelloStatefulEjb)
            {
                System.out.println("AppClient successful lookup of datasource definitions !");
                stat.addStatus("DataSource-Definition-appclient-test", stat.PASS);
            } else {
                System.out.println("AppClient lookup not successful" );
                stat.addStatus("DataSource-Definition-appclient-test", stat.FAIL);
                throw new RuntimeException("Appclient failure during lookup of datasource definitions");
            }

            String url = "http://" + host + ":" + port +
                    "/datasource-definition/servlet";
            System.out.println("invoking DataSource-Definition test servlet at " + url);
            int code = invokeServlet(url);


            if (code != 200) {
                System.out.println("Incorrect return code: " + code);
                stat.addStatus("DataSource-Definition-web-ejb-test", stat.FAIL);
            } else {
                stat.addStatus("DataSource-Definition-web-ejb-test", stat.PASS);
            }
        } catch (Exception ex) {
            System.out.println("DataSource-Definition web & ejb test failed.");
            stat.addStatus("DataSource-Definition-web-ejb-test", stat.FAIL);
            ex.printStackTrace();
        }

        return;

    }

    private boolean lookupDataSource(String dataSourceName, boolean expectSuccess) {
        Connection c = null;
        try {
            InitialContext ic = new InitialContext();
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

    private int invokeServlet(String url) throws Exception {

        URL u = new URL(url);

        HttpURLConnection c1 = (HttpURLConnection) u.openConnection();
        int code = c1.getResponseCode();
        InputStream is = c1.getInputStream();
        BufferedReader input = new BufferedReader(new InputStreamReader(is));
        String line = null;
        while ((line = input.readLine()) != null)
            System.out.println(line);
        if (code != 200) {
            System.out.println("Incorrect return code: " + code);
        }
        return code;
    }

}

