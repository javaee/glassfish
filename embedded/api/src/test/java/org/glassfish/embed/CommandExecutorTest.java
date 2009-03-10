/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.embed;

import org.glassfish.embed.util.LoggerHelper;
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
        System.out.println("\n*********  setUpClass in CETest\n");
        myGF = Server.getServer("server");
        if(myGF == null) {
            EmbeddedInfo ei = new EmbeddedInfo();
            ei.setServerName("server");
            ei.setHttpPort(8080);
            ei.setAdminHttpPort(4848);
            EmbeddedFileSystem efs = ei.getFileSystem();
            efs.setInstallRoot(new File("cetest"));
            efs.setAutoDelete(true);
            myGF = new Server(ei);
        }
        try {
            myGF.start();
        }
        catch(Exception e) {
            // expected...
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        System.out.println("\n*********  tearClass in CETest\n");
        myGF.stop();
    }


    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testCreateJdbcConnectionPoolSuccess() {
        CommandExecution ce = null;
        CommandParameters cp = new CommandParameters();
        cp.setOperand("DerbyPool");
        cp.setOption("datasourceclassname", "org.apache.derby.jdbc.ClientDataSource");
        cp.setOption("isisolationguaranteed", "false");
        cp.setOption("restype", "javax.sql.DataSource");
        cp.setOption("property", "PortNumber=1527:Password=APP:User=APP:serverName=localhost:DatabaseName=sun-appserv-samples:connectionAttributes=\\;create\\\\=true");
        try {
            ce = myGF.execute("create-jdbc-connection-pool", cp);
        } catch (Exception ex) {
            LoggerHelper.severe("testCreateJdbcConnectionPoolSuccess failed");
            ex.printStackTrace();
            fail();
        }
        assertEquals(ActionReport.ExitCode.SUCCESS, ce.getExitCode());
    }

    @Test
    public void testCreateJdbcResourceSuccess() {
        CommandExecution ce = null;
        CommandParameters cp = new CommandParameters();
        cp.setOperand("jdbc/__default");
        cp.setOption("connectionpoolid", "DerbyPool");

        try {
            ce = myGF.execute("create-jdbc-resource", cp);
        } catch (Exception ex) {
            LoggerHelper.severe("testCreateJdbcResourceSuccess failed");
            ex.printStackTrace();
            fail();
        }
        assertEquals(ActionReport.ExitCode.SUCCESS, ce.getExitCode());
    }

    @Test
    public void testDeleteJdbcResourceSuccess() {
        CommandExecution ce = null;
        CommandParameters cp = new CommandParameters();
        cp.setOperand("jdbc/__default");
        try {
            ce = myGF.execute("delete-jdbc-resource", cp);
        } catch (Exception ex) {
            LoggerHelper.severe("testDeleteJdbcResourceSuccess failed");
            ex.printStackTrace();
            fail();
        }
        assertEquals(ActionReport.ExitCode.SUCCESS, ce.getExitCode());
    }

    @Test
    public void testDeleteJdbcConnectionPoolSuccess() {
        CommandExecution ce = null;
        CommandParameters cp = new CommandParameters();
        cp.setOperand("DerbyPool");
        try {
            ce = myGF.execute("delete-jdbc-connection-pool", cp);
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
        CommandExecution ce = null;
        CommandParameters cp = new CommandParameters();
        cp.setOperand("poolA");
        try {
            ce = myGF.execute("create-jdbc-connection-pool", cp);
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
        CommandExecution ce = null;
        CommandExecution ce2 = null;
        try {
            CommandParameters cp = new CommandParameters();
            cp.setOperand(file.getCanonicalPath());
            cp.setOption("force", "true");
            ce = myGF.execute("deploy", cp);

            CommandParameters cp2 = new CommandParameters();
            cp2.setOperand("simple");
            ce2 = myGF.execute("undeploy", cp2);
            
        } catch (Exception ex) {
            LoggerHelper.severe("testDeploySuccess failed");
            ex.printStackTrace();
            fail();
        }
        assertEquals(ActionReport.ExitCode.SUCCESS, ce.getExitCode());
        assertEquals(ActionReport.ExitCode.SUCCESS, ce2.getExitCode());

        try {
            Socket socket = new Socket("localhost", 4848);
            Socket socket2 = new Socket("localhost", 8080);
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
        CommandExecution ce = null;
        CommandParameters cp = new CommandParameters();
        cp.setOperand(file);
        try {
            ce = myGF.execute("deploy", cp);
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
        CommandExecution ce = null;
        CommandParameters cp = new CommandParameters();
        cp.setOperand("HTTP_LISTENER_PORT=38080:HTTP_SSL_LISTENER_PORT=38181");
        
        try {
            ce = myGF.execute("create-system-properties", cp);
            assertEquals(ActionReport.ExitCode.SUCCESS, ce.getExitCode());
            
            CommandExecution ce2 = myGF.execute("list-system-properties", new CommandParameters());
            assertEquals(ActionReport.ExitCode.SUCCESS, ce2.getExitCode());
            //java.util.List<ActionReport.MessagePart> l = ce.getReport().getTopMessagePart().getChildren();
            //assertEquals("HTTP_LISTENER_PORT=38080",l.get(0).getMessage());
        } catch (Exception ex) {
            LoggerHelper.severe("testCreateSystemPropertiesSuccess failed");
            ex.printStackTrace();
            fail();
        }
    }

    private static Server myGF;
}