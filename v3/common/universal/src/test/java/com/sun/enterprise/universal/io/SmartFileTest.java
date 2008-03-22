/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.enterprise.universal.io;

import java.io.File;
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
public class SmartFileTest {

    public SmartFileTest() {
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
     * Test of sanitize method, of class SmartFile.
     */
    @Test
    public void sanitize() {
        //for(String path : FILENAMES) {
            //System.out.println(path + " --> " + SmartFile.sanitize(path));
        //}
    }

    private static final String[] FILENAMES = new String[]
    {
        "c:/",
        "",
        "\\foo",
        "/",
        "/xxx/yyy/././././../yyy",
        "/x/y/z/../../../temp",
        "\\\\",
        "\\\\foo\\goo\\hoo",
        "x/y/../../../..",
    };

}