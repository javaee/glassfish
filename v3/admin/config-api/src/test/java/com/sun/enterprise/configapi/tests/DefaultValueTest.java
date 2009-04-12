package com.sun.enterprise.configapi.tests;

import com.sun.grizzly.config.dom.NetworkListener;
import com.sun.grizzly.config.dom.NetworkListeners;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.Dom;

/**
 * Test attribute and raw attribute access *
 */
public class DefaultValueTest extends ConfigApiTest {

    NetworkListener listener;

    public String getFileName() {
        return "DomainTest";
    }

    @Before
    public void setup() {
        NetworkListeners httpService = getHabitat().getComponent(NetworkListeners.class);
        listener = httpService.getNetworkListener().get(0);

    }

    @Test
    public void rawAttributeTest() throws NoSuchMethodException {

        String address = listener.getAddress();

        Dom raw = Dom.unwrap(listener);
        Attribute attr = raw.getProxyType().getMethod("getAddress").getAnnotation(Attribute.class);
        assertEquals(attr.defaultValue(), address);
        
        assertEquals(raw.attribute("address"), address);
        assertEquals(raw.rawAttribute("address"), address);

    }

}
