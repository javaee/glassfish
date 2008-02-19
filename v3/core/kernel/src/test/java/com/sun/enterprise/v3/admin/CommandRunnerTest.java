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

    @Before
    public void setup() {
        cr = new CommandRunner();
    }
}
