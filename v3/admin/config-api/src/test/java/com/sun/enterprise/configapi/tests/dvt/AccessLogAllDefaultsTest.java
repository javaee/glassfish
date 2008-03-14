/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.enterprise.configapi.tests.dvt;

import com.sun.enterprise.config.serverbeans.AccessLog;
import com.sun.enterprise.configapi.tests.ConfigApiTest;
import java.beans.PropertyVetoException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author kedar
 */
public class AccessLogAllDefaultsTest extends ConfigApiTest {

    private AccessLog al = null;
            
    public AccessLogAllDefaultsTest() {
    }
    
    @Override
    public String getFileName() {
        return ("AccessLogAllDefaultsTest"); //this is the xml to load
    }
    
    @Before
    public void setUp() {
        al = super.getHabitat().getComponent(AccessLog.class);
    }

    @After
    public void tearDown() {
        al = null;
    }
    @Test 
    public void testAllDefaults() {
        assertEquals("%client.name% %auth-user-name% %datetime% %request% %status% %response.length%",
                al.getFormat());
        assertEquals("true", al.getRotationEnabled());
        assertEquals("1440", al.getRotationIntervalInMinutes());
        assertEquals("time", al.getRotationPolicy());
        assertEquals("yyyyMMdd-HH'h'mm'm'ss's'", al.getRotationSuffix());
    }
}