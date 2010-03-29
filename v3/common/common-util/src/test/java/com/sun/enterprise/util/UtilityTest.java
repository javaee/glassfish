/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.enterprise.util;

import java.util.*;
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
public class UtilityTest {

    public UtilityTest() {
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
     * VERY SIMPLE Test of getEnvOrProp method, of class Utility.
     */
    @Test
    public void testGetEnvOrProp() {
        System.out.println("getEnvOrProp");
        Map<String, String> env = System.getenv();
        Set<String> keys = env.keySet();
        String key = null;
        String value = null;

        // warning:  super-paranoid bullet-proof test ahead!!!
        for (String akey : keys) {
            // Make sure both key and value are kosher

            if(!StringUtils.ok(akey))
                continue;

            // make sure this name:value is NOT in System Properties!
            // empty string counts as a value!
            if(System.getProperty(akey) != null)
                continue;

            String avalue = env.get(akey);

            if(!StringUtils.ok(avalue))
                continue;

            key = akey;
            value = avalue;
            break;
        }

        // allow the case where there are no env. variables.  Probably impossible
        // but this test needs to run on many many many environments and we don't
        // want to fail in such a case.

        if(key == null)
            return;

        assertEquals(Utility.getEnvOrProp(key), value);
        String sysPropValue = "SYS_PROP" + value;
        System.setProperty(key, sysPropValue);
        assertEquals(Utility.getEnvOrProp(key), sysPropValue);
    }
}
