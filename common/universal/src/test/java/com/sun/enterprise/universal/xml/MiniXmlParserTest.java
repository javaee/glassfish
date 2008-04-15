/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.enterprise.universal.xml;

import java.io.File;
import java.net.*;
import java.util.*;
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
        hasProfiler = new File(
           MiniXmlParserTest.class.getClassLoader().getResource("hasprofiler.xml").getPath());
        adminport = new File(
           MiniXmlParserTest.class.getClassLoader().getResource("adminport.xml").getPath());
        adminport2 = new File(
           MiniXmlParserTest.class.getClassLoader().getResource("adminport2.xml").getPath());
        assertTrue(wrongOrder.exists());
        assertTrue(rightOrder.exists());
        assertTrue(noconfig.exists());
        assertTrue(hasProfiler.exists());
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
    /*
     * Positive test case -- make sure profiler is parsed correctly
     * here is the piece of xml it will be parsing:
     * 
            <profiler classpath="/profiler/class/path" enabled="true" name="MyProfiler" native-library-path="/bin">
                <jvm-options>-Dprofiler3=foo3</jvm-options>
                <jvm-options>-Dprofiler2=foo2</jvm-options>
                <jvm-options>-Dprofiler1=foof</jvm-options>
            </profiler>
     * 
     */
    @Test
    public void test8() {
        try {
            MiniXmlParser instance = new MiniXmlParser(hasProfiler, "server");
            Map<String, String> config = instance.getProfilerConfig();
            List<String> jvm = instance.getProfilerJvmOptions();
            Map<String,String> sysProps = instance.getProfilerSystemProperties();
            assertEquals(jvm.size(), 3);
            assertEquals(jvm.get(0), "-Dprofiler3=foo3");
            assertEquals(jvm.get(1), "-Dprofiler2=foo2");
            assertEquals(jvm.get(2), "-Dprofiler1=foof");
            assertNotNull(config);
            assertEquals(config.size(), 4);
            assertEquals(config.get("classpath"), "/profiler/class/path");
            assertEquals(config.get("enabled"), "true");
            assertEquals(config.get("name"), "MyProfiler");
            assertEquals(config.get("native-library-path"), "/bin");
            assertEquals(sysProps.size(), 2);
            assertEquals(sysProps.get("name1"), "value1");
            assertEquals(sysProps.get("name2"), "value2");
            
        }
        catch (MiniXmlParserException ex) {
            Logger.getLogger(MiniXmlParserTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
        
     /*
     * Exercise the parsing of asadmin virtual server, http-listener and port numbers
     * this one tests for TWO listeners
     */
    @Test
    public void test9() {
        try {
            MiniXmlParser instance = new MiniXmlParser(adminport2, "server");
            Set<Integer> ports = instance.getAdminPorts();
            assertEquals(ports.size(), 2);
            assertTrue(ports.contains(new Integer(3333)));
            assertTrue(ports.contains(new Integer(4444)));
        }
        catch (MiniXmlParserException ex) {
            Logger.getLogger(MiniXmlParserTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
     /*
     * Exercise the parsing of asadmin virtual server, http-listener and port numbers
     * this one tests for ONE listener
     */
    @Test
    public void test10() {
        try {
            MiniXmlParser instance = new MiniXmlParser(adminport, "server");
            Set<Integer> ports = instance.getAdminPorts();
            assertEquals(ports.size(), 1);
            assertTrue(ports.contains(new Integer(3333)));
        }
        catch (MiniXmlParserException ex) {
            Logger.getLogger(MiniXmlParserTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
     /*
     * Verify the right logfile name is returned.
     */
    @Test
    public void test11() throws MiniXmlParserException {
            MiniXmlParser instance = new MiniXmlParser(adminport, "server");
            assertEquals(instance.getLogFilename(), "${com.sun.aas.instanceRoot}/logs/server.log");
    }
    
    
    
    private static File hasProfiler;
    private static File wrongOrder;
    private static File rightOrder;
    private static File noconfig;
    private static File adminport;
    private static File adminport2;
}

