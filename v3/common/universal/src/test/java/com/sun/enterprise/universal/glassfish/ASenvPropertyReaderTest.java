/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.enterprise.universal.glassfish;

import java.io.File;
import java.util.Map;
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

public class ASenvPropertyReaderTest {

    public ASenvPropertyReaderTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        installDir = new File(
            ASenvPropertyReaderTest.class.getClassLoader().getResource
            ("config/asenv.bat").getPath()).getParentFile().getParentFile();
        System.out.println("INSTALL-DIR: " + installDir);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        pr = new ASenvPropertyReader(installDir);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void test1() {
        // this is too noisy for the build system.
        // I wonder how to get output to the surefire directory?!?
        //System.out.println(pr);
        Map<String,String> props = pr.getProps();
        assertEquals(props.get("com.sun.aas.antLib"), props.get("com.sun.aas.hadbRoot"));
    }
    ASenvPropertyReader pr;
    private static File installDir;
}
