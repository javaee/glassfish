
package org.jvnet.hk2.config.test;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.InjectionTarget;
import org.jvnet.hk2.config.NoopConfigInjector;

@Service(name = "ejb-container-availability", metadata = "target=org.jvnet.hk2.config.test.EjbContainerAvailability,@availability-enabled=optional,@availability-enabled=default:true,@availability-enabled=datatype:java.lang.String,@availability-enabled=leaf,@sfsb-ha-persistence-type=optional,@sfsb-ha-persistence-type=default:replicated,@sfsb-ha-persistence-type=datatype:java.lang.String,@sfsb-ha-persistence-type=leaf,@sfsb-persistence-type=optional,@sfsb-persistence-type=default:file,@sfsb-persistence-type=datatype:java.lang.String,@sfsb-persistence-type=leaf,@sfsb-checkpoint-enabled=optional,@sfsb-checkpoint-enabled=datatype:java.lang.String,@sfsb-checkpoint-enabled=leaf,@sfsb-quick-checkpoint-enabled=optional,@sfsb-quick-checkpoint-enabled=datatype:java.lang.String,@sfsb-quick-checkpoint-enabled=leaf,@sfsb-store-pool-name=optional,@sfsb-store-pool-name=datatype:java.lang.String,@sfsb-store-pool-name=leaf")
@InjectionTarget(EjbContainerAvailability.class)
public class EjbContainerAvailabilityInjector
    extends NoopConfigInjector
{


}
