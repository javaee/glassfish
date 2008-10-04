package com.sun.enterprise.configapi.tests;

import org.junit.Test;
import org.junit.Before;
import org.junit.Ignore;
import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.Dom;
import static org.junit.Assert.*;
import com.sun.enterprise.config.serverbeans.HttpService;
import com.sun.enterprise.config.serverbeans.HttpListener;

/**
 * Test attribute and raw attribute access *
 */
public class DefaultValueTest extends ConfigApiTest {

    HttpListener listener;

    public String getFileName() {
        return "DomainTest";
    }

    @Before
    public void setup() {

        HttpService httpService = getHabitat().getComponent(HttpService.class);
        listener = httpService.getHttpListener().get(0);

    }

    @Test
    public void rawAttributeTest() throws NoSuchMethodException {

        String family = listener.getFamily();

        Dom raw = Dom.unwrap(listener);
        Attribute attr = raw.getProxyType().getMethod("getFamily").getAnnotation(Attribute.class);
        assertEquals(attr.defaultValue(), family);
        
        assertEquals(raw.attribute("family"), family);
        assertEquals(raw.rawAttribute("family"), family);

    }

}
