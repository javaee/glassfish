
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.enterprise.util.io;

import com.sun.enterprise.util.ObjectAnalyzer;
import java.io.File;
import java.io.IOException;
import java.util.Stack;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author bnevins
 */
public class ServerDirsTest {
    public ServerDirsTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        final ClassLoader cl = ServerDirsTest.class.getClassLoader();
        childFile = new File(cl.getResource("grandparent/parent/child").toURI());
        parentFile = new File(cl.getResource("grandparent/parent").toURI());
        grandParentFile = new File(cl.getResource("grandparent").toURI());
        initUserDirs();
        assertTrue(new File(childFile, "readme.txt").isFile());
        assertTrue(childFile.isDirectory());
        assertTrue(parentFile.isDirectory());
        assertTrue(grandParentFile.isDirectory());
        assertTrue(userNextToTopLevelFile.isDirectory());
        assertTrue(userTopLevelFile.isDirectory());
        System.out.println(childFile);
        System.out.println(parentFile);
        System.out.println(grandParentFile);
        System.out.println("Top legal directory: " + userTopLevelFile);
        System.out.println("Next to top legal directory: " + userNextToTopLevelFile);
    }

    /**
     * It is not allowed to use a dir that has no parent...
     * @throws Exception
     */
    @Test(expected = IOException.class)
    public void testNoParent() throws Exception {
        assertNotNull(userTopLevelFile);
        assertTrue(userTopLevelFile.isDirectory());
        assertNull(userTopLevelFile.getParentFile());

        try {
            ServerDirs sd = new ServerDirs(userTopLevelFile);
        }
        catch (IOException e) {
            System.out.println("Got expected IOException.  Here is the message string: " + e.getLocalizedMessage());
            throw e;
        }
    }

    /**
     * Test is no good anymore -- the ServerDirs now always look for "config" dir
     * and domain name.
     *
    @Test
    public void testNoGrandParent() throws Exception {
    assertNotNull(userNextToTopLevelFile);
    assertTrue(userNextToTopLevelFile.isDirectory());
    File parent = userNextToTopLevelFile.getParentFile();
    assertNotNull(parent);
    assertNull(parent.getParentFile());
    assertEquals(parent, userTopLevelFile);

    ServerDirs sd = new ServerDirs(userNextToTopLevelFile);
    }
     **/
    @Test
    public void testSpecialFiles() throws IOException {
        ServerDirs sd = new ServerDirs(childFile);
        assertTrue(sd.getConfigDir() != null);
        assertTrue(sd.getDomainXml() != null);
    }

    @Test
    public void testNoArgConstructor() {
        ServerDirs sd = new ServerDirs();
        // check 3 volunteers for nullness...
        assertNull(sd.getPidFile());
        assertNull(sd.getServerGrandParentDir());
        assertFalse(sd.isValid());
    }

    private static void initUserDirs() {
        // this is totally developer-environment dependent!
        // very inefficient but who cares -- this is a unit test.
        // we need this info to simulate an illegal condition like
        // specifying a directory that has no parent and/or grandparent

        Stack<File> stack = new Stack<File>();
        File f = childFile;  // guaranteed to have a valid parent and grandparent

        do {
            stack.push(f);
            f = f.getParentFile();
        }
        while(f != null);

        // the first pop has the top-level
        // the next pop has the next-to-top-level
        userTopLevelFile = stack.pop();
        userNextToTopLevelFile = stack.pop();
    }
    private static File childFile;
    private static File parentFile;
    private static File grandParentFile;
    private static File userTopLevelFile;
    private static File userNextToTopLevelFile;
}
