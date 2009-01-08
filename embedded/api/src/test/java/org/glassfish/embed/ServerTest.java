/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.embed;

import com.sun.enterprise.deploy.shared.ArchiveFactory;
import com.sun.enterprise.universal.io.SmartFile;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.v3.server.ApplicationLifecycle;
import com.sun.hk2.component.InhabitantsParser;
import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.Properties;
import java.util.logging.Level;
import org.apache.catalina.Engine;
import org.glassfish.api.container.Sniffer;
import org.glassfish.api.deployment.archive.ArchiveHandler;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;
import org.jvnet.hk2.component.Habitat;

/**
 *
 * @author Jennifer
 */
public class ServerTest {

    public ServerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        EmbeddedInfo info = new EmbeddedInfo();
        info.setServerName("server");
        server = Server.getServer(info.name);

        if(server == null)
            server = new Server(info);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        try {
            server.stop();
        } catch (EmbeddedException ee) {

        }
    }

    @Before
    public void setUp() throws EmbeddedException {
        try {
            server.stop();
        } catch (EmbeddedException ee) {

        }
    }

    @After
    public void tearDown() throws EmbeddedException {
        try {
            server.stop();
        } catch (EmbeddedException ee) {

        }
    }

    /* server mustBeStarted */

    /**
     * Test of deploy method, of class Server.
     */
    @Test
    public void testDeploy_File_Success() throws Exception {
        System.out.println("deploy success");
        File archive = SmartFile.sanitize(new File("target/test-classes/simple.war"));

        assertTrue(archive.exists());

        Application app = null;
        server.start();
        try {
            app = server.deploy(archive);
        }
        catch(Exception e) {
            System.out.println("Unexpected Exception: " + e);
            fail("test failed: testDeploy_File_Success");
        }

        assertNotNull(app);
        app.undeploy();
        server.stop();
    }

    @Test(expected=EmbeddedException.class)
    public void testDeploy_File_Fail() throws Exception {
        System.out.println("deploy fail");
        File archive = SmartFile.sanitize(new File("target/test-classes/simple.war"));
        assertTrue(archive.exists());

        Application app = null;
        try {
            app = server.deploy(archive);
        } catch(EmbeddedException ee) {
            System.out.println("Expected Exception: " + ee);
            throw ee;
        } catch (Exception e) {
            System.out.println("Unxpected Exception: " + e);
            fail("test failed: testDeploy_File_Fail");
        }
    }

    @Test
    public void testDeploy_RA_Success() throws Exception {
        File archive = SmartFile.sanitize(new File("target/test-classes/simple.war"));
        ArchiveFactory archiveFactory = server.habitat.getComponent(ArchiveFactory.class);
        ReadableArchive a = archiveFactory.openArchive(archive);
        assertTrue(archive.exists());

        ApplicationLifecycle appLife = server.habitat.getComponent(ApplicationLifecycle.class);
        ArchiveHandler h = appLife.getArchiveHandler(a);
        File appDir = new File(server.getFileSystem().getAppsDir(), a.getName());
        FileUtils.whack(appDir);
        appDir.mkdirs();
        h.expand(a, archiveFactory.createArchive(appDir));
        a.close();
        a = archiveFactory.openArchive(appDir);

        Application app = null;
        server.start();
        try {
            app = server.deploy(a);
        } catch(Exception e) {
            System.out.println("Unexpected Exception: " + e);
            fail("failed test: testDeploy_RA_Success");
        }

        assertNotNull(app);
        app.undeploy();
        server.stop();
    }

    @Test(expected=EmbeddedException.class)
    public void testDeploy_RA_Fail() throws Exception {
        File archive = SmartFile.sanitize(new File("target/test-classes/simple.war"));
        ArchiveFactory archiveFactory = server.habitat.getComponent(ArchiveFactory.class);
        ReadableArchive a = archiveFactory.openArchive(archive);

        Application app = null;
        try {
            app = server.deploy(a);
        } catch(EmbeddedException ee) {
            System.out.println("Expected Exception: " + ee);
            throw ee;
        } catch(Exception e) {
            System.out.println("Unexpected Exception: " + e);
            fail("failed test: testDeploy_RA_Fail");
        }
    }

    @Test
    public void testStop_Success() throws Exception {
        server.start();
        try {
            server.stop();
        } catch (Exception e) {
            System.out.println("Unxpected Exception: " + e);
            fail("failed test: testStop_Success");
        }
    }

    @Test(expected=EmbeddedException.class)
    public void testStop_Fail() throws Exception {
        try {
            server.stop();
        } catch(EmbeddedException ee) {
            System.out.println("Expected Exception: " + ee);
            throw ee;
        } catch (Exception e) {
            System.out.println("Unxpected Exception: " + e);
            fail("failed test: testStop_Fail");
        }
    }

    @Test
    public void testGetEngines_Success() throws Exception {
        System.out.println("getEngines Success");
        String expResult = "com.sun.appserv";
        String result = null;
        server.start();
        Engine[] engines = server.getEngines();
        for (Engine e : engines) {
            result = e.getName();
        }
        assertEquals(expResult, result);
    }

    @Test(expected=EmbeddedException.class)
    public void testGetEngines_Fail() throws Exception {
        System.out.println("getEngines Fail");
        try {
            server.getEngines();
        } catch(EmbeddedException ee) {
            System.out.println("Expected Exception: " + ee);
            throw ee;
        } catch (Exception e) {
            System.out.println("Unxpected Exception: " + e);
            fail("failed test: testGetEgines_Fail");
        }
    }

    /* server mustNotBeStarted */
    
    @Test
    public void testCreateVirtualServer_Success() throws Exception {
        EmbeddedInfo info = new EmbeddedInfo();
        info.setServerName("server2");
        EmbeddedFileSystem efs = new EmbeddedFileSystem();
        efs.setRoot(new File("servertest2"));
        efs.setAutoDelete(true);
        info.setFileSystem(efs);
        Server server = new Server(info);
        try {
            server.createVirtualServer(server.createHttpListener(3333));
        } catch (Exception e) {
            System.out.println("Unxpected Exception: " + e);
            fail("failed test: testCreateVirtualServer_Success");
        }
    }

    @Test(expected=EmbeddedException.class)
    public void testCreateVirtualServer_Fail() throws Exception {
        EmbeddedInfo info = new EmbeddedInfo();
        info.setServerName("server2");
        Server server = new Server(info);
        server.start();
        try {
            server.createVirtualServer(server.createHttpListener(4444));
        } catch(EmbeddedException ee) {
            System.out.println("Expected Exception: " + ee);
            throw ee;
        } catch (Exception e) {
            System.out.println("Unxpected Exception: " + e);
            fail("failed test: testCreateVirtualServer_Fail");
        }
    }

    @Test
    public void testCreateHttpListener_Success() throws Exception {
        EmbeddedInfo info = new EmbeddedInfo();
        info.setServerName("server2");
        EmbeddedFileSystem efs = new EmbeddedFileSystem();
        efs.setRoot(new File("servertest"));
        efs.setAutoDelete(true);
        info.setFileSystem(efs);
        Server server = new Server(info);
        try {
            server.createHttpListener(3333);
        } catch (Exception e) {
            System.out.println("Unxpected Exception: " + e);
            fail("failed test: testCreateHttpListener_Success");
        }
    }

    @Test(expected=EmbeddedException.class)
    public void testCreateHttpListener_Fail() throws Exception {
        EmbeddedInfo info = new EmbeddedInfo();
        info.setServerName("server2");
        EmbeddedFileSystem efs = new EmbeddedFileSystem();
        efs.setRoot(new File("servertest"));
        efs.setAutoDelete(true);
        info.setFileSystem(efs);
        Server server = new Server(info);
        server.start();
        try {
            server.createHttpListener(3333);
        } catch(EmbeddedException ee) {
            System.out.println("Expected Exception: " + ee);
            throw ee;
        } catch (Exception e) {
            System.out.println("Unxpected Exception: " + e);
            fail("failed test: testCreateHttpListener_Fail");
        }
    }

    private static Server server;

}