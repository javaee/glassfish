
package org.jvnet.hk2.config.test;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.InjectionTarget;
import org.jvnet.hk2.config.NoopConfigInjector;

@Service(name = "web-container-availability", metadata = "target=org.jvnet.hk2.config.test.WebContainerAvailability,@availability-enabled=optional,@availability-enabled=default:true,@availability-enabled=datatype:java.lang.String,@availability-enabled=leaf,@persistence-type=optional,@persistence-type=default:replicated,@persistence-type=datatype:java.lang.String,@persistence-type=leaf,@persistence-frequency=optional,@persistence-frequency=default:web-method,@persistence-frequency=datatype:java.lang.String,@persistence-frequency=leaf,@persistence-scope=optional,@persistence-scope=default:session,@persistence-scope=datatype:java.lang.String,@persistence-scope=leaf,@persistence-store-health-check-enabled=optional,@persistence-store-health-check-enabled=default:false,@persistence-store-health-check-enabled=datatype:java.lang.Boolean,@persistence-store-health-check-enabled=leaf,@sso-failover-enabled=optional,@sso-failover-enabled=default:false,@sso-failover-enabled=datatype:java.lang.Boolean,@sso-failover-enabled=leaf,@http-session-store-pool-name=optional,@http-session-store-pool-name=datatype:java.lang.String,@http-session-store-pool-name=leaf,@disable-jreplica=optional,@disable-jreplica=default:false,@disable-jreplica=datatype:java.lang.Boolean,@disable-jreplica=leaf")
@InjectionTarget(WebContainerAvailability.class)
public class WebContainerAvailabilityInjector
    extends NoopConfigInjector
{


}
