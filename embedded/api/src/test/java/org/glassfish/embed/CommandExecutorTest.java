/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.embed;

import com.sun.enterprise.universal.io.SmartFile;
import org.glassfish.api.ActionReport;
import java.io.File;
import java.net.Socket;
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

    @Test(expected=EmbeddedException.class)
    public void testCreateJdbcConnectionPoolFail() throws EmbeddedException {
        System.out.println("Negative Test: CommandExecutorTest testCreateJdbcConnectionPoolFail()");
        System.out.println("Severe messages expected...");
        options.setProperty("DEFAULT", "poolA");
        try {
            ce.execute("create-jdbc-connection-pool", options);
        } catch (EmbeddedException ee) {
            System.out.println("Expected Exception: " + ee.getLocalizedMessage());
            throw ee;
        } catch (Exception ex) {
            LoggerHelper.severe("Unexpected Exception: " + ex);
            fail("test failed: testCreateJdbcConnectionPoolFail");
        }
        assertEquals(ActionReport.ExitCode.FAILURE, ce.getExitCode());
    }

    @Test
    public void testDeploySuccess() {
        System.out.println("CommandExecutorTest testDeploySuccess()");
        File file = SmartFile.sanitize(new File("target/test-classes/simple.war"));
        assertTrue(file.exists());
        try {
            options.setProperty("DEFAULT", file.getCanonicalPath());
            options.setProperty("force", "true");
            ce.execute("deploy", options);
            options.clear();
            options.setProperty("DEFAULT", "simple");
            ce.execute("undeploy", options);
        } catch (Exception ex) {
            LoggerHelper.severe("testDeploySuccess failed");
            ex.printStackTrace();
            fail();
        }
        assertEquals(ActionReport.ExitCode.SUCCESS, ce.getExitCode());

        try {
            Socket socket = new Socket("localhost", 4848);
            Socket socket2 = new Socket("localhost", 8888);
            assertTrue(socket.isConnected());
            assertTrue(socket2.isConnected());
            socket.close();
            socket2.close();
        } catch(Exception e) {
            fail(e.getLocalizedMessage());
        }

    }

    @Test(expected=EmbeddedException.class)
    public void testDeployFail() throws EmbeddedException {
        System.out.println("Negative Test: CommandExecutorTest testDeployFail()");
        System.out.println("Severe messages expected...");
        String file = "foo";
        options.setProperty("DEFAULT", file);
        try {
            ce.execute("deploy", options);
        } catch (EmbeddedException ee) {
            System.out.println("Expected Exception: " + ee.getLocalizedMessage());
            throw ee;
        } catch (Exception ex) {
            System.out.print("Unexpected Exception: " + ex);
            fail("test failed: testDeployFail");
        }
        assertEquals(ActionReport.ExitCode.FAILURE, ce.getExitCode());
    }

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