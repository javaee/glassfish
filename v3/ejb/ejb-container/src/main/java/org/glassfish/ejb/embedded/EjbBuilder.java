package org.glassfish.ejb.embedded;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.component.Habitat;
import org.glassfish.api.embedded.Port;
import org.glassfish.api.embedded.ContainerBuilder;
import org.glassfish.api.admin.ServerEnvironment;
import com.sun.enterprise.config.serverbeans.EjbContainer;
import com.sun.enterprise.config.serverbeans.Config;

import java.beans.PropertyVetoException;

/**
 * @author Jerome Dochez
 */
@Service(name="ejb")
public class EjbBuilder implements ContainerBuilder<EmbeddedEjbContainer> {

    @Inject
    Habitat habitat;    

    @Inject(optional=true)
    EjbContainer ejbConfig=null;

    @Inject(name= ServerEnvironment.DEFAULT_INSTANCE_NAME)
    Config config;

    volatile EmbeddedEjbContainer instance=null;
    

    public synchronized EmbeddedEjbContainer create(org.glassfish.api.embedded.Server server) {
        if (instance==null) {
            instance =  new EmbeddedEjbContainer(this);
        }
        return instance;
    }

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

}

