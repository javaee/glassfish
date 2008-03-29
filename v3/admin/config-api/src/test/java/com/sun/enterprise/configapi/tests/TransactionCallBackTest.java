package com.sun.enterprise.configapi.tests;

import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.config.*;
import org.glassfish.tests.utils.Utils;
import com.sun.enterprise.config.serverbeans.HttpService;
import com.sun.enterprise.config.serverbeans.AccessLog;
import com.sun.enterprise.config.serverbeans.Property;
import com.sun.enterprise.config.serverbeans.HttpListener;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.beans.PropertyVetoException;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/**
 * User: Jerome Dochez
 * Date: Mar 28, 2008
 * Time: 4:23:31 PM
 */
public class TransactionCallBackTest extends ConfigPersistence {

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
        Map<String, String> configChanges = new HashMap<String, String>();
        configChanges.put("id", "funky-listener");

        ConfigSupport.createAndSet(serviceBean, HttpListener.class, configChanges,
                new ConfigSupport.TransactionCallBack<WriteableView>() {
                    public void performOn(WriteableView param) throws TransactionFailure {
                        try {
                            // if you know the type...
                            HttpListener listener = param.getProxy(HttpListener.class);
                            Property prop = param.allocateProxy(Property.class);
                            prop.setName("Julien");
                            prop.setValue("Le petit Clown");
                            listener.getProperty().add(prop);

                            // if you don't know the type
                            Method m = null;
                            try {
                                m = param.getProxyType().getMethod("getProperty");
                            } catch (NoSuchMethodException e) {
                                throw new TransactionFailure("Cannot find getProperty method", e);
                            }
                            Property prop2 = param.allocateProxy(Property.class);
                            prop2.setName("Aleksey");
                            prop2.setValue("Le petit blond");
                            try {
                                List list = (List) m.invoke(param.getProxy(param.getProxyType()));
                                list.add(prop2);
                            } catch (IllegalAccessException e) {
                                throw new TransactionFailure("Cannot call getProperty method", e);
                            } catch (InvocationTargetException e) {
                                throw new TransactionFailure("Cannot call getProperty method", e);
                            }
                        } catch(PropertyVetoException e) {
                            throw new TransactionFailure("Cannot add property to listener", e);
                        }
                        
                    }
                });
    }

    public boolean assertResult(String s) {
        return s.indexOf("Aleksey")!=-1;
    }    
}
