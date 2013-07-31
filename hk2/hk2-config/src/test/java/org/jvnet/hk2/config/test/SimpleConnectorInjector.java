
package org.jvnet.hk2.config.test;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.InjectionTarget;
import org.jvnet.hk2.config.NoopConfigInjector;

@Service(name = "simple-connector", metadata = "target=org.jvnet.hk2.config.test.SimpleConnector,@port=optional,@port=default:8080,@port=datatype:java.lang.String,@port=leaf,<ejb-container-availability>=org.jvnet.hk2.config.test.EjbContainerAvailability,<web-container-availability>=org.jvnet.hk2.config.test.WebContainerAvailability,<generic-container>=org.jvnet.hk2.config.test.GenericContainer,<*>=collection:org.jvnet.hk2.config.test.GenericContainer")
@InjectionTarget(SimpleConnector.class)
public class SimpleConnectorInjector
    extends NoopConfigInjector
{


}
