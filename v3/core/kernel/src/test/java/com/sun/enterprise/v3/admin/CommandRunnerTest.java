package com.sun.enterprise.v3.admin;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;
import com.sun.enterprise.v3.admin.CommandRunner;
import java.util.Properties;


/**
 * junit test to test CommandRunner class
 */
public class CommandRunnerTest {
    private CommandRunner cr = null;

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
    public void parsePropertiesTest() {
        String propsStr = "prop1=valA:prop2=valB:prop3=valC";
        Properties propsExpected = new Properties();
        propsExpected.put("prop1", "valA");
        propsExpected.put("prop2", "valB");
        propsExpected.put("prop3", "valC");
        Properties propsActual = cr.parseProperties(propsStr);
        assertEquals(propsExpected, propsActual);
    }
    
    @Test
    public void parsePropertiesEscapeCharTest() {
        String propsStr = "connectionAttributes=\\;create\\\\=true";
        Properties propsExpected = new Properties();
        propsExpected.put("connectionAttributes", ";create\\=true");
        Properties propsActual = null;
        propsActual = cr.parseProperties(propsStr);
        assertEquals(propsExpected, propsActual);
    }
    
    @Test
    public void getParamValueTest() throws Exception {
        String paramValueStr = "prop1=valA:prop2=valB:prop3=valC";
        Object paramValActual = cr.getParamValue(String.class, paramValueStr);
        Object paramValExpected =  "prop1=valA:prop2=valB:prop3=valC";
        assertEquals("String type", paramValExpected, paramValActual);
  
        paramValActual = cr.getParamValue(Properties.class, paramValueStr);
        paramValExpected = new Properties();        
        ((Properties)paramValExpected).put("prop1", "valA");
        ((Properties)paramValExpected).put("prop2", "valB");
        ((Properties)paramValExpected).put("prop3", "valC");
        assertEquals("Properties type", paramValExpected, paramValActual);
    }
    
    @Before
    public void setup() {
        cr = new CommandRunner();
    }
}
