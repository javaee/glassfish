/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.enterprise.universal.collections;

import java.util.Map;
import java.util.jar.*;
import java.util.jar.Manifest;
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
public class ManifestUtilsTest {

    public ManifestUtilsTest() {
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
     * Test of normalize method, of class ManifestUtils.
     */
    @Test
    public void normalize() {
        Manifest m = new Manifest();
        String hasToken = "abc" + ManifestUtils.EOL_TOKEN + "def";
        String convertedHasToken = "abc" + ManifestUtils.EOL + "def";
        Attributes mainAtt = m.getMainAttributes();
        Map<String,Attributes> entries =  m.getEntries();
        Attributes fooAtt = new Attributes();
        entries.put("foo", fooAtt);
        fooAtt.putValue("fooKey", "fooValue");
        fooAtt.putValue("fooKey2", hasToken);
        mainAtt.putValue("mainKey", "mainValue");
        
        Map<String,Map<String,String>> norm = ManifestUtils.normalize(m);
        Map<String,String> normMainAtt = norm.get(ManifestUtils.MAIN_ATTS);
        Map<String,String> normFooAtt = norm.get("foo");
        
        assertTrue(norm.size() == 2);
        assertNotNull(normMainAtt);
        assertNotNull(normFooAtt);
        assertTrue(normMainAtt.size() == 1);
        assertTrue(normFooAtt.size() == 2);
        assertFalse(normFooAtt.get("fooKey2").equals(hasToken));
        assertTrue(normFooAtt.get("fooKey2").equals(convertedHasToken));
        assertFalse(hasToken.equals(convertedHasToken));
        assertEquals(normMainAtt.get("mainKey"), "mainValue");
    }

}
