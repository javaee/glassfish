
package org.jvnet.hk2.config.test;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.InjectionTarget;
import org.jvnet.hk2.config.NoopConfigInjector;

@Service(name = "generic-config", metadata = "target=org.jvnet.hk2.config.test.GenericConfig,@name=optional,@name=datatype:java.lang.String,@name=leaf,key=@name,keyed-as=org.jvnet.hk2.config.test.GenericConfig,<generic-config>=org.jvnet.hk2.config.test.GenericConfig")
@InjectionTarget(GenericConfig.class)
public class GenericConfigInjector
    extends NoopConfigInjector
{


}
