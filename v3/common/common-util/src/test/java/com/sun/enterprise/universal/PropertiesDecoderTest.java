/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.enterprise.universal;

import java.util.HashMap;
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
public class PropertiesDecoderTest {

    public PropertiesDecoderTest() {
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
     * Test of unflatten method, of class PropertiesDecoder.
     */
    @Test
    public void testUnflatten() {
        System.out.println("*****   Unflatten Test   ******* ");
        String s = "foo=goo:xyz:hoo=ioo";
        Map<String, String> result = PropertiesDecoder.unflatten(s);
        Map<String, String> expResult = new HashMap<String,String>();
        expResult.put("foo", "goo");
        expResult.put("xyz", null);
        expResult.put("hoo", "ioo");
        assertEquals(expResult, result);

        s = "foo=goo:xyz:hoo=ioo:qqq=::::z:";
        result = PropertiesDecoder.unflatten(s);
        expResult.put("qqq", null);
        expResult.put("z", null);
        assertEquals(expResult, result);

        s = "foo=goo:xyz:hoo=ioo:qqq=::::z:foo=qbert:a=b=c=d";
        result = PropertiesDecoder.unflatten(s);
        expResult.put("foo", "qbert");
        expResult.put("a", "b=c=d");
        assertEquals(expResult, result);
        System.out.println(result.toString());

        System.out.println("*****   Unflatten Test   ******* ");
    }
}
