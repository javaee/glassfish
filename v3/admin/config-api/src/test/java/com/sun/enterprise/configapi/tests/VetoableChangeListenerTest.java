package com.sun.enterprise.configapi.tests;

import org.jvnet.hk2.config.*;
import org.jvnet.hk2.config.types.*;
import org.jvnet.hk2.component.*;
import org.junit.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.glassfish.tests.utils.*;
import com.sun.enterprise.config.serverbeans.*;

import java.beans.*;

/**
 * This test registers an vetoable change listener on a config bean and vetoes
 * any change on that object.
 *
 * @author Jerome Dochez
 */
public class VetoableChangeListenerTest extends ConfigApiTest implements VetoableChangeListener {

    Habitat habitat;
    boolean result = false;

    public String getFileName() {
        return "DomainTest";
    }

    @Before
    public void setup() {
        habitat = Utils.getNewHabitat(this);
    }

    @Test
    public void propertyChangeEventReceptionTest() throws TransactionFailure {

        HttpService httpService = habitat.getComponent(HttpService.class);
        assertNotNull(httpService);

       // let's find a acceptable target.
        VirtualServer target =null;
        for (VirtualServer vs : httpService.getVirtualServer()) {
            if (!vs.getProperty().isEmpty()) {
                target = vs;
                break;
            }
        }

        assertNotNull(target);

        ((ConfigBean) ConfigSupport.getImpl(target)).getOptionalFeature(ConstrainedBeanListener.class).addVetoableChangeListener(this);

        try {
            ConfigSupport.apply(new SingleConfigCode<VirtualServer>() {

                public Object run(VirtualServer param) throws PropertyVetoException, TransactionFailure {
                    // first one is fine...
                    param.setId("foo");
                    param.setAccessLog("Foo");
                    return null;
                }
            }, target);
        } catch(TransactionFailure e) {
            e.printStackTrace();
            result=true;
        }

        ((ConfigBean) ConfigSupport.getImpl(target)).getOptionalFeature(ConstrainedBeanListener.class).removeVetoableChangeListener(this);
        assertTrue(result);
    }


    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
        throw new PropertyVetoException("I don't think so !", evt);
    }
}
