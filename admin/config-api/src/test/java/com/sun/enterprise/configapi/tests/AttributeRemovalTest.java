package com.sun.enterprise.configapi.tests;

import org.junit.Test;
import org.junit.Before;
import org.jvnet.hk2.config.*;
import org.glassfish.tests.utils.Utils;
import com.sun.enterprise.config.serverbeans.HttpService;
import com.sun.enterprise.config.serverbeans.VirtualServer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.List;

/**
 * User: Jerome Dochez
 * Date: Jun 25, 2008
 * Time: 8:03:41 AM
 */
public class AttributeRemovalTest extends ConfigApiTest {

    public String getFileName() {
        return "DomainTest";
    }

    @Test
    public void removeAttributeTest() throws TransactionFailure {
        HttpService httpService = Utils.getNewHabitat(this).getComponent(HttpService.class);
        VirtualServer vs = httpService.getVirtualServerByName("server");
        ConfigSupport.apply(new SingleConfigCode<VirtualServer>() {

            public Object run(VirtualServer param) throws PropertyVetoException, TransactionFailure {
                param.setDefaultWebModule("/context/bar");
                return null;
            }
        }, vs);

        // ensure it's here
        org.junit.Assert.assertNotNull(vs.getDefaultWebModule());

        ConfigSupport.apply(new SingleConfigCode<VirtualServer>() {

            public Object run(VirtualServer param) throws PropertyVetoException, TransactionFailure {
                param.setDefaultWebModule(null);
                return null;
            }
        }, vs);

        // ensure it's removed
        org.junit.Assert.assertNull(vs.getDefaultWebModule());
    }

}
