/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.enterprise.admin.cli.remote;

import java.io.ByteArrayOutputStream;
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
public class RemoteResponseManagerTest {

    public RemoteResponseManagerTest() {
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

    @Test
    public void foo() {}
    /**
     * Test of process method, of class RemoteResponseManager.
     *
    @Test (expected=RemoteFailureException.class)
    public void badCodeTest() throws Exception {
        RemoteResponseManager rrm = new RemoteResponseManager(getFakeBAOS(), 404);
    }
    @Test (expected=RemoteSuccessException.class)
    public void goodCodeTest() throws Exception {
        RemoteResponseManager rrm = new RemoteResponseManager(getFakeBAOS(), 200);
    }

    @Test (expected=RemoteFailureException.class)
    public void nullBaosTest() throws Exception {
        RemoteResponseManager rrm = new RemoteResponseManager(null, 200);
    }

    @Test (expected=RemoteFailureException.class)
    public void emptyBaosTest() throws Exception {
        RemoteResponseManager rrm = new RemoteResponseManager(new ByteArrayOutputStream(), 200);
    }
*/
    private ByteArrayOutputStream getFakeBAOS() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write('A');
        baos.write('b');
        baos.write('C');
        return baos;
    }
}