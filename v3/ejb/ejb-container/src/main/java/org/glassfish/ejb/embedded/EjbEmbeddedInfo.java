package org.glassfish.ejb.embedded;

import org.glassfish.api.embedded.EmbeddedContainerInfo;
import org.glassfish.api.admin.ServerEnvironment;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import com.sun.enterprise.config.serverbeans.EjbContainer;
import com.sun.enterprise.config.serverbeans.Config;

import java.beans.PropertyVetoException;

/**
 * @author Jerome Dochez
 */
@Service(name="ejb")
public class EjbEmbeddedInfo implements EmbeddedContainerInfo<EjbEmbeddedContainer> {

    @Inject(optional=true)
    EjbContainer ejbConfig=null;

    @Inject(name= ServerEnvironment.DEFAULT_INSTANCE_NAME)
    Config config;

    @Inject
    EjbEmbeddedContainer container;

    EjbContainer getConfig() {
        if (ejbConfig==null) {
            try {
                ConfigSupport.apply(new SingleConfigCode<Config>() {
                    public Object run(Config c) throws PropertyVetoException, TransactionFailure {
                        EjbContainer ejb = c.createChild(EjbContainer.class);
                        c.setEjbContainer(ejb);
                        return ejb;
                    }
                }, config);
            } catch(TransactionFailure e) {
                e.printStackTrace();
            }
            ejbConfig = config.getEjbContainer();
        }
        return ejbConfig;
    }

    public EjbEmbeddedContainer create(org.glassfish.api.embedded.Server server) {
        return container;
    }
}
