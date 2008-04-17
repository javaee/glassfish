/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.enterprise.universal;

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
public class StringUtilsTest {

    public StringUtilsTest() {
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
     * Test of removeEnclosingQuotes method, of class StringUtils.
     */
    @Test
    public void removeEnclosingQuotes() {
        System.out.println("removeEnclosingQuotes");
        String a = "\"hello\"";
        String b = "'hello'";
        String c = "\"hello'";
        String d = "\"\"hello";
        
        assertEquals(StringUtils.removeEnclosingQuotes(a), "hello");
        assertEquals(StringUtils.removeEnclosingQuotes(b), "hello");
        assertEquals(StringUtils.removeEnclosingQuotes(c), "\"hello\'");
        assertEquals(StringUtils.removeEnclosingQuotes(d), "\"\"hello");
        assertEquals(StringUtils.removeEnclosingQuotes("\""), "\"");
        assertEquals(StringUtils.removeEnclosingQuotes("'"), "'");
        assertEquals(StringUtils.removeEnclosingQuotes("''"), "");
        assertEquals(StringUtils.removeEnclosingQuotes("\"\""), "");
        assertEquals(StringUtils.removeEnclosingQuotes(""), "");
        assertNull(StringUtils.removeEnclosingQuotes(null));
        
    }

}