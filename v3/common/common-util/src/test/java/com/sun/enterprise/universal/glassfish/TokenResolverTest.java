/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.enterprise.universal.glassfish;

import java.util.HashMap;
import java.util.List;
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
public class TokenResolverTest {

    public TokenResolverTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        testMap = new HashMap<String,String>();
        testMap.put("name1", "value1");
        testMap.put("name2", "value2");
        testMap.put("name3", "value3");
        testMap.put("name4", "value4");
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of resolve method, of class TokenResolver.
     */
    @Test
    public void testResolve_Map() {
        System.out.println("Testing token-resolve of map");
        Map<String,String> map2 = new HashMap<String,String>();

        map2.put("foo", "${name1}");
        map2.put("foo2", "${name111}");
        map2.put("zzz${name3}zzz", "zzz");
        map2.put("qqq${name2}qqq", "${name4}");

        TokenResolver instance = new TokenResolver(testMap);
        instance.resolve(map2);
        assertEquals(map2.get("foo"), "value1");
        assertEquals(map2.get("foo2"), "${name111}");
        // this entry should be gone:
        assertNull(map2.get("qqq${name2}qqq"));

        // and replaced with this:
        assertEquals(map2.get("qqqvalue2qqq"), "value4");

        assertEquals(map2.get("zzzvalue3zzz"), "zzz");
        
        instance.resolve(map2);
    }
    /**
     * Test of resolve method, of class TokenResolver.
     */
    @Test
    public void testResolve_List() {
        System.out.println("resolve");
        List<String> list = null;
        TokenResolver instance = new TokenResolver(testMap);
        //instance.resolve(list);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of resolve method, of class TokenResolver.
     */
    @Test
    public void testResolve_String() {
        System.out.println("resolve String Test");
        TokenResolver instance = new TokenResolver(testMap);
        String expResult = "xyzvalue1xyz";
        String result = instance.resolve("xyz${name1}xyz");
        assertEquals(expResult, result);

        expResult = "xyz$value1xyz";
        result = instance.resolve("xyz$${name1}xyz");
        assertEquals(expResult, result);

        expResult = "xyzvalue1}xyz";
        result = instance.resolve("xyz${name1}}xyz");
        assertEquals(expResult, result);

        expResult = "xyzvalue4xyz";
        result = instance.resolve("xyz${name4}xyz");
        assertEquals(expResult, result);

        expResult = "xyz${name5}xyz";
        result = instance.resolve("xyz${name5}xyz");
        assertEquals(expResult, result);
    }

    private Map<String,String> testMap;
}