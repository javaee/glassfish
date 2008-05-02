/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.enterprise.universal.glassfish;

import com.sun.enterprise.universal.collections.*;
import java.util.Map;
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
 * @author bnevins
 * Note -- this UNIT TEST can't be run in the same JVM as GFSystemTest was run from.
 * To use the rest -- temporarily rename it to GFSystemTest
 * note also that this unit test will not run as is from maven -- because
 * the name does not match any class in the normal build area...
 * 
 */
public class GFSystemTest2 {

    public GFSystemTest2() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of GFSystem for the case where there are multiple instances in a JVM
     *
    @Test
    public void threadTest() {
        try {
            Thread t1 = new ParentThread("xxx");
            Thread t2 = new ParentThread("yyy");
            Thread t3 = new ParentThread("zzz");
            t1.start();
            t2.start();
            t3.start();
            t1.join();
            t2.join();
            t3.join();
        } catch (InterruptedException ex) {
            Logger.getLogger(GFSystemTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        assertFalse(failed);
    }
*/
    /**
     * Test of GFSystem for the case where there are multiple instances in a JVM
     * But we screwed-up and called GFSystem from the main thread...
     */
    @Ignore
    @Test
    public void threadTest2() {
        try {
            GFSystem.init();
            Thread t1 = new ParentThread("xxx");
            Thread t2 = new ParentThread("yyy");
            Thread t3 = new ParentThread("zzz");
            t1.start();
            t2.start();
            t3.start();
            t1.join();
            t2.join();
            t3.join();
        } catch (InterruptedException ex) {
            Logger.getLogger(GFSystemTest2.class.getName()).log(Level.SEVERE, null, ex);
        }
        assertTrue(GFSystemTest.failed);
    }

    public static synchronized void setFailure() {
        failed = true;
    }
    private static volatile boolean failed = false;
}
