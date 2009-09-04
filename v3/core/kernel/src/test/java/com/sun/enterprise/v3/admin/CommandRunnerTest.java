/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008-2009 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.enterprise.v3.admin;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;
import java.util.Properties;
import java.util.List;
import org.glassfish.api.Param;

import java.lang.reflect.AnnotatedElement;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandModel;
import org.glassfish.config.support.CommandModelImpl;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.ComponentException;

/**
 * junit test to test CommandRunner class
 */
public class CommandRunnerTest {
    private CommandRunnerImpl cr = null;

    @Test
    public void getPropertiesValueTest() {

        Properties props = new Properties();
        props.put("foo", "bar");
        props.put("hellO", "world");
        props.put("one", "two");
        props.put("thrEE", "Four");
        props.put("FivE", "six");
        props.put("sIx", "seVen");
        props.put("eiGHT", "niNe");
        String value = cr.getPropertiesValue(props, "foo", false);
        assertEquals("value is bar", "bar", value);
        value = cr.getPropertiesValue(props, "hello", true);
        assertEquals("value is world", "world", value);        
        value = cr.getPropertiesValue(props, "onE", true);
        assertEquals("value is two", "two", value);
        value = cr.getPropertiesValue(props, "three", true);
        assertEquals("value is four", "Four", value);                
        value = cr.getPropertiesValue(props, "five", false);
        assertEquals("value is null", null, value);
        value = cr.getPropertiesValue(props, "six", true);
        assertEquals("value is SeVen", "seVen", value);
        value = cr.getPropertiesValue(props, "eight", true);
        assertEquals("value is niNe", "niNe", value);
        value = cr.getPropertiesValue(props, "none", true);
        assertEquals("value is null", null, value);        
    }

    @Test
    public void convertStringToPropertiesTest() {
        String propsStr = "prop1=valA:prop2=valB:prop3=valC";
        Properties propsExpected = new Properties();
        propsExpected.put("prop1", "valA");
        propsExpected.put("prop2", "valB");
        propsExpected.put("prop3", "valC");
        Properties propsActual = cr.convertStringToProperties(propsStr, ':');
        assertEquals(propsExpected, propsActual);
    }
    
    @Test
    public void parsePropertiesEscapeCharTest() {
        String propsStr = "connectionAttributes=\\;create\\\\\\=true";
        Properties propsExpected = new Properties();
        propsExpected.put("connectionAttributes", ";create\\=true");
        Properties propsActual = null;
        propsActual = cr.convertStringToProperties(propsStr, ':');
        assertEquals(propsExpected, propsActual);
    }
    
    @Test
    public void parsePropertiesEscapeCharTest2() {
        String propsStr = "connectionAttributes=;create\\=true";
        Properties propsExpected = new Properties();
        propsExpected.put("connectionAttributes", ";create=true");
        Properties propsActual = null;
        propsActual = cr.convertStringToProperties(propsStr, ':');
        assertEquals(propsExpected, propsActual);
    }
    
    @Test
    public void convertStringToObjectTest() throws Exception {
        DummyCommand dc = new DummyCommand();
        Class<?> cl = dc.getClass();
        AnnotatedElement target = (AnnotatedElement)cl.getDeclaredField("foo");
        String paramValueStr = "prop1=valA:prop2=valB:prop3=valC";
        Object paramValActual = cr.convertStringToObject(target, String.class, paramValueStr);
        Object paramValExpected =  "prop1=valA:prop2=valB:prop3=valC";
        assertEquals("String type", paramValExpected, paramValActual);
  
        target = (AnnotatedElement)cl.getDeclaredField("prop");
        paramValActual = cr.convertStringToObject(target, Properties.class, paramValueStr);
        paramValExpected = new Properties();        
        ((Properties)paramValExpected).put("prop1", "valA");
        ((Properties)paramValExpected).put("prop2", "valB");
        ((Properties)paramValExpected).put("prop3", "valC");
        assertEquals("Properties type", paramValExpected, paramValActual);

        paramValueStr = "server1:server2:server3";
        target = (AnnotatedElement)cl.getDeclaredField("lstr");
        paramValActual = cr.convertStringToObject(target, List.class, paramValueStr);
        List<String> paramValueList = new java.util.ArrayList();
        paramValueList.add("server1");
        paramValueList.add("server2");
        paramValueList.add("server3");
        assertEquals("List type", paramValueList, paramValActual);

        paramValueStr = "server1,server2,server3";
        target = (AnnotatedElement)cl.getDeclaredField("astr");
        paramValActual = cr.convertStringToObject(target, (new String[]{}).getClass(),
                                                  paramValueStr);
        String[] strArray = new String[3];
        strArray[0] = "server1";
        strArray[1] = "server2";
        strArray[2] = "server3";
        assertEquals("String Array type", strArray, (String[])paramValActual);
    }

    @Test
    public void getParamValueStringTest() {
        try {
            DummyCommand dc = new DummyCommand();
            Class<?> cl = dc.getClass();
            AnnotatedElement ae = (AnnotatedElement)cl.getDeclaredField("foo");
            Param param = ae.getAnnotation(Param.class);
            Properties props = new Properties();
            props.put("foo", "true");
            String val = cr.getParamValueString(props, param, ae);
            assertEquals("val should be true", "true", val);

            ae = (AnnotatedElement)cl.getDeclaredField("bar");
            param = ae.getAnnotation(Param.class);
            val = cr.getParamValueString(props, param, ae);
            assertEquals("val should be false", "false", val);

            ae = (AnnotatedElement)cl.getDeclaredField("hello");
            param = ae.getAnnotation(Param.class);
            val = cr.getParamValueString(props, param, ae);
            assertEquals("val should be null", null, val);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    @Test
    public void getParamFieldTest() {
        try {
            DummyCommand dc = new DummyCommand();
            Class<?> cl = dc.getClass();
            AnnotatedElement ae = (AnnotatedElement)cl.getDeclaredField("hello");
            Object obj = cr.getParamField(dc, ae);
            assertEquals("obj should be world", "world", (String)obj);
            ae = (AnnotatedElement)cl.getDeclaredField("prop");
            obj = cr.getParamField(dc, ae);
            assertEquals("obj should be null", null, obj);            
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Test
    public void convertStringToListTest() {
        String listStr = "server1\\:server2:\\\\server3:server4";
        List<String> listExpected = new java.util.ArrayList();
        listExpected.add("server1:server2");
        listExpected.add("\\server3");
        listExpected.add("server4");
        List<String> listActual = cr.convertStringToList(listStr, ':');
        assertEquals(listExpected, listActual);
    }

    @Test
    public void convertStringToStringArrayTest() {
        String strArray = "server1\\,server2,\\\\server3,server4";
        String[] strArrayExpected = new String[3];
        strArrayExpected[0]="server1,server2";
        strArrayExpected[1]="\\server3";
        strArrayExpected[2]="server4";
        String[] strArrayActual = cr.convertStringToStringArray(strArray, ',');
        assertEquals(strArrayExpected, strArrayActual);
    }

    @Test
    public void getUsageTextTest() {
        String expectedUsageText = "Usage: dummy-admin --foo=foo [--bar=false] --hello=there world ";
        DummyAdminCommand dac = new DummyAdminCommand();
        CommandModel model = new CommandModelImpl(DummyAdminCommand.class);
        String actualUsageText = cr.getUsageText(dac, model);
        assertEquals(expectedUsageText, actualUsageText);
    }

    @Test
    public void validateParametersTest() {
        Properties props = new Properties();
        props.put("foo", "bar");
        props.put("hello", "world");
        props.put("one", "two");
        try {
            cr.validateParameters(new CommandModelImpl(DummyAdminCommand.class), props);
        }
        catch (ComponentException ce) {
            String expectedMessage = " Invalid option: one";
            assertEquals(expectedMessage, ce.getMessage());
        }
    }

    @Test
    public void skipValidationTest() {
        DummyAdminCommand dac = new DummyAdminCommand();
        assertFalse(cr.skipValidation(dac));
        SkipValidationCommand svc = new SkipValidationCommand();
        assertTrue(cr.skipValidation(svc));        
    }
    
    @Before
    public void setup() {
        cr = new CommandRunnerImpl();
    }

        //mock-up DummyCommand object
    public class DummyCommand {
        @Param(name="foo")
        String foo;
        @Param(name="bar", defaultValue="false")
        String bar;
        @Param
        String hello="world";
        @Param(name="prop", separator=':')
        Properties prop;
        @Param(name="lstr", separator=':')
        List<String> lstr;
        @Param(name="astr")
        String[] astr;
        
        public void execute(AdminCommandContext context) {}
    }

        //mock-up DummyAdminCommand object
    @Service(name="dummy-admin")
    public class DummyAdminCommand implements AdminCommand {
        @Param(optional=false)
        String foo;

        @Param(name="bar", defaultValue="false", optional=true)
        String foobar;

        @Param(optional=false, defaultValue="there")
        String hello;

        @Param(optional=false, primary=true)
        String world;
            
        public void execute(AdminCommandContext context) {}
    }

        //mock-up SkipValidationCommand
    public class SkipValidationCommand implements AdminCommand {
        boolean skipParamValidation=true;
        public void execute(AdminCommandContext context) {}        
    }
    

}
