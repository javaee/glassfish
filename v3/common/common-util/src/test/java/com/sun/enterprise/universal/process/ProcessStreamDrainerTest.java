/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.enterprise.universal.process;

import com.sun.enterprise.util.OS;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
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
public class ProcessStreamDrainerTest {

    public ProcessStreamDrainerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        if(windows)
        {
            commands.add("c:/dev/jdk/bin/jps.exe");
            //commands.add("c:/dev/mks/mksnt/ls.exe");
        }
        else {
            commands.add("ls");
        }
        pb = new ProcessBuilder(commands);
  }

    @After
    public void tearDown() {
    }

    /**
     * Test of drain method, of class ProcessStreamDrainer.
     */
    @Test
    public void testSave() throws IOException, InterruptedException {
        System.out.println("save test");
        Process process = pb.start();
        ProcessStreamDrainer drainer = ProcessStreamDrainer.save(processName, process);
        drainer.waitFor();
        System.out.println("OUT:  " + drainer.getOutString());
        System.out.println("ERR:  " + drainer.getErrString());
        //assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    private static final boolean windows = OS.isWindows();
    private static final String processName = "Directory Listing";
    private final List<String>   commands = new LinkedList<String>();
    private                 ProcessBuilder pb;
}