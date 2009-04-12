package com.sun.enterprise.configapi.tests;

import com.sun.grizzly.config.dom.NetworkListener;
import com.sun.grizzly.config.dom.NetworkListeners;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
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

        NetworkListeners service = getHabitat().getComponent(NetworkListeners.class);
        assertNotNull(service);
        NetworkListener listener = service.getNetworkListener().get(0);
        assertNotNull(listener);

        ConfigBeanProxy parent = service.getParent();
        assertNotNull(parent);

        NetworkListeners myService = listener.getParent(NetworkListeners.class);
        assertNotNull(myService);
        assertNotNull(myService.getNetworkListener().get(0).getName());
    }
}
