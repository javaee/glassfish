package com.sun.enterprise.configapi.tests;

import com.sun.enterprise.config.serverbeans.HttpListener;
import com.sun.enterprise.config.serverbeans.HttpService;

import org.junit.Test;
import static org.junit.Assert.*;
import org.jvnet.hk2.config.ConfigBeanProxy;

/**
 * test the getParentAPI.
 *
 * @author Jerome Dochez
 */
public class ParentTest extends ConfigApiTest {


    public String getFileName() {
        return "DomainTest";
    }

    @Test
    public void parents() {

        HttpService service = getHabitat().getComponent(HttpService.class);
        assertNotNull(service);
        HttpListener listener = service.getHttpListener().get(0);
        assertNotNull(listener);

        ConfigBeanProxy parent = service.getParent();
        assertNotNull(parent);

        HttpService myService = listener.getParent(HttpService.class);
        assertNotNull(myService);
        assertNotNull(myService.getHttpListener().get(0).getId());
    }
}
