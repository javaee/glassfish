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
        props.setProperty("foo", "bar");

        DFDeploymentProperties instance = new DFDeploymentProperties();
        instance.setProperties(props);

        String storedProps = (String) instance.get(DFDeploymentProperties.PROPERTY);
        assertEquals(storedProps, "keepSessions=true:foo=bar");
    }

    /**
     * Test of getProperties method, of class DFDeploymentProperties.
     */
    @Test
    public void testGetProperties() {
        DFDeploymentProperties instance = new DFDeploymentProperties();
        instance.put(DFDeploymentProperties.PROPERTY, "keepSessions=true:foo=bar");
        Properties expResult = new Properties();
        expResult.setProperty("keepSessions", "true");
        expResult.setProperty("foo", "bar");

        Properties result = instance.getProperties();
        assertEquals(expResult, result);
    }


}
