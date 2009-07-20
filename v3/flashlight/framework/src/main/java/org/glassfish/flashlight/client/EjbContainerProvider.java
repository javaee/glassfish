package org.glassfish.flashlight.client;

import org.glassfish.external.probe.provider.annotations.Probe;
import org.glassfish.external.probe.provider.annotations.ProbeParam;

import java.lang.reflect.Method;

/**
 * @author Mahesh Kannan
 *         Date: Jul 20, 2008
 */
public interface EjbContainerProvider<K, V> {

    @Probe(name="entry")
        public void namedEntry(
            @ProbeParam("method") Method m, @ProbeParam("beanName") String beanName);
    

}
