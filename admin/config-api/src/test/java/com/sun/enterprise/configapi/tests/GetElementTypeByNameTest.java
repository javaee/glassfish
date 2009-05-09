package com.sun.enterprise.configapi.tests;

import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.ConfigSupport;
import com.sun.enterprise.config.serverbeans.HttpService;

/**
 * Test the getElementTypeByName ConfigSupport API
 *
 * @Author Jerome Dochez 
 */
public class GetElementTypeByNameTest extends ConfigApiTest {

    public String getFileName() {
        return "DomainTest";
    }


    @Test
    public void testAppRoot() {
        HttpService domain = getHabitat().getComponent(HttpService.class);
        Class<? extends ConfigBeanProxy> elementType = null;
        try {
            elementType = ConfigSupport.getElementTypeByName(domain, "http-listener");
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertNotNull(elementType);
        assertTrue(elementType.getName().endsWith("HttpListener"));
    }
}