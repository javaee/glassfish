/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.enterprise.universal.xml;

import java.io.File;
import java.net.*;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
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
public class MiniXmlParserTest {

    public MiniXmlParserTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        wrongOrder = new File(
           MiniXmlParserTest.class.getClassLoader().getResource("wrongorder.xml").getPath());
        rightOrder = new File(
           MiniXmlParserTest.class.getClassLoader().getResource("rightorder.xml").getPath());
        noconfig = new File(
           MiniXmlParserTest.class.getClassLoader().getResource("noconfig.xml").getPath());
        assertTrue(wrongOrder.exists());
        assertTrue(rightOrder.exists());
        assertTrue(noconfig.exists());

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
     * Positive Test Case where servers appears after configs
     * @throws com.sun.enterprise.universal.xml.MiniXmlParserException
     */
    @Test
    public void test1() {
        try {
            MiniXmlParser instance = new MiniXmlParser(wrongOrder, "server");
            Map<String, String> javaConfig = instance.getJavaConfig();
            List<String> jvmOptions = instance.getJvmOptions();
            assertEquals(jvmOptions.get(0), "JVMOPTION1");
            assertEquals(jvmOptions.get(1), "JVMOPTION2");
            assertEquals(javaConfig.get("test"), "test");
        }
        catch (MiniXmlParserException ex) {
            Logger.getLogger(MiniXmlParserTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    /**
     * Test that the correct Exception is thrown for a null xml File
     * @throws com.sun.enterprise.universal.xml.MiniXmlParserException
     */
    @Test( expected=MiniXmlParserException.class)
    public void test2() throws MiniXmlParserException {
            MiniXmlParser instance = new MiniXmlParser(null, "server");
        
    }
    /**
     * Test that the correct Exception is thrown for a non-existing xml File
     * @throws com.sun.enterprise.universal.xml.MiniXmlParserException
     */
    @Test( expected=MiniXmlParserException.class)
    public void test3() throws MiniXmlParserException {
            MiniXmlParser instance = new MiniXmlParser(new File("."), "server");
        
    }
    /**
     * Positive Test Case where configs appears after servers
     * @throws com.sun.enterprise.universal.xml.MiniXmlParserException
     */
    @Test
    public void test4() {
        try {
            MiniXmlParser instance = new MiniXmlParser(rightOrder, "server");
            Map<String, String> javaConfig = instance.getJavaConfig();
            List<String> jvmOptions = instance.getJvmOptions();
            assertEquals(jvmOptions.get(0), "JVMOPTION1");
            assertEquals(jvmOptions.get(1), "JVMOPTION2");
            assertEquals(javaConfig.get("test"), "test");
        }
        catch (MiniXmlParserException ex) {
            Logger.getLogger(MiniXmlParserTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    /**
     * Negative Test Case where there is no "server-config"
     * @throws com.sun.enterprise.universal.xml.MiniXmlParserException
     */
    @Test( expected=MiniXmlParserException.class)
    public void test5() throws MiniXmlParserException {
         try {
             MiniXmlParser instance = new MiniXmlParser(noconfig, "server");
         }
        catch (MiniXmlParserException ex) {
            System.out.println("This exception is expected.  Here it is: " + ex);
            throw ex;
        }        
    }
    /*
     * Positive test cases -- look at <system-property>
     */
    @Test
    public void test6() {
        try {
            MiniXmlParser instance = new MiniXmlParser(rightOrder, "server");
            Map<String, String> javaConfig = instance.getJavaConfig();
            List<String> jvmOptions = instance.getJvmOptions();
            Map<String,String> sysProps = instance.getSystemProperties();
            assertEquals(jvmOptions.get(0), "JVMOPTION1");
            assertEquals(jvmOptions.get(1), "JVMOPTION2");
            assertEquals(javaConfig.get("test"), "test");
            assertEquals(sysProps.get("beforeJavaConfig"), "true");
            assertEquals(sysProps.get("afterJavaConfig"), "true");
            assertNull(sysProps.get("foo"));
            assertEquals(sysProps.size(), 3);
        }
        catch (MiniXmlParserException ex) {
            Logger.getLogger(MiniXmlParserTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    /*
     * Positive test case -- make sure system-property in <server> overrides the one in <config>
     */
    @Test
    public void test7() {
        try {
            MiniXmlParser instance = new MiniXmlParser(rightOrder, "server");
            Map<String, String> javaConfig = instance.getJavaConfig();
            Map<String,String> sysProps = instance.getSystemProperties();
            assertEquals(sysProps.get("test-prop"), "valueFromServer");
        }
        catch (MiniXmlParserException ex) {
            Logger.getLogger(MiniXmlParserTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    private static File wrongOrder;
    private static File rightOrder;
    private static File noconfig;
}

