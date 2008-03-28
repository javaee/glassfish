package com.sun.enterprise.configapi.tests;

import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.ConfigBean;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.glassfish.tests.utils.Utils;
import com.sun.enterprise.config.serverbeans.HttpService;
import com.sun.enterprise.config.serverbeans.AccessLog;
import com.sun.enterprise.config.serverbeans.HttpListener;

import java.util.Map;
import java.util.HashMap;

/**
 * User: Jerome Dochez
 * Date: Mar 27, 2008
 * Time: 3:18:57 PM
 */
public class DirectRemovalTest extends ConfigPersistence {

    Habitat habitat = Utils.getNewHabitat(this);

    /**
     * Returns the file name without the .xml extension to load the test configuration
     * from. By default, it's the name of the TestClass.
     *
     * @return the configuration file name
     */
    public String getFileName() {
        return "DomainTest";
    }

    @Override
    public Habitat getHabitat() {
        return habitat;
    }

    public void doTest() throws TransactionFailure {

        HttpService service = habitat.getComponent(HttpService.class);

        ConfigBean serviceBean = (ConfigBean) ConfigBean.unwrap(service);

        for (HttpListener listener : service.getHttpListener()) {
            if (listener.getId().endsWith("http-listener-1")) {
                ConfigSupport.deleteChild(serviceBean, (ConfigBean) ConfigBean.unwrap(listener));
                break;
            }
        }
    }

    public boolean assertResult(String s) {
        // we must not find it
        return s.indexOf("id=\"http-listener-1\"")==-1;
    }
}
