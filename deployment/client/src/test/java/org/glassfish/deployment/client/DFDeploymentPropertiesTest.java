/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.deployment.client;

import java.util.Map;
import java.util.Properties;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Tim
 */
public class DFDeploymentPropertiesTest {

    public DFDeploymentPropertiesTest() {
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
     * Test of setProperties method, of class DFDeploymentProperties.
     */
    @Test
    public void testSetProperties() {
        Properties props = new Properties();
        props.setProperty("keepSessions", "true");

        DFDeploymentProperties instance = new DFDeploymentProperties();
        instance.setProperties(props);

        String storedProps = (String) instance.get(DFDeploymentProperties.PROPERTIES);
        assertEquals(storedProps, "keepSessions=true");
    }

    /**
     * Test of getProperties method, of class DFDeploymentProperties.
     */
    @Test
    public void testGetProperties() {
        DFDeploymentProperties instance = new DFDeploymentProperties();
        instance.put(DFDeploymentProperties.PROPERTIES, "keepSessions=true");
        Properties expResult = new Properties();
        expResult.setProperty("keepSessions", "true");

        Properties result = instance.getProperties();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetMultipleProp() {
        DFDeploymentProperties instance = new DFDeploymentProperties();
        instance.put(DFDeploymentProperties.PROPERTIES, "keepSessions=true:otherSetting=test");

        Properties expResult = new Properties();
        expResult.setProperty("keepSessions", "true");
        expResult.setProperty("otherSetting", "test");

        Properties result = instance.getProperties();
        assertEquals(expResult, result);
    }

    @Test
    public void testSetMultipleProp() {
        Properties props = new Properties();
        props.setProperty("keepSessions", "true");
        props.setProperty("otherSetting", "test");

        DFDeploymentProperties instance = new DFDeploymentProperties();
        instance.setProperties(props);

        String internalValue = (String) instance.get(DFDeploymentProperties.PROPERTIES);
        String expResult1 = "keepSessions=true:otherSetting=test";
        String expResult2 = "otherSetting=test:keepSessions=true";
        assertTrue(internalValue.equals(expResult1) || internalValue.equals(expResult2));



    }

}