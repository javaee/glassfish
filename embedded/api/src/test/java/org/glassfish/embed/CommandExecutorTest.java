/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.embed;

import org.glassfish.api.ActionReport;
import java.io.File;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jennifer
 */

public class CommandExecutorTest {

    public CommandExecutorTest() throws EmbeddedException {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        myGF = Server.getServer("server");
        if(myGF == null) {
            EmbeddedInfo ei = new EmbeddedInfo();
            ei.setServerName("server");
            EmbeddedFileSystem efs = new EmbeddedFileSystem();
            efs.setRoot(new File("cetest"));
            efs.setAutoDelete(true);
            ei.setFileSystem(efs);
            myGF = new Server(ei);
        }
        try {
            myGF.start();
        }
        catch(Exception e) {
            // expected...
        }
        ce = new CommandExecutor(myGF);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        myGF.stop();
    }


    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
        options.clear();
    }
    //@Ignore
    @Test
    public void testCreateJdbcConnectionPoolSuccess() {
        options.setProperty("datasourceclassname", "org.apache.derby.jdbc.ClientDataSource");
        options.setProperty("isisolationguaranteed", "false");
        options.setProperty("restype", "javax.sql.DataSource");
        options.setProperty("property", "PortNumber=1527:Password=APP:User=APP:serverName=localhost:DatabaseName=sun-appserv-samples:connectionAttributes=\\;create\\\\=true");
        options.setProperty("DEFAULT", "DerbyPool");
        try {
            ce.execute("create-jdbc-connection-pool", options);
        } catch (Exception ex) {
            LoggerHelper.severe("testCreateJdbcConnectionPoolSuccess failed");
            ex.printStackTrace();
            fail();
        }
        assertEquals(ActionReport.ExitCode.SUCCESS, ce.getExitCode());
    }
    //@Ignore
    @Test
    public void testCreateJdbcResourceSuccess() {
        options.setProperty("connectionpoolid", "DerbyPool");
        options.setProperty("DEFAULT", "jdbc/__default");
        try {
            ce.execute("create-jdbc-resource", options);
        } catch (Exception ex) {
            LoggerHelper.severe("testCreateJdbcResourceSuccess failed");
            ex.printStackTrace();
            fail();
        }
        assertEquals(ActionReport.ExitCode.SUCCESS, ce.getExitCode());
    }
    //@Ignore
    @Test
    public void testDeleteJdbcResourceSuccess() {
        options.setProperty("DEFAULT", "jdbc/__default");
        try {
            ce.execute("delete-jdbc-resource", options);
        } catch (Exception ex) {
            LoggerHelper.severe("testDeleteJdbcResourceSuccess failed");
            ex.printStackTrace();
            fail();
        }
        assertEquals(ActionReport.ExitCode.SUCCESS, ce.getExitCode());
    }
    //@Ignore
    @Test
    public void testDeleteJdbcConnectionPoolSuccess() {
        options.setProperty("DEFAULT", "DerbyPool");
        try {
            ce.execute("delete-jdbc-connection-pool", options);
        } catch (Exception ex) {
            LoggerHelper.severe("testDeleteJdbcConnectionPoolSuccess failed");
            ex.printStackTrace();
            fail();
        }
        assertEquals(ActionReport.ExitCode.SUCCESS, ce.getExitCode());
    }
    //@Ignore
    @Test
    public void testCreateJdbcConnectionPoolFail() {
        options.setProperty("DEFAULT", "poolA");
        try {
            ce.execute("create-jdbc-connection-pool", options);
        } catch (Exception ex) {
            boolean isEmbEx = ex instanceof EmbeddedException;
            assertTrue(isEmbEx);
            if (!isEmbEx) {
                LoggerHelper.severe("testCreateJdbcConnectionPoolFail failed");
                ex.printStackTrace();
                fail();
            }
        }
        assertEquals(ActionReport.ExitCode.FAILURE, ce.getExitCode());
    }
    @Ignore
    @Test
    public void testDeploySuccess() {
        String file = "api/target/test-classes/simple.war";
        options.setProperty("DEFAULT", file);
        try {
            ce.execute("deploy", options);
        } catch (Exception ex) {
            LoggerHelper.severe("testDeploySuccess failed");
            ex.printStackTrace();
            fail();
        }
        assertEquals(ActionReport.ExitCode.SUCCESS, ce.getExitCode());
    }
    @Ignore
    @Test
    public void testDeployFail() {
        String file = "foo";
        options.setProperty("DEFAULT", file);
        try {
            ce.execute("deploy", options);
        } catch (Exception ex) {
            boolean isEmbEx = ex instanceof EmbeddedException;
            assertTrue(isEmbEx);
            if (!isEmbEx) {
                LoggerHelper.severe("testDeployFail failed");
                ex.printStackTrace();
                fail();
            }
        }
        assertEquals(ActionReport.ExitCode.FAILURE, ce.getExitCode());
    }
    //@Ignore
    @Test
    public void testCreateSystemPropertiesSuccess() {
        options.setProperty("DEFAULT", "HTTP_LISTENER_PORT=38080:HTTP_SSL_LISTENER_PORT=38181");
        try {
            ce.execute("create-system-properties", options);
            assertEquals(ActionReport.ExitCode.SUCCESS, ce.getExitCode());
            options.clear();
            ce.execute("list-system-properties", options);
            //java.util.List<ActionReport.MessagePart> l = ce.getReport().getTopMessagePart().getChildren();
            //assertEquals("HTTP_LISTENER_PORT=38080",l.get(0).getMessage());
        } catch (Exception ex) {
            LoggerHelper.severe("testCreateSystemPropertiesSuccess failed");
            ex.printStackTrace();
            fail();
        }
        assertEquals(ActionReport.ExitCode.SUCCESS, ce.getExitCode());
    }

    private Properties options = new Properties();
    private static CommandExecutor ce;
    private static Server myGF;
}