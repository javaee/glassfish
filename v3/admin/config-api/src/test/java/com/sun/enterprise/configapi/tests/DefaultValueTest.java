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
@Ignore
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

        String family = listener.getEnabled();

        Dom raw = Dom.unwrap(listener);
        Attribute attr = raw.getProxyType().getMethod("getEnabled").getAnnotation(Attribute.class);
        assertEquals(attr.defaultValue(), family);

        assertEquals(raw.attribute("enabled"), family);
        assertEquals(raw.rawAttribute("enabled"), family);

    }

}
